package com.kkambbak.global.security;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UsernameNotFoundException("Failed Load User By UserId: " + userId));
        return createUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Failed Load User By UserId: " + userId));
        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        return UserDetailsImpl.builder()
                .userId(user.getId())
                .genderType(user.getGender())
                .roles(List.of("ROLE_USER"))
                .build();
    }
}