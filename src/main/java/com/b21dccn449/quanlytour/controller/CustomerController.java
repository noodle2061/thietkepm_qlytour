package com.b21dccn449.quanlytour.controller;

import com.b21dccn449.quanlytour.entity.Address;
import com.b21dccn449.quanlytour.entity.Customer;
import com.b21dccn449.quanlytour.entity.FullName;
import com.b21dccn449.quanlytour.service.CustomerService;
import com.b21dccn449.quanlytour.validation.OnCreate;
import com.b21dccn449.quanlytour.validation.OnUpdate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final Validator validator;

    @Autowired
    public CustomerController(CustomerService customerService, Validator validator) {
        this.customerService = customerService;
        this.validator = validator;
    }


    @GetMapping
    public String listCustomers(Model model,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Customer> customerPage = customerService.getAllCustomers(pageable);

        model.addAttribute("customerPage", customerPage);

        int totalPages = customerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "customers";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        Customer customer = new Customer();
        // Khởi tạo các đối tượng lồng nhau để form không bị lỗi khi truy cập lần đầu
        customer.setFullName(new FullName());
        customer.setAddress(new Address());
        customer.setActive(true); // Giá trị mặc định
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Thêm Khách hàng Mới");
        return "customer-form";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOptional = customerService.getCustomerById(id);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            if (customer.getFullName() == null) {
                customer.setFullName(new FullName());
            }
            if (customer.getAddress() == null) {
                customer.setAddress(new Address());
            }
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Chỉnh sửa Khách hàng (ID: " + id + ")");
            return "customer-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với ID: " + id);
            return "redirect:/customers";
        }
    }


    @PostMapping("/save")
    public String saveOrUpdateCustomer(@ModelAttribute("customer") Customer customer,
                                       BindingResult bindingResult, // BindingResult phải ngay sau @ModelAttribute
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        Set<ConstraintViolation<Customer>> violations;
        if (customer.getId() == null) { // Trường hợp tạo mới
            violations = validator.validate(customer, OnCreate.class, Default.class);
        } else { // Trường hợp cập nhật
            violations = validator.validate(customer, OnUpdate.class, Default.class);
        }

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Customer> violation : violations) {
                String propertyPath = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                bindingResult.rejectValue(propertyPath, "error." + propertyPath, message);
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", customer.getId() == null ? "Thêm Khách hàng Mới (Có lỗi)" : "Chỉnh sửa Khách hàng (ID: " + customer.getId() + ") (Có lỗi)");
            if (customer.getFullName() == null) customer.setFullName(new FullName());
            if (customer.getAddress() == null) customer.setAddress(new Address());
            return "customer-form";
        }

        try {
            if (customer.getAddress() != null &&
                    (customer.getAddress().getStreet() == null || customer.getAddress().getStreet().isBlank()) &&
                    (customer.getAddress().getCity() == null || customer.getAddress().getCity().isBlank())) {
                customer.setAddress(null);
            }

            if (customer.getId() == null) {
                customerService.createCustomer(customer);
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm khách hàng thành công!");
            } else {
                Optional<Customer> updated = customerService.updateCustomer(customer.getId(), customer);
                if (updated.isPresent()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật khách hàng thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng để cập nhật (ID: " + customer.getId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving customer: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi lưu khách hàng: " + e.getMessage());
            return "customer-form";
        }
        return "redirect:/customers";
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = customerService.deleteCustomer(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khách hàng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng để xóa (ID: " + id + ")");
            }
        } catch (Exception e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa khách hàng: " + e.getMessage());
        }
        return "redirect:/customers";
    }
}
