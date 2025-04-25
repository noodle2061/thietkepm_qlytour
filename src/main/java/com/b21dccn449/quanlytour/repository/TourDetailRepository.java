package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.TourDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {

    // Tìm các TourDetail đang mở bán (status='AVAILABLE' và thời gian bắt đầu trong tương lai)
    // và còn chỗ trống (amount_customer < max_capacity)
    @Query("SELECT td FROM TourDetail td JOIN td.tour t " +
           "WHERE td.status = 'AVAILABLE' " +
           "AND t.status = 'ACTIVE' " + // Đảm bảo Tour gốc cũng đang hoạt động
           "AND td.timeStart > :currentTime " +
           "AND td.amountCustomer < td.maxCapacity " +
           "ORDER BY td.timeStart ASC")
    List<TourDetail> findAvailableTourDetails(@Param("currentTime") LocalDateTime currentTime);

    // (Tùy chọn) Thêm các phương thức tìm kiếm khác nếu cần
    // Ví dụ: Tìm theo tên tour chứa từ khóa
    @Query("SELECT td FROM TourDetail td JOIN td.tour t " +
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

     // Tìm các TourDetail theo tourId và còn chỗ
     @Query("SELECT td FROM TourDetail td " +
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
