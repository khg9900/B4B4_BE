package com.example.emergencyassistb4b4.domain.volunteer.controller;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.*;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerPostService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class VolunteerPostController {

    private final VolunteerPostService volunteerPostService;

    @PreAuthorize("hasRole('NGO')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request) {
        volunteerPostService.createPost(userDetails.getUser().getId(), request);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_CREATE_SUCCESS, null);
    }

    @PreAuthorize("hasRole('NGO')")
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        volunteerPostService.updatePost(userDetails.getUser().getId(), postId, request);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, null);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<PostTotalResponse>>> getPosts(
        @ModelAttribute PostFilterRequest filter,
        Pageable pageable
    ) {
        Slice<PostTotalResponse> response = volunteerPostService.getPostList(filter, pageable);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, response);
    }

    @PreAuthorize("hasRole('NGO')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Slice<PostsResponse>>> getMyPosts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @ModelAttribute PostFilterRequest filter,
        Pageable pageable
    ) {
        Slice<PostsResponse> response = volunteerPostService.getMyPostList(userDetails.getUser().getId(), filter, pageable);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        PostDetailResponse response = volunteerPostService.getPost(postId);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, response);
    }

    @GetMapping("/{postId}/teams")
    public ResponseEntity<ApiResponse<PostTeamsResponse>> getTeamStatus(@PathVariable Long postId) {
        PostTeamsResponse response = volunteerPostService.getTeamStatus(postId);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, response);
    }

    @PreAuthorize("hasRole('NGO')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyPost(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        volunteerPostService.deleteMyPost(userDetails.getUser().getId(), postId);  // 소유자 검증 + 삭제
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, null);
    }

}