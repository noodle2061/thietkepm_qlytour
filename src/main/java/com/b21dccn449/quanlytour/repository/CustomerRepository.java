package com.b21dccn449.quanlytour.repository;

import java.util.List; // Import List

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import com.b21dccn449.quanlytour.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findAll(Pageable pageable);


    @Query("SELECT c FROM Customer c JOIN c.fullName fn " +
           "WHERE LOWER(fn.firstName) LIKE :keyword " +
           "OR LOWER(fn.lastName) LIKE :keyword " +
           "OR LOWER(c.tel) LIKE :keyword " +
           "OR LOWER(c.email) LIKE :keyword")
    List<Customer> searchByNameOrPhoneOrEmail(@Param("keyword") String keyword);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
