package com.vanguard.weatherapp.aop;

import com.vanguard.weatherapp.entity.TokenHistory;
import com.vanguard.weatherapp.entity.UserToken;
import com.vanguard.weatherapp.exception.ExceededRateLimitException;
import com.vanguard.weatherapp.exception.InvalidTokenException;
import com.vanguard.weatherapp.repository.TokenHistoryRepository;
import com.vanguard.weatherapp.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final static String TARGET_PARAM_NAME = "token";
    private final static long RATE_LIMIT_WINDOW_HOUR = 1L;

    private final UserTokenRepository userTokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;


    @SneakyThrows
    @Around("@annotation(RateLimit)")
    public Object isLimitExceeded(ProceedingJoinPoint joinPoint) {

        String token = getUserToken(joinPoint);

        Optional<UserToken> foundTokenEntity = userTokenRepository.findByToken(token);

        OffsetDateTime accessTime = OffsetDateTime.now();

        boolean isExceeded = foundTokenEntity.map(
                tokenEntity -> {
                    int count = tokenHistoryRepository.countByTokenIdAndAfter(tokenEntity.getId(), accessTime.minusHours(RATE_LIMIT_WINDOW_HOUR));
                    if (count >= tokenEntity.getRateLimit()) {
                        return true;
                    }
                    tokenHistoryRepository.save(TokenHistory.builder().userToken(tokenEntity).build());
                    return false;
                }
        ).orElseThrow(() -> {
            log.warn("The given user token: {} cannot be found", token);
            return new InvalidTokenException("Invalid user token");
        });

        if (isExceeded) {
            log.warn("Request is rejected due to rate limit, token: {}", foundTokenEntity.get());
            throw new ExceededRateLimitException("Rate limit is already exceeded");
        }
        return joinPoint.proceed();
    }

    private String getUserToken(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = getParams(joinPoint);
        return (String) params.get(TARGET_PARAM_NAME);
    }

    private Map<String, Object> getParams(ProceedingJoinPoint joinPoint) {
        Object[] paramValues = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

        return IntStream.range(0, paramNames.length)
                .boxed()
                .collect(
                        toMap(
                                index -> paramNames[index],
                                index -> paramValues[index]
                        )
                );
    }
}
