//package com.example.emergencyassistb4b4.volunteer.service;
//
//import com.example.emergencyassistb4b4.domain.user.domain.User;
//
//import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
//import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.CreatePostRequest;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
//import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
//import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
//import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerJoinService;
//import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerPostService;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@SpringBootTest
//@Rollback(false)
//class VolunteerJoinConcurrencyTest {
//
//    @Autowired private VolunteerJoinService volunteerJoinService;
//    @Autowired private UserRepository userRepository;
//    @Autowired private VolunteerPostService volunteerPostService;
//    @Autowired private VolunteerParticipantRepository participantRepository;
//
//    @BeforeEach
//    void setUp() {
//        // 유저 30명 생성
//        for (int i = 1; i <= 30; i++) {
//            userRepository.save(User.builder()
//                    .email("user" + i + "@test.com")
//                    .password("1234")
//                    .loginType(LoginType.LOCAL)
//                    .provider(null)
//                    .userRole(UserRole.IND)
//                    .build());
//        }
//
//        // 게시글 + 팀 자동 생성
//        User admin = userRepository.findAll().get(0);
//
//        PostLocationDto locationDto = PostLocationDto.builder()
//                .placeName("서울시 강남구")
//                .latitude(new Double("37.4979"))
//                .longitude(new Double("127.0276"))
//                .build();
//
//        PostAttendancePolicyDto policyDto = PostAttendancePolicyDto.builder()
//                .checkinStart(LocalDateTime.now().plusDays(1))
//                .checkinEnd(LocalDateTime.now().plusDays(1).plusHours(1))
//                .allowedRadiusM(150)
//                .minStayMinutes(30)
//                .build();
//
//        CreatePostRequest request = CreatePostRequest.builder()
//                .title("테스트 게시글")
//                .content("내용 없음")
//                .category("RECRUITMENT")
//                .totalCapacity(100)
//                .teamSize(20)
//                .location(locationDto)
//                .attendancePolicy(policyDto)
//                .build();
//
//        volunteerPostService.createPost(admin.getId(), request);
//    }
//
//    @Test
//    void 동시에_30명이_신청하면_20명만_성공한다() throws InterruptedException {
//        int threadCount = 30;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        for (long i = 1; i <= threadCount; i++) {
//            final long userId = i;
//            final long postId = 1;
//
//            executorService.submit(() -> {
//                try {
//                    volunteerJoinService.joinTeam(postId, 1, userId);
//                } catch (RuntimeException e) {
//                    System.out.println("중복/초과");
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//
//        long count = participantRepository.count();
//        Assertions.assertEquals(20, count);
//    }
//
//}