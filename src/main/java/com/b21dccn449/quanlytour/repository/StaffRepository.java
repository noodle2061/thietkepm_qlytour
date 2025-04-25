package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    // Spring Data JPA tự tạo các phương thức CRUD cơ bản

    // Tìm Staff theo username (kế thừa từ User)
    Optional<Staff> findByUsername(String username);

    // Tìm Staff theo staff_code
    Optional<Staff> findByStaffCode(String staffCode);
}
