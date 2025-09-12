package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceEventListener;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceStateSetEvent;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.*;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.AttendancePolicyProvider;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TTLRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerCancelEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerCancelEvent;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.TeamStatusDto;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerUpdatedEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerPostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TeamParticipationRedisService teamParticipationRedisService;
    private final VolunteerUpdatedEventProducer producer;
    private final AttendanceEventListener attendanceEventListener;
    private final VolunteerCancelEventProducer volunteerCancelEventProducer;
    private final TTLRedisService ttlRedisService;
    private final RabbitMQRedisService rabbitMQRedisService;

    // 모집 게시글 생성
    @Transactional
    public void createPost(Long userId, CreatePostRequest request) {

        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        // Post 생성
        Post post = request.toEntity(user);

        // 팀 생성
        List<VolunteerTeam> teams = generateTeams(post, request.getTotalCapacity(), request.getTeamSize());
        post.addTeams(teams);

        // 저장
        postRepository.save(post);

        scheduleAttendanceForTeams(teams, request.getAttendancePolicy());

    }

    // 모집 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        // 업데이트
        post.update(request);

        // kafka 메세지 발행
        VolunteerUpdatedEvent event = VolunteerUpdatedEvent.from(post);

        scheduleAttendanceForTeams(post.getTeams(), request.getAttendancePolicy());

        producer.sendVolunteerUpdatedEvent(event);

    }

    // 모집 게시글 다건 조회
    @Transactional(readOnly = true)
    public Slice<PostTotalResponse> getPostList(PostFilterRequest filter, Pageable pageable) {

        if (filter.getVolunteerStartDate() != null && filter.getVolunteerEndDate() != null &&
                filter.getVolunteerStartDate().isAfter(filter.getVolunteerEndDate())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        Slice<Post> posts = postRepository.findPosts(null, filter, pageable);

        return posts.map(post -> {
            int currentParticipants = post.getTeams().stream().mapToInt(team -> {
                var period = postRepository.findCheckinPeriodByPostId(post.getId()).orElse(null);
                boolean expired = period != null && LocalDateTime.now().isAfter(period.checkinEnd());
                return expired ? 0 : teamParticipationRedisService.getCurrentCount(post.getId(), team.getId());
            }).sum();
            return PostTotalResponse.from(post, currentParticipants);
        });
    }

    // 모집 게시글 다건 조회 (NGO)
    @Transactional(readOnly = true)
    public Slice<PostTotalResponse> getMyPostList(Long userId, PostFilterRequest filter, Pageable pageable) {
        if (filter.getVolunteerStartDate() != null && filter.getVolunteerEndDate() != null &&
                filter.getVolunteerStartDate().isAfter(filter.getVolunteerEndDate())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        Slice<Post> posts = postRepository.findPosts(userId, filter, pageable);

        return posts.map(post -> {
            int currentParticipants = post.getTeams().stream().mapToInt(team -> {
                var period = postRepository.findCheckinPeriodByPostId(post.getId()).orElse(null);
                boolean expired = period != null && LocalDateTime.now().isAfter(period.checkinEnd());
                return expired ? 0 : teamParticipationRedisService.getCurrentCount(post.getId(), team.getId());
            }).sum();
            return PostTotalResponse.from(post, currentParticipants);
        });
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


        for (VolunteerTeam volunteerTeam: post.getTeams()){
            try {
                rabbitMQRedisService.clearTrackingState(volunteerTeam.getId());
            } catch (Exception e) {
                log.error("TrackingState 삭제 실패 teamId={} : {}", volunteerTeam.getId(), e.getMessage());
                // 필요시 재시도 큐나 DLQ로 이동
            }
        }

        ttlRedisService.deleteAllKeysByPostId(postId);
        postRepository.delete(post);
    }


    @Transactional(readOnly = true)
    public TeamParticipantsResponse getTeamParticipants(Long postId, Long teamId){
        // Redis 또는 DB 기반으로 팀 참여자 상태 조회
        VolunteerTeam volunteerTeam = postRepository.findTeamByPostIdAndTeamId(postId, teamId).
                orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        return TeamParticipantsResponse.fromEntities(volunteerTeam.getId(), volunteerTeam.getTeamNumber(),volunteerTeam.getParticipants());
    }

    @Transactional
    public void  updateParticipantAttendance(Long postId, Long teamId, Long participantId, CheckinStatusRequest checkinStatusRequest){

        VolunteerParticipant volunteerParticipant=postRepository.findParticipantInTeam(postId,teamId,participantId)
                                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));;

        volunteerParticipant.updateStatus(checkinStatusRequest.getStatus());

    }

    // 게시글 별 팀 인원 조회
    @Transactional(readOnly = true)
    public PostTeamsResponse getTeamStatus(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        List<TeamStatusDto> teamStatuses = post.getTeams().stream().map(team -> {
            var period = postRepository.findCheckinPeriodByPostId(postId).orElse(null);
            boolean expired = period != null && LocalDateTime.now().isAfter(period.checkinEnd());
            int currentCount = expired ? 0 : teamParticipationRedisService.getCurrentCount(postId, team.getId());
            return new TeamStatusDto(team.getId(), team.getTeamNumber(), team.getMaxCapacity(), currentCount);
        }).toList();

        return new PostTeamsResponse(post.getId(), teamStatuses);
    }

    // 팀 생성
    private List<VolunteerTeam> generateTeams(Post post, int totalCapacity, int teamSize) {
        List<VolunteerTeam> volunteerTeams = new ArrayList<>();
        int teamCount = totalCapacity / teamSize;

        for (int i = 0; i < teamCount; i++) {
            VolunteerTeam team = VolunteerTeam.builder()
                    .post(post)
                    .teamNumber(i+1)
                    .maxCapacity(teamSize)
                    .build();
            volunteerTeams.add(team);
        }
        return volunteerTeams;
    }

    // 제네릭 메서드로 변경
    private <T extends AttendancePolicyProvider> void scheduleAttendanceForTeams(
            List<VolunteerTeam> teams,
            T request
    ) {
        LocalDateTime checkinStart = request.getAttendancePolicy().getCheckinStart();

        teams.stream()
                .map(team -> new AttendanceStateSetEvent(team.getId(), checkinStart))
                .forEach(attendanceEventListener::onAttendanceStateSet);
    }

}