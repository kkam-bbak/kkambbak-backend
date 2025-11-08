package com.kkambbak.scheduler.aop;

import com.kkambbak.client.discord.DiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionHandlingAspect {

    private final DiscordClient discordWebhookClient;

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduledMethods() {
    }

    @AfterThrowing(pointcut = "scheduledMethods()", throwing = "exception")
    public void handleSchedulerException(JoinPoint joinPoint, Exception exception) {
        String jobName = joinPoint.getTarget().getClass().getSimpleName();

        discordWebhookClient.sendSchedulerError(jobName, exception);

        log.error("Exception occurred during scheduled job execution - errorType: {}, errorMessage: {}, stackTrace: {}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                getStackTraceString(exception));
    }

    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}