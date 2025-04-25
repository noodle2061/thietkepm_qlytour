package com.b21dccn449.quanlytour.entity;

import com.fasterxml.jackson.annotation.JsonProperty; // <-- THÊM IMPORT NÀY
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the base 'users' table using JOINED inheritance strategy.
 * Base entity for all system users (Customer, Staff).
 * Contains common user information and links to Address and FullName.
 * Getters and setters are manually implemented.
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "username")
       })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "full_name_id", referencedColumnName = "id", nullable = false)
    private FullName fullName;

    private Integer age;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(nullable = false, length = 20)
    private String tel;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // --- Constructors ---
    public User() {
    }

    public User(Address address, FullName fullName, Integer age, String email, Boolean isActive, String password, LocalDateTime registrationDate, String tel, String username) {
        this.address = address;
        this.fullName = fullName;
        this.age = age;
        this.email = email;
        this.isActive = isActive;
        this.password = password;
        this.registrationDate = registrationDate;
        this.tel = tel;
        this.username = username;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public FullName getFullName() {
        return fullName;
    }

    public void setFullName(FullName fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for the isActive field.
     * Use @JsonProperty to ensure the JSON field name is "isActive".
     * @return the active status
     */
    @JsonProperty("isActive") // <-- THÊM CHÚ THÍCH NÀY
    public Boolean isActive() {
        return isActive;
    }

    /**
     * Setter for the isActive field.
     * @param active the new active status
     */
    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // --- Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isActive == null) {
             isActive = true;
        }
    }

    // --- equals(), hashCode(), toString() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        if (id != null && user.id != null) {
            return id.equals(user.id);
        }
        return this == o;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        String fullNameStr = "null";
        if (fullName != null) {
            String lastName = fullName.getLastName() != null ? fullName.getLastName() : "";
            String firstName = fullName.getFirstName() != null ? fullName.getFirstName() : "";
            fullNameStr = lastName + " " + firstName;
        }
        return "User{" +
               "id=" + id +
               ", fullName=" + fullNameStr +
               ", age=" + age +
               ", email='" + email + '\'' +
               ", isActive=" + isActive + // Sử dụng getter ở đây
               ", registrationDate=" + registrationDate +
               ", tel='" + tel + '\'' +
               ", username='" + username + '\'' +
               '}';
    }
}
