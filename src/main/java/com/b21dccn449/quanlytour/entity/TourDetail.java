package com.b21dccn449.quanlytour.entity; // <-- Đã cập nhật package

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the 'tour_details' table.
 * Contains specific information about a scheduled instance of a Tour.
 */
@Entity
@Table(name = "tour_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount_customer")
    private Integer amountCustomer;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "meeting_point", length = 255)
    private String meetingPoint;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 255)
    private String status;

    @Column(name = "time_finish", nullable = false)
    private LocalDateTime timeFinish;

    @Column(name = "time_start", nullable = false)
    private LocalDateTime timeStart;

    // Relationship: Many TourDetails belong to one Tour
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    // Relationship: One TourDetail can be associated with many Orders
    @OneToMany(mappedBy = "tourDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    // Relationship: One TourDetail can have many Tickets issued for it
    @OneToMany(mappedBy = "tourDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;
}
