package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// Removed unused import lombok.ToString;
// Removed unused import java.util.List;

@Entity
@Table(name = "managers")
@DiscriminatorValue("MANAGER")
@PrimaryKeyJoinColumn(name = "user_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Manager extends Staff {

    @Column(name = "manager_code", unique = true, nullable = false, length = 50)
    private String managerCode;

    @Column(length = 100)
    private String title;

}
