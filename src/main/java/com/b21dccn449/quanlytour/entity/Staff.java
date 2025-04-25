package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "staff")
@DiscriminatorValue("STAFF")
@PrimaryKeyJoinColumn(name = "user_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Staff extends User {

    @Column(name = "staff_code", unique = true, nullable = false, length = 50)
    private String staffCode;

    @Column(precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(length = 100)
    private String position;

    @Column(name = "hire_date")
    private LocalDate hireDate;


    @OneToMany(mappedBy = "salesperson", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Order> soldOrders;

}
