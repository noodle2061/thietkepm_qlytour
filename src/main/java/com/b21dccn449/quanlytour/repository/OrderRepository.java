package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Spring Data JPA tự tạo các phương thức CRUD cơ bản
    // Có thể thêm các phương thức truy vấn tùy chỉnh nếu cần
}
