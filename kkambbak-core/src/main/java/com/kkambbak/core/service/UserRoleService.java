package com.kkambbak.core.service;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.UserRole;
import com.kkambbak.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow();
    }

    @Transactional
    public void upgradeRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getRole() != UserRole.PREMIUM) {
            user.setRole(UserRole.PREMIUM);
            userRepository.save(user);
        }
    }

    @Transactional
    public void downgradeRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getRole() != UserRole.STANDARD) {
            user.setRole(UserRole.STANDARD);
            userRepository.save(user);
        }
    }
}