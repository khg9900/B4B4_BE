package com.example.emergencyassistb4b4.domain.volunteer.scheduler;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VolunteerStatusUpdateScheduler {
    private final PostRepository postRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateVolunteerStatus() {
        List<Post> posts = postRepository.findAllExpiredPosts(LocalDate.now());
        posts.forEach(post -> post.setStatus(PostStatus.CLOSED));
        postRepository.saveAll(posts);
    }
}
