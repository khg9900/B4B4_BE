package com.example.emergencyassistb4b4.global.security.auth;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email {}", email);
                    throw new ApiException(ErrorStatus.USER_NOT_FOUND);
                });

        // 비밀번호가 null 이면 예외
        if (!StringUtils.hasText(user.getPassword())) {
            throw new ApiException(ErrorStatus.INVALID_PASSWORD);
        }

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(Long id) { // 로그인 시 시큐리티가 이 메서드 자동 호출함

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        // 시큐리티가 이해할 수 있는 CustomUserDetails 객체 반환
        return new CustomUserDetails(user); // 직접 만든 UserDetails 구현체
    }
}