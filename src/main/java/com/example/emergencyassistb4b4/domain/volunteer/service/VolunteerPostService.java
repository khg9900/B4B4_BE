package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceEventListener;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceStateSetEvent;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.*;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.AttendancePolicyProvider;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.AttendancePolicyProvider;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.TeamStatusDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerUpdatedEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerPostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TeamParticipationRedisService teamParticipationRedisService;
    private final VolunteerUpdatedEventProducer producer;
    private final AttendanceEventListener attendanceEventListener;

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
        producer.sendVolunteerUpdatedEvent(event);

        scheduleAttendanceForTeams(post.getTeams(), request.getAttendancePolicy());

    }

    // 모집 게시글 다건 조회
    @Transactional(readOnly = true)
    public Slice<PostsResponse> getPostList(PostFilterRequest filter, Pageable pageable) {

        if (filter.getVolunteerStartDate() != null && filter.getVolunteerEndDate() != null &&
            filter.getVolunteerStartDate().isAfter(filter.getVolunteerEndDate())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        return postRepository.findPosts(null, filter, pageable)
                .map(PostsResponse::from);
    }

    // 모집 게시글 다건 조회 (NGO)
    @Transactional(readOnly = true)
    public Slice<PostsResponse> getMyPostList(Long userId, PostFilterRequest filter, Pageable pageable) {

        if (filter.getVolunteerStartDate() != null && filter.getVolunteerEndDate() != null &&
            filter.getVolunteerStartDate().isAfter(filter.getVolunteerEndDate())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        return postRepository.findPosts(userId, filter, pageable)
            .map(PostsResponse::from);
    }

    // 모집 게시글 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    public void deleteMyPost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorStatus.FORBIDDEN);
        }

        // 게시글 상태가 모집 중일 경우에만 삭제 가능
        if (post.getStatus() != PostStatus.OPEN) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        postRepository.delete(post);
    }

    // 게시글 별 팀 인원 조회
    @Transactional(readOnly = true)
    public PostTeamsResponse getTeamStatus(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        List<TeamStatusDto> teamStatuses = post.getTeams().stream()
                .map(team -> {
                    int currentCount = teamParticipationRedisService.getCurrentCount(postId,team.getId());
                    return new TeamStatusDto(
                            team.getId(),
                            team.getTeamNumber(),
                            team.getMaxCapacity(),
                            currentCount
                    );
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