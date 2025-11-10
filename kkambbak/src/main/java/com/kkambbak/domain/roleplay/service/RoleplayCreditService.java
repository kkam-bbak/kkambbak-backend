package com.kkambbak.domain.roleplay.service;


import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.payment.service.SubscriptionService;
import com.kkambbak.domain.roleplay.exception.PremiumCreditExceedException;
import com.kkambbak.domain.roleplay.exception.StandardCreditExceedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleplayCreditService {
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    private static final int STANDARD_LIMIT = 5;
    private static final int PREMIUM_LIMIT = 15;

    @Transactional
    public void useCredit(User user) {
        SubscriptionStatus subscriptionStatus = subscriptionService.verifySubscriptionStatus(user.getId());
        if(!hasRemainingCredit(user,subscriptionStatus)){
            if(subscriptionStatus == SubscriptionStatus.ACTIVE){
                log.warn("Credit limit exceeded for premium");
                throw new PremiumCreditExceedException();
            }
            else{
                log.warn("Credit limit exceeded for standard");
                throw new StandardCreditExceedException();
            }
        }

        user.incrementUsage();
        userRepository.save(user);
    }

    public boolean hasRemainingCredit(User user,SubscriptionStatus status) {
        int limit = (status == SubscriptionStatus.ACTIVE) ? PREMIUM_LIMIT : STANDARD_LIMIT;
        return user.getRoleplayCount() < limit;
    }


}
