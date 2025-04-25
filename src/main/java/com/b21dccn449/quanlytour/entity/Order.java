package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the 'orders' table. Links to Customer and potentially Staff (salesperson).
 */
@Entity
@Table(name = "orders",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "booking_reference")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", unique = true, length = 50)
    private String bookingReference;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // Relationship: Many Orders belong to one Customer
    @ManyToOne(fetch = FetchType.LAZY)
    // JoinColumn refers to the ID in the 'users' table, but the type is Customer
    @JoinColumn(name = "customer_id", nullable = false) // Name matches PDF's tblBill.CustomerId
    @ToString.Exclude
    private Customer customer; // Type is Customer

    // Relationship: Many Orders can be for one specific TourDetail (Unchanged)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_detail_id", nullable = false)
    @ToString.Exclude
    private TourDetail tourDetail;

    // Relationship: One Order can have many Tickets (Unchanged)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Ticket> tickets;

    // Relationship: Track which staff member handled the order
    @ManyToOne(fetch = FetchType.LAZY)
    // JoinColumn refers to the ID in the 'users' table, but the type is Staff
    @JoinColumn(name = "salesperson_id", nullable = true) // Nullable if order placed online
    @ToString.Exclude
    private Staff salesperson; // Type changed to Staff (polymorphism handles if it's a Manager)

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
