package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.PostFilterRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostQueryRepository {
    Slice<Post> findPosts(Long userId, PostFilterRequest filter, Pageable pageable);
}
