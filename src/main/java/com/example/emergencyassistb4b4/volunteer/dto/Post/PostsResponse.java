package com.example.emergencyassistb4b4.volunteer.dto.Post;

import com.example.emergencyassistb4b4.volunteer.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsResponse {
    private Long id;
    private String title;
    private String Nickname;
    private String createdAt;
    private String category;
    private int capacity;

    public static PostsResponse from(Post post) {
        return PostsResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .Nickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt().toString().replace("T", " ").substring(0, 16)) // yyyy-MM-dd HH:mm
                .category(post.getCategory().name())
                .capacity(post.getTotalCapacity())
                .build();
    }
}
