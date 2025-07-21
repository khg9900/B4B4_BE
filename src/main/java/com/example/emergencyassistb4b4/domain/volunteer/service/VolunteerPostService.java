package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.*;
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
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.kafka.producer.VolunteerUpdatedEventProducer;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerPostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TeamParticipationRedisService teamParticipationRedisService;
    private final VolunteerUpdatedEventProducer producer;

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
    }

    // 모집 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        // 위치 수정
        PostLocationDto location = request.getLocation();
        post.getLocation().update(
                location.getPlaceName(),
                location.getLatitude(),
                location.getLongitude()
        );

        // 출석 정책 수정
        PostAttendancePolicyDto policy = request.getAttendancePolicy();
        post.getAttendancePolicy().update(
                policy.getCheckinStart(),
                policy.getCheckinEnd(),
                policy.getAllowedRadiusM(),
                policy.getMinStayMinutes()
        );

        // kafka 메세지 발행
        VolunteerUpdatedEvent event = VolunteerUpdatedEvent.from(post);
        producer.sendVolunteerUpdatedEvent(event);
    }

    // 모집 게시글 다건 조회
    @Transactional(readOnly = true)
    public Slice<PostsResponse> getPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostsResponse::from);
    }

    // 모집 게시글 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    // 게시글 별 팀 인원 조회
    @Transactional(readOnly = true)
    public PostTeamsResponse getTeamStatus(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        List<TeamStatusDto> teamStatuses = post.getTeams().stream()
                .map(team -> {
                    int currentCount = teamParticipationRedisService.getCurrentCount(team.getId());
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

}