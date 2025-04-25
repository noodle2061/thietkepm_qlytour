package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
// Removed Lombok imports
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;
import java.util.Objects; // Import for equals/hashCode

/**
 * Represents the 'addresses' table.
 * Stores address details, associated with a User.
 * Getters and setters are manually implemented.
 */
@Entity
@Table(name = "addresses")
// Removed Lombok annotations @Data, @NoArgsConstructor, @AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String street; // Số nhà, tên đường

    @Column(length = 100)
    private String city; // Thành phố/Tỉnh

    // --- Constructors ---
    public Address() {
    }

    public Address(String street, String city) {
        this.street = street;
        this.city = city;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    // No setter for ID

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // --- equals(), hashCode(), toString() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(id, address.id) && Objects.equals(street, address.street) && Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, street, city);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
