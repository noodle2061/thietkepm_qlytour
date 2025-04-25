package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Spring Data JPA tự động tạo các phương thức CRUD cơ bản:
    // save(), findById(), findAll(), deleteById(), etc.

    // Phương thức này sẽ hỗ trợ phân trang khi gọi findAll()
    Page<Customer> findAll(Pageable pageable);

    // Bạn có thể thêm các phương thức truy vấn tùy chỉnh khác nếu cần
    // Ví dụ: tìm kiếm Customer theo email hoặc tên
    // Optional<Customer> findByEmail(String email);
    // Page<Customer> findByFullNameLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}