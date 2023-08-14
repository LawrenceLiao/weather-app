package com.vanguard.weatherapp.aop;

import com.vanguard.weatherapp.entity.TokenHistory;
import com.vanguard.weatherapp.entity.UserToken;
import com.vanguard.weatherapp.exception.ExceededRateLimitException;
import com.vanguard.weatherapp.exception.InvalidTokenException;
import com.vanguard.weatherapp.fixture.StubsUserToken;
import com.vanguard.weatherapp.repository.TokenHistoryRepository;
import com.vanguard.weatherapp.repository.UserTokenRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RateLimitAspectTest {

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private TokenHistoryRepository tokenHistoryRepository;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private final static String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        when(joinPoint.getArgs()).thenReturn(stubArgs(TOKEN));
        when(joinPoint.getSignature()).thenReturn(stubSignature());
    }

    @Test
    void shouldInvokeTheProxiedMethodWhenRateLimitDoesNotReach() throws Throwable {
        UserToken userToken = StubsUserToken.stubUserToken(TOKEN);

        when(userTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(userToken));
        when(tokenHistoryRepository.countByTokenIdAndAfter(anyLong(), any(OffsetDateTime.class))).thenReturn(1);

        rateLimitAspect.isLimitExceeded(joinPoint);

        verify(tokenHistoryRepository).save(any(TokenHistory.class));
        verify(joinPoint).proceed();
    }

    @Test
    void shouldThrowExceedRateLimitExceptionAndInterceptTheInvocationOfTargetedMethodWhenRateLimitExceeds() throws Throwable {
        UserToken userToken = StubsUserToken.stubUserToken(TOKEN);

        when(userTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(userToken));
        when(tokenHistoryRepository.countByTokenIdAndAfter(anyLong(), any(OffsetDateTime.class))).thenReturn(5);

        assertThrows(ExceededRateLimitException.class, () -> rateLimitAspect.isLimitExceeded(joinPoint));

        verify(tokenHistoryRepository, never()).save(any(TokenHistory.class));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void shouldThrowInvalidTokenExceptionAndInterceptTheInvocationOfTargetedMethodWhenTokenIsNotFoundInDB() throws Throwable {
        when(userTokenRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> rateLimitAspect.isLimitExceeded(joinPoint));

        verify(tokenHistoryRepository, never()).countByTokenIdAndAfter(anyLong(), any(OffsetDateTime.class));
        verify(tokenHistoryRepository, never()).save(any(TokenHistory.class));
        verify(joinPoint, never()).proceed();
    }

    private String[] stubArgs(String token) {
        return new String[] {"melbourne", "au", token};
    }
    private CodeSignature stubSignature() {
        return new CodeSignature() {
            @Override
            public Class[] getParameterTypes() {
                return new Class[0];
            }

            @Override
            public String[] getParameterNames() {
                return new String[] {"city", "country", "token"};
            }

            @Override
            public Class[] getExceptionTypes() {
                return new Class[0];
            }

            @Override
            public String toShortString() {
                return null;
            }

            @Override
            public String toLongString() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public int getModifiers() {
                return 0;
            }

            @Override
            public Class getDeclaringType() {
                return null;
            }

            @Override
            public String getDeclaringTypeName() {
                return null;
            }
        };
    }
}
