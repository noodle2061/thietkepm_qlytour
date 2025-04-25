package com.b21dccn449.quanlytour.entity;

import jakarta.persistence.*;
// Thêm các import cần thiết
import com.b21dccn449.quanlytour.entity.User; // Import lớp cha User
import com.b21dccn449.quanlytour.entity.Order; // Import lớp Order
import com.b21dccn449.quanlytour.entity.Ticket; // Import lớp Ticket
import com.b21dccn449.quanlytour.entity.Address; // Import Address (cho constructor)
import com.b21dccn449.quanlytour.entity.FullName; // Import FullName (cho constructor)
import java.util.List; // Import List
import java.util.Objects; // Import for equals/hashCode

/**
 * Represents the 'customers' table, inheriting from User.
 * Removed rewardPoint and customerRanking fields.
 */
@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {

    // --- ĐÃ XÓA CÁC TRƯỜNG rewardPoint và customerRanking ---
    // @Column(name = "reward_point")
    // private Integer rewardPoint = 0;
    // @Column(name = "customer_ranking", length = 50)
    // private String customerRanking;

    // Relationship: One Customer can have many Orders
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    // Relationship: One Customer can have many Tickets
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    // --- Constructors ---
    public Customer() {
        super();
    }

    // Constructor chỉ với các trường của Customer (chỉ còn collections)
    public Customer(List<Order> orders, List<Ticket> tickets) {
        super();
        this.orders = orders;
        this.tickets = tickets;
    }

     // Constructor bao gồm các trường từ User
     public Customer(Address address, FullName fullName, Integer age, String email, Boolean isActive, String password, java.time.LocalDateTime registrationDate, String tel, String username) {
        super(address, fullName, age, email, isActive, password, registrationDate, tel, username);
        // Không còn gán rewardPoint và customerRanking
    }


    // --- Getters and Setters ---

    // --- ĐÃ XÓA Getters/Setters cho rewardPoint và customerRanking ---
    // public Integer getRewardPoint() { ... }
    // public void setRewardPoint(Integer rewardPoint) { ... }
    // public String getCustomerRanking() { ... }
    // public void setCustomerRanking(String customerRanking) { ... }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    // --- equals(), hashCode(), toString() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        // Chỉ cần gọi super.equals vì không còn trường riêng của Customer để so sánh
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Chỉ cần gọi super.hashCode
        return super.hashCode();
    }

    @Override
    public String toString() {
        // Chỉ hiển thị thông tin từ User
        return "Customer{} " + super.toString();
    }
}
