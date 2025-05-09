package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.TourDetail;
import jakarta.persistence.LockModeType; // <-- IMPORT LockModeType
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock; // <-- IMPORT Lock annotation
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // <-- IMPORT Optional

@Repository
public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {

    /**
     * Finds a TourDetail by its ID and acquires a pessimistic write lock.
     * This prevents other transactions from reading (with certain lock modes) or updating
     * this row until the current transaction completes.
     *
     * @param id The ID of the TourDetail to find and lock.
     * @return An Optional containing the locked TourDetail if found, otherwise empty.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE) // <-- ADD PESSIMISTIC WRITE LOCK
    @Query("SELECT td FROM TourDetail td WHERE td.id = :id") // Query to find by ID
    Optional<TourDetail> findByIdForUpdate(@Param("id") Long id);


    // --- Các phương thức khác giữ nguyên ---

    @Query("SELECT td FROM TourDetail td JOIN FETCH td.tour t " +
           "WHERE td.status = 'AVAILABLE' " +
           "AND t.status = 'ACTIVE' " +
           "AND td.timeStart > :currentTime " +
           "AND td.amountCustomer < td.maxCapacity " +
           "ORDER BY td.timeStart ASC")
    List<TourDetail> findAvailableTourDetails(@Param("currentTime") LocalDateTime currentTime);

    @Query(value = "SELECT td FROM TourDetail td JOIN FETCH td.tour t " +
                   "WHERE td.status = 'AVAILABLE' " +
                   "AND t.status = 'ACTIVE' " +
                   "AND td.timeStart > :currentTime " +
                   "AND td.amountCustomer < td.maxCapacity ",
           countQuery = "SELECT count(td) FROM TourDetail td JOIN td.tour t " +
                        "WHERE td.status = 'AVAILABLE' " +
                        "AND t.status = 'ACTIVE' " +
                        "AND td.timeStart > :currentTime " +
                        "AND td.amountCustomer < td.maxCapacity ")
    Page<TourDetail> findAvailableTourDetailsPaginated(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);


    @Query("SELECT td FROM TourDetail td JOIN FETCH td.tour t " +
           "WHERE td.status = 'AVAILABLE' " +
           "AND t.status = 'ACTIVE' " +
           "AND td.timeStart > :currentTime " +
           "AND td.amountCustomer < td.maxCapacity " +
           "AND LOWER(t.name) LIKE LOWER(concat('%', :tourName, '%')) " +
           "ORDER BY td.timeStart ASC")
    List<TourDetail> findAvailableTourDetailsByName(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("tourName") String tourName
    );

     @Query("SELECT td FROM TourDetail td JOIN FETCH td.tour t " +
            "WHERE td.tour.id = :tourId " +
            "AND td.status = 'AVAILABLE' " +
            "AND td.timeStart > :currentTime " +
            "AND td.amountCustomer < td.maxCapacity " +
            "ORDER BY td.timeStart ASC")
     List<TourDetail> findAvailableByTourId(
             @Param("tourId") Long tourId,
             @Param("currentTime") LocalDateTime currentTime
     );
}
