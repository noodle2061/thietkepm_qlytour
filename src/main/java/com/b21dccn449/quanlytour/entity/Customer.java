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


@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public Customer() {
        super();
    }

    public Customer(List<Order> orders, List<Ticket> tickets) {
        super();
        this.orders = orders;
        this.tickets = tickets;
    }

     public Customer(Address address, FullName fullName, Integer age, String email, Boolean isActive, String password, java.time.LocalDateTime registrationDate, String tel, String username) {
        super(address, fullName, age, email, isActive, password, registrationDate, tel, username);
    }

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Chỉ cần gọi super.hashCode
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Customer{} " + super.toString();
    }
}
