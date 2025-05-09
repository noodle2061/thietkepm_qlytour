package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
import lombok.Data; // Hoặc xóa nếu bạn không dùng Lombok và tự viết getter/setter
import lombok.NoArgsConstructor; // Hoặc xóa nếu bạn không dùng Lombok
import lombok.AllArgsConstructor; // Hoặc xóa nếu bạn không dùng Lombok
import lombok.ToString; // Hoặc xóa nếu bạn không dùng Lombok

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the 'tickets' table. Links to Customer, Order, and TourDetail.
 * Trường seatNumber đã được loại bỏ.
 */
@Entity
@Table(name = "tickets")
@Data // Giữ lại nếu bạn vẫn dùng Lombok cho các trường còn lại
@NoArgsConstructor // Giữ lại nếu bạn vẫn dùng Lombok
@AllArgsConstructor // Cập nhật constructor này nếu bạn không dùng Lombok
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "passenger_name", length = 255)
    private String passengerName; // Tên hành khách

    @Column(name = "price_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePaid; // Giá đã trả

    // --- TRƯỜNG seatNumber ĐÃ BỊ XÓA KHỎI ĐÂY ---

    @Column(nullable = false, length = 50)
    private String status; // Trạng thái vé (ISSUED, CANCELLED, USED)

    // Relationship: Many Tickets can belong to one Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude // Tránh vòng lặp vô hạn khi gọi toString()
    private Customer customer; // Khách hàng sở hữu vé

    // Relationship: Many Tickets belong to one Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude // Tránh vòng lặp vô hạn khi gọi toString()
    private Order order; // Đơn hàng chứa vé này

    // Relationship: Many Tickets can be for one specific TourDetail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_detail_id", nullable = false)
    @ToString.Exclude // Tránh vòng lặp vô hạn khi gọi toString()
    private TourDetail tourDetail; // Chi tiết chuyến đi của vé

    /**
     * Constructor không bao gồm seatNumber (nếu bạn không dùng Lombok @AllArgsConstructor).
     * Nếu dùng Lombok, bạn không cần constructor này.
     */
     // public Ticket(Long id, LocalDateTime issueDate, String passengerName, BigDecimal pricePaid, String status, Customer customer, Order order, TourDetail tourDetail) {
     //    this.id = id;
     //    this.issueDate = issueDate;
     //    this.passengerName = passengerName;
     //    this.pricePaid = pricePaid;
     //    this.status = status;
     //    this.customer = customer;
     //    this.order = order;
     //    this.tourDetail = tourDetail;
     // }


     @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
         if (status == null) {
             status = "ISSUED"; // Trạng thái mặc định khi tạo vé
         }
    }

    // Nếu bạn không dùng Lombok, hãy đảm bảo đã xóa thủ công phương thức getSeatNumber() và setSeatNumber().
}
