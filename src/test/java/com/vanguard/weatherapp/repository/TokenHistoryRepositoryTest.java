package com.vanguard.weatherapp.repository;

import com.vanguard.weatherapp.entity.TokenHistory;
import com.vanguard.weatherapp.entity.UserToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
public class TokenHistoryRepositoryTest {

    @Autowired
    private TokenHistoryRepository tokenHistoryRepository;

    @Autowired
    private UserTokenRepository userTokenRepository;
    private final static String TOKEN = "test-token";


    @Test
    void shouldGetHowManyTimesTheTokenIsUsedByUserTokenIdFromDatabase() {
        UserToken userToken = userTokenRepository.save(UserToken.builder().token(TOKEN).build());
        long userTokenId = userToken.getId();

        int initialAmount = tokenHistoryRepository.countByTokenIdAndAfter(userTokenId, OffsetDateTime.now().minusMinutes(10));

        assertThat(initialAmount).isEqualTo(0);

        List<TokenHistory> histories = List.of(
                TokenHistory.builder().userToken(userToken).build(),
                TokenHistory.builder().userToken(userToken).build(),
                TokenHistory.builder().userToken(userToken).build()
        );

        tokenHistoryRepository.saveAll(histories);

        int amountAfter = tokenHistoryRepository.countByTokenIdAndAfter(userTokenId, OffsetDateTime.now().minusMinutes(10));

        assertThat(amountAfter).isEqualTo(histories.size());
    }
}
