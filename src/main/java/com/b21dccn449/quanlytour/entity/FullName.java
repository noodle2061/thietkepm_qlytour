package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
// Removed Lombok imports
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;
import java.util.Objects; // Import for equals/hashCode

/**
 * Represents the 'full_names' table.
 * Stores first and last names, associated with a User.
 * Getters and setters are manually implemented.
 */
@Entity
@Table(name = "full_names")
// Removed Lombok annotations @Data, @NoArgsConstructor, @AllArgsConstructor
public class FullName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName; // Tên

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName; // Họ

    // --- Constructors ---
    public FullName() {
    }

    public FullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    // No setter for ID

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // --- equals(), hashCode(), toString() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullName fullName)) return false;
        return Objects.equals(id, fullName.id) && Objects.equals(firstName, fullName.firstName) && Objects.equals(lastName, fullName.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }

    @Override
    public String toString() {
        return "FullName{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
