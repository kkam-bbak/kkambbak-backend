package com.kkambbak.scheduler.aop;

import com.kkambbak.client.discord.DiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ScheduledTaskTracingAspect {

    private static final String LINE_SEGMENT = "=".repeat(80);
    private final DiscordClient discordClient;

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    private void methodAnnotatedWithScheduled() {
    }

    @Pointcut("execution(* com.kkambbak.scheduler..*(..))")
    private void atExecutionPointInMyNamespace() {
    }

    @Around("methodAnnotatedWithScheduled() && atExecutionPointInMyNamespace()")
    public Object traceScheduledTask(ProceedingJoinPoint joinPoint) throws Throwable {
        String jobName = joinPoint.getTarget().getClass().getSimpleName();
        StopWatch stopWatch = new StopWatch();

        try {
            log.info(LINE_SEGMENT);
            log.info("{} Start", jobName);
            stopWatch.start();

            Object result = joinPoint.proceed();

            stopWatch.stop();
            double executionTime = stopWatch.getTotalTimeSeconds();
            log.info("{} End -- Total elapsed time = {}s", jobName, executionTime);

            discordClient.sendSchedulerSuccess(jobName, executionTime);

            return result;

        } catch (Throwable e) {
            stopWatch.stop();
            throw e;
        }
    }

    @AfterThrowing(pointcut = "methodAnnotatedWithScheduled() && atExecutionPointInMyNamespace()", throwing = "exception")
    public void handleException(JoinPoint joinPoint, Throwable exception) {
        String jobName = joinPoint.getTarget().getClass().getSimpleName();
        log.error("[{}] threw an exception: ", jobName, exception);
    }
}