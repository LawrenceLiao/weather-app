package com.vanguard.weatherapp.repository;

import com.vanguard.weatherapp.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {
    @Query("SELECT COUNT(th) FROM TokenHistory th WHERE th.userToken.id = :tokenId AND th.accessAt > :accessTime")
    int countByTokenIdAndAfter(@Param("tokenId") Long tokenId, @Param("accessTime") OffsetDateTime accessTime);
}
