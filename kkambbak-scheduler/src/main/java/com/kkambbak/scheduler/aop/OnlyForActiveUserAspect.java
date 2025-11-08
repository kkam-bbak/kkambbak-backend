package com.kkambbak.scheduler.aop;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OnlyForActiveUserAspect {

    @Around("@annotation(onlyForActiveUser) && args(user,..)")
    public Object checkActiveUser(ProceedingJoinPoint joinPoint, User user, OnlyForActiveUser onlyForActiveUser) throws Throwable {
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.info("Ignore operation for inactive user - method: {}, userId: {}, userStatus: {}",
                    joinPoint.getSignature().getName(),
                    user.getId(),
                    user.getStatus());
            return false;
        }

        return joinPoint.proceed();
    }
}