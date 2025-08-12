//package com.example.emergencyassistb4b4.volunteer.service;
//
//import com.example.emergencyassistb4b4.domain.user.domain.User;
//import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
//import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
//import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.CreatePostRequest;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.UpdatePostRequest;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
//import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
//import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerPostService;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//class VolunteerPostServiceTest {
//
//    @Autowired
//    private VolunteerPostService volunteerPostService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Test
//    @DisplayName("게시글 생성이 성공적으로 동작한다")
//    void createPost_success() {
//        // given
//        User user = userRepository.save(User.builder()
//                .email("test@example.com")
//                .password("pw")
//                .loginType(LoginType.LOCAL)
//                .userRole(UserRole.NGO)
//                .build());
//
//        CreatePostRequest request = CreatePostRequest.builder()
//                .title("봉사 테스트")
//                .content("도와주세요")
//                .category("RECRUITMENT")
//                .totalCapacity(10)
//                .teamSize(3)
//                .location(PostLocationDto.builder()
//                        .placeName("서울역")
//                        .latitude(Double.valueOf(37.555))
//                        .longitude(Double.valueOf(126.9707))
//                        .build())
//                .attendancePolicy(PostAttendancePolicyDto.builder()
//                        .checkinStart(LocalDateTime.now())
//                        .checkinEnd(LocalDateTime.now().plusHours(2))
//                        .allowedRadiusM(150)
//                        .minStayMinutes(45)
//                        .build())
//                .build();
//
//        // when
//        volunteerPostService.createPost(user.getId(), request);
//
//        // then
//        List<Post> posts = postRepository.findAll();
//        assertThat(posts).hasSize(1);
//        Post post = posts.get(0);
//        assertThat(post.getTitle()).isEqualTo("봉사 테스트");
//        assertThat(post.getTeams()).isNotEmpty(); // 팀 생성 검증
//    }
//
//    @Test
//    @DisplayName("게시글 수정이 성공적으로 동작한다")
//    void updatePost_success() {
//        // given
//        User user = userRepository.save(User.builder()
//                .email("test2@example.com")
//                .password("pw")
//                .loginType(LoginType.LOCAL)
//                .userRole(UserRole.NGO)
//                .build());
//
//        CreatePostRequest request = CreatePostRequest.builder()
//                .title("수정 전 제목")
//                .content("수정 전 내용")
//                .category("RECRUITMENT")
//                .totalCapacity(6)
//                .teamSize(2)
//                .location(PostLocationDto.builder()
//                        .placeName("강남역")
//                        .latitude(Double.valueOf(37.4979))
//                        .longitude(Double.valueOf(127.0276))
//                        .build())
//                .attendancePolicy(PostAttendancePolicyDto.builder()
//                        .checkinStart(LocalDateTime.now())
//                        .checkinEnd(LocalDateTime.now().plusHours(1))
//                        .allowedRadiusM(100)
//                        .minStayMinutes(30)
//                        .build())
//                .build();
//
//        volunteerPostService.createPost(user.getId(), request);
//        Post post = postRepository.findAll().get(0);
//
//        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
//                .location(PostLocationDto.builder()
//                        .placeName("수정된 장소")
//                        .latitude(Double.valueOf(35.1234))
//                        .longitude(Double.valueOf(129.1234))
//                        .build())
//                .attendancePolicy(PostAttendancePolicyDto.builder()
//                        .checkinStart(LocalDateTime.now())
//                        .checkinEnd(LocalDateTime.now().plusHours(3))
//                        .allowedRadiusM(300)
//                        .minStayMinutes(60)
//                        .build())
//                .build();
//
//        // when
//        volunteerPostService.updatePost(user.getId(), post.getId(), updateRequest);
//
//        // then
//        Post updated = postRepository.findById(post.getId()).orElseThrow();
//        assertThat(updated.getLocation().getPlaceName()).isEqualTo("수정된 장소");
//        assertThat(updated.getAttendancePolicy().getMinCheckinMinutes()).isEqualTo(60);
//    }
//}
//
