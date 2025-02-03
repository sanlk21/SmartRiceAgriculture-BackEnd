package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.QueryHint;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherPredictionRepository extends JpaRepository<WeatherPrediction, Long> {

    @Query("SELECT w FROM WeatherPrediction w " +
            "WHERE w.locationId = :locationId " +
            "AND w.predictionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY w.predictionDate ASC")
    @QueryHints({
            @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
            @QueryHint(name = org.hibernate.annotations.QueryHints.READ_ONLY, value = "true")
    })
    List<WeatherPrediction> findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(
            @Param("locationId") Integer locationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT DISTINCT w.locationId FROM WeatherPrediction w WHERE w.predictionDate >= :currentDate")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<Integer> findDistinctLocationIds(@Param("currentDate") LocalDateTime currentDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM WeatherPrediction w WHERE w.predictionDate < :date")
    void deleteByPredictionDateBefore(@Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("DELETE FROM WeatherPrediction w WHERE w.locationId = :locationId AND w.predictionDate < :date")
    void deleteByLocationIdAndPredictionDateBefore(
            @Param("locationId") Integer locationId,
            @Param("date") LocalDateTime date
    );

    // New helper method to check prediction count
    @Query("SELECT COUNT(w) FROM WeatherPrediction w " +
            "WHERE w.locationId = :locationId " +
            "AND w.predictionDate BETWEEN :startDate AND :endDate")
    long countPredictionsForDateRange(
            @Param("locationId") Integer locationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}