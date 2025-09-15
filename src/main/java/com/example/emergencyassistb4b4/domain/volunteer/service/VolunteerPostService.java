package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceEventListener;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceStateSetEvent;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.TeamStatusDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.*;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.AttendancePolicyProvider;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TTLRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerCancelEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerUpdatedEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerCancelEvent;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerPostService {

    private final PostRepository postRepository;
    private final TeamParticipationRedisService teamParticipationRedisService;
    private final VolunteerUpdatedEventProducer producer;
    private final AttendanceEventListener attendanceEventListener;
    private final VolunteerCancelEventProducer volunteerCancelEventProducer;
    private final TTLRedisService ttlRedisService;
    private final RabbitMQRedisService rabbitMQRedisService;

    @Transactional
    public void createPost(User user, CreatePostRequest request) {
        Post post = request.toEntity(user);
        List<VolunteerTeam> teams = generateTeams(post, request.getTotalCapacity(), request.getTeamSize());
        post.addTeams(teams);
        postRepository.save(post);
        scheduleAttendanceForTeams(teams, request.getAttendancePolicy());
    }

    @Transactional
    public void updatePost(User user, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        post.update(request);

        // kafka 메세지 발행
        VolunteerUpdatedEvent event = VolunteerUpdatedEvent.from(post);
        scheduleAttendanceForTeams(post.getTeams(), request.getAttendancePolicy());
        producer.sendVolunteerUpdatedEvent(event);
    }

    @Transactional(readOnly = true)
    public Slice<PostTotalResponse> getPostList(PostFilterRequest filter, Pageable pageable) {
        validateFilterDates(filter);
        Slice<Post> posts = postRepository.findPosts(null, filter, pageable);
        return getPostTotalResponses(posts);
    }

    @Transactional(readOnly = true)
    public Slice<PostTotalResponse> getMyPostList(Long userId, PostFilterRequest filter, Pageable pageable) {
        validateFilterDates(filter);
        Slice<Post> posts = postRepository.findPosts(userId, filter, pageable);
        return getPostTotalResponses(posts);
    }


    // 모집 게시글 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    @Transactional
    public void deleteMyPost(Long userId, Long postId) {

        Post post = postRepository.findByIdWithTeams(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorStatus.FORBIDDEN);
        }

        if (post.getStatus() != PostStatus.OPEN) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        VolunteerCancelEvent event = VolunteerCancelEvent.from(post);

        try {
            // Kafka 전송 성공해야 다음으로 진행
            volunteerCancelEventProducer.sendVolunteerCanceledEvent(event);
            log.info("Kafka 발행 성공: {}", event);
        } catch (Exception e) {
            log.error("Kafka 발행 실패, 롤백 처리: {}", event, e);
            throw new ApiException(ErrorStatus.KAFKA_SEND_FAILED);
        }

        post.getTeams().forEach(team -> {
            try {
                rabbitMQRedisService.clearTrackingState(team.getId());
            } catch (Exception e) {
                log.error("TrackingState 삭제 실패 teamId={} : {}", team.getId(), e.getMessage());
            }
        });

        ttlRedisService.deleteAllKeysByPostId(postId);
        postRepository.delete(post);
    }


    @Transactional(readOnly = true)
    public TeamParticipantsResponse getTeamParticipants(Long postId, Long teamId) {
        VolunteerTeam volunteerTeam = postRepository.findTeamByPostIdAndTeamId(postId, teamId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        return TeamParticipantsResponse.fromEntities(volunteerTeam.getId(),
                volunteerTeam.getTeamNumber(),
                volunteerTeam.getParticipants());
    }

    @Transactional
    public void updateParticipantAttendance(Long postId, Long teamId, Long participantId, CheckinStatusRequest checkinStatusRequest) {
        VolunteerParticipant volunteerParticipant = postRepository.findParticipantInTeam(postId, teamId, participantId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        volunteerParticipant.updateStatus(checkinStatusRequest.getStatus());

    }

    // 게시글 별 팀 인원 조회
    @Transactional(readOnly = true)
    public PostTeamsResponse getTeamStatus(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        var periodOpt = postRepository.findCheckinPeriodByPostId(postId);
        LocalDateTime now = LocalDateTime.now();
        boolean expired = periodOpt.map(p -> now.isAfter(p.checkinEnd())).orElse(false);

        List<TeamStatusDto> teamStatuses = post.getTeams().stream()
                .map(team -> new TeamStatusDto(team.getId(),
                        team.getTeamNumber(),
                        team.getMaxCapacity(),
                        expired ? 0 : teamParticipationRedisService.getCurrentCount(postId, team.getId())))
                .toList();

        return new PostTeamsResponse(post.getId(), teamStatuses);
    }

    private Slice<PostTotalResponse> getPostTotalResponses(Slice<Post> posts) {
        LocalDateTime now = LocalDateTime.now();

        return posts.map(post -> {
            var periodOpt = postRepository.findCheckinPeriodByPostId(post.getId());
            boolean expired = periodOpt.map(p -> now.isAfter(p.checkinEnd())).orElse(false);

            int currentParticipants = post.getTeams().stream()
                    .mapToInt(team -> expired ? 0 : teamParticipationRedisService.getCurrentCount(post.getId(), team.getId()))
                    .sum();

            return PostTotalResponse.from(post, currentParticipants);
        });
    }

    private void validateFilterDates(PostFilterRequest filter) {
        if (filter.getVolunteerStartDate() != null &&
                filter.getVolunteerEndDate() != null &&
                filter.getVolunteerStartDate().isAfter(filter.getVolunteerEndDate())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
    }

    private List<VolunteerTeam> generateTeams(Post post, int totalCapacity, int teamSize) {

        List<VolunteerTeam> volunteerTeams = new ArrayList<>();
        int teamCount = totalCapacity / teamSize;

        for (int i = 0; i < teamCount; i++) {
            volunteerTeams.add(VolunteerTeam.builder()
                    .post(post)
                    .teamNumber(i + 1)
                    .maxCapacity(teamSize)
                    .build());
        }

        return volunteerTeams;
    }

    private <T extends AttendancePolicyProvider> void scheduleAttendanceForTeams(List<VolunteerTeam> teams, T request) {
        LocalDateTime checkinStart = request.getAttendancePolicy().getCheckinStart();
        teams.stream()
                .map(team -> new AttendanceStateSetEvent(team.getId(), checkinStart))
                .forEach(attendanceEventListener::onAttendanceStateSet);
    }

}