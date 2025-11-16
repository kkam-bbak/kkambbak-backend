package com.kkambbak.domain.user.facade;

import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.domain.user.dto.UpdateProfileDto;
import com.kkambbak.domain.user.dto.registerKoreanDto;
import com.kkambbak.domain.user.exception.ProfileUpdateException;
import com.kkambbak.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    @Transactional
    public void register(Long userId, UpdateProfileDto request) {
        Gender gender = userService.validateProfileRequest(request);

        try {
            userService.register(userId, request, gender);
        } catch (Exception e) {
            log.error("Failed to register profile for userId: {}", userId, e);
            throw new ProfileUpdateException();
        }
    }

    @Transactional
    public void registerKorean(Long userId, registerKoreanDto request) {
        userService.validateKoreanNameRequest(request);

        try {
            userService.registerKorean(userId, request);
        } catch (Exception e) {
            log.error("Failed to register korean name for userId: {}", userId, e);
            throw new ProfileUpdateException();
        }
    }
}