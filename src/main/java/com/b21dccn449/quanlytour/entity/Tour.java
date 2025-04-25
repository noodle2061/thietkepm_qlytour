package com.b21dccn449.quanlytour.entity; // <-- Đã cập nhật package

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_price", precision = 38, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 255)
    private String category;

    @Column(length = 255)
    private String departure;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "min_participants")
    private Integer minParticipants;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String status;

    // Relationship: One Tour can have many TourDetails
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourDetail> tourDetails;
}
