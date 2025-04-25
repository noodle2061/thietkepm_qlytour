package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the 'tickets' table. Links to Customer, Order, and TourDetail.
 */
@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "passenger_name", length = 255)
    private String passengerName; // Could potentially link to a specific User/Customer if needed

    @Column(name = "price_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "seat_number", length = 50)
    private String seatNumber;

    @Column(nullable = false, length = 50)
    private String status;

    // Relationship: Many Tickets can belong to one Customer
    @ManyToOne(fetch = FetchType.LAZY)
    // JoinColumn refers to the ID in the 'users' table, but the type is Customer
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private Customer customer; // Type changed to Customer

    // Relationship: Many Tickets belong to one Order (Unchanged)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    // Relationship: Many Tickets can be for one specific TourDetail (Unchanged)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_detail_id", nullable = false)
    @ToString.Exclude
    private TourDetail tourDetail;

     @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
         if (status == null) {
             status = "ISSUED"; // Default status
         }
    }
}
