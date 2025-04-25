package com.b21dccn449.quanlytour.service;

import com.b21dccn449.quanlytour.entity.Customer;
import com.b21dccn449.quanlytour.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for managing Customer entities.
 * Provides CRUD operations and pagination.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Logic validation nếu cần
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
            // Cập nhật các trường từ User
            existingCustomer.setFullName(customerDetails.getFullName());
            existingCustomer.setAddress(customerDetails.getAddress());
            existingCustomer.setEmail(customerDetails.getEmail());
            existingCustomer.setTel(customerDetails.getTel());
            existingCustomer.setAge(customerDetails.getAge());
            existingCustomer.setUsername(customerDetails.getUsername());
            existingCustomer.setActive(customerDetails.isActive()); // Sử dụng getter/setter

            // Xử lý mật khẩu: chỉ cập nhật nếu có mật khẩu mới được cung cấp
            if (customerDetails.getPassword() != null && !customerDetails.getPassword().isBlank()) {
                 // Nên có logic mã hóa mật khẩu ở đây trước khi set
                 existingCustomer.setPassword(customerDetails.getPassword());
            }

            // --- ĐÃ XÓA CẬP NHẬT CHO rewardPoint và customerRanking ---
            // existingCustomer.setRewardPoint(customerDetails.getRewardPoint());
            // existingCustomer.setCustomerRanking(customerDetails.getCustomerRanking());

            // Xử lý cascade nếu cần
             if (existingCustomer.getAddress() != null && existingCustomer.getAddress().getId() == null) {
                 // Logic để lưu Address mới nếu cần
             }
             if (existingCustomer.getFullName() != null && existingCustomer.getFullName().getId() == null) {
                 // Logic để lưu FullName mới nếu cần
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
}
