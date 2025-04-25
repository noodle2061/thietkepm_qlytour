package com.b21dccn449.quanlytour.controller;

import com.b21dccn449.quanlytour.entity.Address; // Import Address
import com.b21dccn449.quanlytour.entity.Customer;
import com.b21dccn449.quanlytour.entity.FullName; // Import FullName
import com.b21dccn449.quanlytour.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.stereotype.Controller; // Thay đổi thành @Controller
import org.springframework.ui.Model; // Import Model
import org.springframework.web.bind.annotation.*; // Giữ lại các annotation cần thiết
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import RedirectAttributes

import java.util.List; // Import List
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors
import java.util.stream.IntStream; // Import IntStream

@Controller // Đổi từ @RestController sang @Controller
@RequestMapping("/customers") // Đổi đường dẫn gốc (ví dụ: không còn /api/v1)
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Hiển thị danh sách khách hàng với phân trang.
     * @param model Model để truyền dữ liệu tới view.
     * @param page Số trang hiện tại (mặc định là 1).
     * @param size Số lượng mục trên mỗi trang (mặc định là 10).
     * @return Tên của template Thymeleaf ("customers").
     */
    @GetMapping
    public String listCustomers(Model model,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size) {
        // Pageable trong Spring Data JPA bắt đầu từ 0, nên trừ đi 1
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Customer> customerPage = customerService.getAllCustomers(pageable);

        model.addAttribute("customerPage", customerPage);

        // Tạo danh sách số trang để hiển thị trong phân trang (nếu cần)
        int totalPages = customerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "customers"; // Trả về tên file template (customers.html)
    }

    /**
     * Hiển thị form để thêm khách hàng mới.
     * @param model Model để truyền đối tượng Customer rỗng tới view.
     * @return Tên của template Thymeleaf ("customer-form").
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Customer customer = new Customer();
        // Khởi tạo các đối tượng lồng nhau để tránh NullPointerException trong form
        customer.setFullName(new FullName());
        customer.setAddress(new Address());
        customer.setActive(true); // Mặc định là active
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Thêm Khách hàng Mới"); // Thêm tiêu đề trang
        return "customer-form"; // Trả về tên file template (customer-form.html)
    }

    /**
     * Hiển thị form để chỉnh sửa khách hàng đã có.
     * @param id ID của khách hàng cần chỉnh sửa.
     * @param model Model để truyền đối tượng Customer tới view.
     * @param redirectAttributes Dùng để gửi thông báo lỗi nếu không tìm thấy.
     * @return Tên của template Thymeleaf ("customer-form") hoặc redirect về danh sách nếu lỗi.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOptional = customerService.getCustomerById(id);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            // Đảm bảo các đối tượng lồng nhau không null trước khi đưa vào form
             if (customer.getFullName() == null) {
                 customer.setFullName(new FullName());
             }
             if (customer.getAddress() == null) {
                 customer.setAddress(new Address());
             }
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Chỉnh sửa Khách hàng (ID: " + id + ")"); // Thêm tiêu đề trang
            return "customer-form"; // Trả về tên file template (customer-form.html)
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với ID: " + id);
            return "redirect:/customers"; // Quay về trang danh sách
        }
    }

    /**
     * Xử lý việc lưu (thêm mới hoặc cập nhật) khách hàng.
     * @param customer Đối tượng Customer được binding từ form.
     * @param redirectAttributes Dùng để gửi thông báo thành công/lỗi.
     * @return Redirect về trang danh sách khách hàng.
     */
    @PostMapping("/save")
    public String saveOrUpdateCustomer(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem Address hoặc FullName có rỗng hoàn toàn không
             if (customer.getAddress() != null &&
                 (customer.getAddress().getStreet() == null || customer.getAddress().getStreet().isBlank()) &&
                 (customer.getAddress().getCity() == null || customer.getAddress().getCity().isBlank())) {
                 customer.setAddress(null); // Nếu rỗng thì đặt là null để tránh lưu bản ghi Address trống
             }
             // Không cần kiểm tra FullName vì các trường là bắt buộc

            if (customer.getId() == null) {
                // Tạo mới
                customerService.createCustomer(customer);
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm khách hàng thành công!");
            } else {
                // Cập nhật
                // Cần đảm bảo service update xử lý đúng ID của Address và FullName nếu chúng đã tồn tại
                Optional<Customer> updated = customerService.updateCustomer(customer.getId(), customer);
                 if (updated.isPresent()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật khách hàng thành công!");
                 } else {
                     redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng để cập nhật (ID: " + customer.getId() + ")");
                 }
            }
        } catch (Exception e) {
            // Log lỗi ở đây nếu cần
            System.err.println("Error saving customer: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu khách hàng: " + e.getMessage());
        }
        return "redirect:/customers"; // Quay về trang danh sách
    }

    /**
     * Xử lý việc xóa khách hàng.
     * @param id ID của khách hàng cần xóa.
     * @param redirectAttributes Dùng để gửi thông báo thành công/lỗi.
     * @return Redirect về trang danh sách khách hàng.
     */
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
            // Log lỗi ở đây nếu cần (ví dụ: lỗi khóa ngoại nếu khách hàng có đơn hàng)
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa khách hàng: " + e.getMessage());
        }
        return "redirect:/customers"; // Quay về trang danh sách
    }
}
