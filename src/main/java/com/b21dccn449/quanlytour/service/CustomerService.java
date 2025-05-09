package com.b21dccn449.quanlytour.service;

import java.util.List; // Import List
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b21dccn449.quanlytour.entity.Customer;
import com.b21dccn449.quanlytour.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        // **QUAN TRỌNG**: Nên có kiểm tra username/email tồn tại và mã hóa mật khẩu ở
        // đây
        // if (customerRepository.existsByUsername(customer.getUsername())) { ... }
        // if (customerRepository.existsByEmail(customer.getEmail())) { ... }
        // customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional
    public Optional<Customer> updateCustomer(Long id, Customer customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            if (customerDetails.getPassword() != null && !customerDetails.getPassword().isBlank()) {
                existingCustomer.setPassword(customerDetails.getPassword());
            }
            if (customerDetails.getFullName() != null) {
                existingCustomer.setFullName(customerDetails.getFullName());
            }
            if (customerDetails.getTel() != null) {
                existingCustomer.setTel(customerDetails.getTel());
            }
            if (customerDetails.getEmail() != null) {
                existingCustomer.setEmail(customerDetails.getEmail());
            }
            if (customerDetails.getAddress() != null) {
                existingCustomer.setAddress(customerDetails.getAddress());
            }
            if (customerDetails.getAge() != null) {
                existingCustomer.setAge(customerDetails.getAge());
            }
            if (customerDetails.isActive() != null) {
                existingCustomer.setActive(customerDetails.isActive());
            }
            return customerRepository.save(existingCustomer);
        });
    }

    @Transactional
    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomersByNameOrPhoneOrEmail(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String likePattern = "%" + keyword.toLowerCase() + "%";
        // Gọi phương thức mới trong repository
        return customerRepository.searchByNameOrPhoneOrEmail(likePattern);
    }

    // Phương thức kiểm tra tồn tại (nếu cần gọi từ service khác)
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}
