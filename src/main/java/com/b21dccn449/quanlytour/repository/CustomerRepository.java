package com.b21dccn449.quanlytour.repository;

import com.b21dccn449.quanlytour.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.util.List; // Import List
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findAll(Pageable pageable);

    // Phương thức tìm kiếm khách hàng theo tên (họ hoặc tên), sđt hoặc email
    // Sử dụng LOWER() để tìm kiếm không phân biệt hoa thường
    @Query("SELECT c FROM Customer c JOIN c.fullName fn " +
           "WHERE LOWER(fn.firstName) LIKE :keyword " +
           "OR LOWER(fn.lastName) LIKE :keyword " +
           "OR LOWER(c.tel) LIKE :keyword " +
           "OR LOWER(c.email) LIKE :keyword")
    List<Customer> searchByNameOrPhoneOrEmail(@Param("keyword") String keyword);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
