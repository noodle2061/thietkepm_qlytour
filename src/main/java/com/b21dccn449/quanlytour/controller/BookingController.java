package com.b21dccn449.quanlytour.controller;

import com.b21dccn449.quanlytour.entity.*; // Import Address and FullName
import com.b21dccn449.quanlytour.repository.StaffRepository;
import com.b21dccn449.quanlytour.service.BookingService;
import com.b21dccn449.quanlytour.service.CustomerService;
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

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final CustomerService customerService;
    private final StaffRepository staffRepository;

    // Constants for default pagination
    private static final int DEFAULT_TOUR_PAGE_SIZE = 5;
    private static final int DEFAULT_CUSTOMER_PAGE_SIZE = 5;

    @Autowired
    public BookingController(BookingService bookingService,
                             CustomerService customerService,
                             StaffRepository staffRepository) {
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.staffRepository = staffRepository;
    }

    // Provides a fresh BookingData object for each request if not already present
    @ModelAttribute("bookingData")
    public BookingData getBookingData() {
        BookingData data = new BookingData();
        // Initialize nested objects to prevent issues with th:field on first load
        if (data.getNewCustomer() == null) {
             data.setNewCustomer(new Customer());
        }
        if (data.getNewCustomer().getFullName() == null) {
            data.getNewCustomer().setFullName(new FullName());
        }
         if (data.getNewCustomer().getAddress() == null) { // Ensure address is also initialized
            data.getNewCustomer().setAddress(new Address());
        }
        return data;
    }

    @GetMapping("/new")
    public String showBookingForm(@ModelAttribute("bookingData") BookingData bookingData, // Receives/updates the bookingData object
                                  BindingResult bindingResult, // Keep for #fields context
                                  // Receive IDs potentially passed via redirect URL parameters
                                  @RequestParam(name = "tourDetailId", required = false) Long tourDetailIdParam,
                                  @RequestParam(name = "customerId", required = false) Long customerIdParam,
                                  // *** FIX: Remove @ModelAttribute for simple type from Flash Attribute ***
                                  Boolean addingNewCustomerFlag, // Receive flag directly from Flash Attribute
                                  // Receive potentially failed new customer data from flash attribute
                                  @ModelAttribute("failedNewCustomerData") Customer failedNewCustomerData,
                                  // Tour pagination params
                                  @RequestParam(name = "page", defaultValue = "1") int tourPageNum,
                                  @RequestParam(name = "size", defaultValue = "" + DEFAULT_TOUR_PAGE_SIZE) int tourPageSize,
                                  // Customer pagination params (only used for initial list)
                                  @RequestParam(name = "customerPage", defaultValue = "1") int customerPageNum,
                                  @RequestParam(name = "customerSize", defaultValue = "" + DEFAULT_CUSTOMER_PAGE_SIZE) int customerPageSize,
                                  Model model) {

        System.out.println("--- showBookingForm ---");
        System.out.println("URL Param - tourDetailIdParam: " + tourDetailIdParam);
        System.out.println("URL Param - customerIdParam: " + customerIdParam);
        // Check if the flag was received from Flash (can be null if not set)
        System.out.println("Flash Param - addingNewCustomerFlag: " + addingNewCustomerFlag);
        System.out.println("Flash Param - failedNewCustomerData: " + (failedNewCustomerData != null));
        System.out.println("Initial bookingData - selectedTourDetailId: " + bookingData.getSelectedTourDetailId());
        System.out.println("Initial bookingData - selectedCustomerId: " + bookingData.getSelectedCustomerId());
        System.out.println("Initial bookingData - addingNewCustomer: " + bookingData.isAddingNewCustomer());


        // --- Update bookingData based on URL parameters and Flash attributes ---
        if (tourDetailIdParam != null) {
            bookingData.setSelectedTourDetailId(tourDetailIdParam);
        }
        if (customerIdParam != null) {
            bookingData.setSelectedCustomerId(customerIdParam);
            // When a customer is selected via URL param, clear other customer states
            bookingData.setAddingNewCustomer(false);
            bookingData.setCustomerSearchKeyword(null);
            bookingData.setCustomerSearchResults(null);
        }
        // Handle the flag from flash attribute for adding new customer mode
        // Check for Boolean object being non-null and true
        if (addingNewCustomerFlag != null && addingNewCustomerFlag.booleanValue()) {
             bookingData.setAddingNewCustomer(true);
             bookingData.setSelectedCustomerId(null); // Clear selected customer if switching to add mode
             bookingData.setCustomerSearchKeyword(null);
             bookingData.setCustomerSearchResults(null);
             // Restore failed new customer data if it exists from flash
             if (failedNewCustomerData != null) {
                 bookingData.setNewCustomer(failedNewCustomerData);
                 // Ensure nested objects are not null after restore
                 if (bookingData.getNewCustomer().getFullName() == null) {
                     bookingData.getNewCustomer().setFullName(new FullName());
                 }
                 if (bookingData.getNewCustomer().getAddress() == null) {
                     bookingData.getNewCustomer().setAddress(new Address());
                 }
             } else if (bookingData.getNewCustomer() == null) { // Initialize if not restored and null
                 // Should be initialized by @ModelAttribute("bookingData") method, but double-check
                 bookingData.setNewCustomer(new Customer());
                 bookingData.getNewCustomer().setFullName(new FullName());
                 bookingData.getNewCustomer().setAddress(new Address());
             }
        }


        System.out.println("Updated bookingData - selectedTourDetailId: " + bookingData.getSelectedTourDetailId());
        System.out.println("Updated bookingData - selectedCustomerId: " + bookingData.getSelectedCustomerId());
        System.out.println("Updated bookingData - addingNewCustomer: " + bookingData.isAddingNewCustomer());


        // --- Load Tour Details Page ---
        tourPageNum = Math.max(1, tourPageNum);
        Pageable tourPageable = PageRequest.of(tourPageNum - 1, tourPageSize, Sort.by("timeStart").ascending());
        Page<TourDetail> tourDetailPage = bookingService.findAllAvailableTourDetailsPaginated(tourPageable);
        model.addAttribute("tourDetailPage", tourDetailPage);
        addPageNumbersToModel(model, "tourPageNumbers", tourDetailPage);

        // --- Load Customer Page only if tour selected AND no customer selected/search/add ---
        if (bookingData.getSelectedTourDetailId() != null &&
            bookingData.getCustomerSearchResults() == null &&
            !bookingData.isAddingNewCustomer() &&
            bookingData.getSelectedCustomerId() == null) { // Condition is now correct based on updated bookingData
             customerPageNum = Math.max(1, customerPageNum);
             Pageable customerPageable = PageRequest.of(customerPageNum - 1, customerPageSize, Sort.by("id").ascending());
             Page<Customer> customerPage = customerService.getAllCustomers(customerPageable);
             model.addAttribute("customerPage", customerPage);
             addPageNumbersToModel(model, "customerPageNumbers", customerPage);
        } else {
             model.addAttribute("customerPage", null);
             model.addAttribute("customerPageNumbers", Collections.emptyList());
        }

        // Reset tour search results if no keyword present in bookingData
        if (bookingData.getSearchKeyword() == null) {
            bookingData.setSearchResults(null);
        }

        // Ensure the latest bookingData is in the model
        model.addAttribute("bookingData", bookingData);
        return "booking-form";
    }

    @PostMapping("/search-tours")
    public String searchTourDetails(@RequestParam(name = "keyword", required = false) String keyword,
                                    @ModelAttribute("bookingData") BookingData bookingData, Model model) {
        List<TourDetail> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = bookingService.findAvailableTourDetails(keyword);
            bookingData.setSearchKeyword(keyword);
        } else {
            results = Collections.emptyList();
            bookingData.setSearchKeyword(null);
        }
        bookingData.setSearchResults(results);
        // Reset subsequent steps when searching tours
        bookingData.setSelectedTourDetailId(null);
        bookingData.setSelectedCustomerId(null);
        bookingData.setCustomerSearchKeyword(null);
        bookingData.setCustomerSearchResults(null);
        bookingData.setAddingNewCustomer(false);
        // Ensure newCustomer is reset/initialized correctly
        bookingData.setNewCustomer(new Customer());
        if (bookingData.getNewCustomer().getFullName() == null) bookingData.getNewCustomer().setFullName(new FullName());
        if (bookingData.getNewCustomer().getAddress() == null) bookingData.getNewCustomer().setAddress(new Address());


        model.addAttribute("bookingData", bookingData);
        // Don't reload tour page here, show search results or empty list
        model.addAttribute("tourDetailPage", null);
        model.addAttribute("tourPageNumbers", Collections.emptyList());
        // Don't reload customer page here either
        model.addAttribute("customerPage", null);
        model.addAttribute("customerPageNumbers", Collections.emptyList());

        return "booking-form";
    }


    @PostMapping("/select-tour")
    public String selectTourDetail(@RequestParam("tourDetailId") Long tourDetailId,
                                   @RequestParam(name="currentPage", defaultValue = "1") int currentPage,
                                   @RequestParam(name="currentSize", defaultValue = "" + DEFAULT_TOUR_PAGE_SIZE) int currentSize,
                                   @ModelAttribute("bookingData") BookingData bookingData, // Get current state if any
                                   RedirectAttributes redirectAttributes) {

        // Reset customer-related state when selecting a new tour
        bookingData.setSelectedCustomerId(null);
        bookingData.setCustomerSearchKeyword(null);
        bookingData.setCustomerSearchResults(null);
        bookingData.setAddingNewCustomer(false);
        // Ensure newCustomer is reset/initialized correctly
        bookingData.setNewCustomer(new Customer());
        if (bookingData.getNewCustomer().getFullName() == null) bookingData.getNewCustomer().setFullName(new FullName());
        if (bookingData.getNewCustomer().getAddress() == null) bookingData.getNewCustomer().setAddress(new Address());

        // Pass tour pagination info as URL parameters
        redirectAttributes.addAttribute("page", currentPage);
        redirectAttributes.addAttribute("size", currentSize);
        // Pass selected tour ID as URL parameter
        redirectAttributes.addAttribute("tourDetailId", tourDetailId);

        return "redirect:/booking/new"; // Redirect to GET /new
    }

    @PostMapping("/search-customers")
    public String searchCustomers(@RequestParam(name = "customerKeyword", required = false) String customerKeyword,
                                  @ModelAttribute("bookingData") BookingData bookingData, Model model) {
        List<Customer> customerResults;

        // Ensure a tour is selected before allowing customer search
         if (bookingData.getSelectedTourDetailId() == null) {
             model.addAttribute("errorMessage", "Vui lòng chọn chuyến đi trước khi tìm khách hàng.");
             // Reload tour data for display
             reloadTourPageData(bookingData, model, 1, DEFAULT_TOUR_PAGE_SIZE);
             model.addAttribute("customerPage", null);
             model.addAttribute("customerPageNumbers", Collections.emptyList());
             model.addAttribute("bookingData", bookingData); // Pass back the current state
             return "booking-form";
         }


        if (customerKeyword != null && !customerKeyword.trim().isEmpty()) {
            customerResults = customerService.searchCustomersByNameOrPhoneOrEmail(customerKeyword);
            bookingData.setCustomerSearchKeyword(customerKeyword);
        } else {
             // If search keyword is empty, redirect back to /new to show the initial paginated customer list
             String tourParams = "?tourDetailId=" + bookingData.getSelectedTourDetailId();
             // Add tour page/size params if needed for consistency
             // tourParams += "&page=...&size=...";
             return "redirect:/booking/new" + tourParams;
        }
        bookingData.setCustomerSearchResults(customerResults);
        // Reset selection when searching
        bookingData.setSelectedCustomerId(null);
        bookingData.setAddingNewCustomer(false);

        // Reload the currently selected/paginated tour data to keep it displayed
        reloadTourPageData(bookingData, model, 1, DEFAULT_TOUR_PAGE_SIZE); // Adjust page/size if needed

        // Don't show paginated customer list when showing search results
        model.addAttribute("customerPage", null);
        model.addAttribute("customerPageNumbers", Collections.emptyList());

        model.addAttribute("bookingData", bookingData);
        return "booking-form";
    }

     /**
      * Handles selecting an existing customer. Redirects back to /new with IDs in URL.
      */
     @PostMapping("/select-customer")
     public String selectCustomer(@RequestParam("customerId") Long customerId,
                                  @RequestParam("tourDetailId") Long tourDetailId, // Nhận trực tiếp từ request
                                  RedirectAttributes redirectAttributes) {

         // Check if a tour was selected first (sử dụng tourDetailId từ request)
         if (tourDetailId == null) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy thông tin chuyến đi đã chọn khi chọn khách hàng.");
             return "redirect:/booking/new";
         }

         // Add IDs as URL parameters for the redirect
         redirectAttributes.addAttribute("tourDetailId", tourDetailId);
         redirectAttributes.addAttribute("customerId", customerId);

         return "redirect:/booking/new";
     }

     /**
      * Handles switching to 'add new customer' mode. Redirects back to /new with tour ID.
      */
      @PostMapping("/add-new-customer-mode")
      public String showAddNewCustomerForm(@ModelAttribute("bookingData") BookingData bookingData,
                                           RedirectAttributes redirectAttributes) {

           Long currentTourDetailId = bookingData.getSelectedTourDetailId();
           if (currentTourDetailId == null) {
              redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn chuyến đi trước khi thêm khách hàng.");
              return "redirect:/booking/new";
           }

          // Add tour ID as URL parameter
          redirectAttributes.addAttribute("tourDetailId", currentTourDetailId);
          // Use flash attribute for the flag as it's temporary state info
          redirectAttributes.addFlashAttribute("addingNewCustomerFlag", true); // Pass Boolean object

          return "redirect:/booking/new";
      }

     @PostMapping("/process")
     public String processBooking(@ModelAttribute("bookingData") @Valid BookingData bookingData,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes, Model model) {

         Long staffId = getCurrentStaffId(); // Lấy ID nhân viên thực hiện
         if (staffId == null) {
              model.addAttribute("errorMessage", "Lỗi hệ thống: Không tìm thấy nhân viên thực hiện mặc định (NV001).");
              reloadBookingDataOnError(bookingData, model);
              return "booking-form";
         }

         // --- Basic Validation ---
         if (bookingData.getNumberOfTickets() == null || bookingData.getNumberOfTickets() <= 0) {
             bindingResult.rejectValue("numberOfTickets", "error.bookingData", "Số lượng vé phải lớn hơn 0.");
         }
         if (bookingData.getPaymentMethod() == null || bookingData.getPaymentMethod().isBlank()) {
              bindingResult.rejectValue("paymentMethod", "error.bookingData", "Vui lòng chọn phương thức thanh toán.");
         }
         if (bookingData.getSelectedTourDetailId() == null) {
              bindingResult.reject("error.bookingData.noTour", "Vui lòng chọn một chuyến đi.");
         }
         if (bookingData.getSelectedCustomerId() == null && !bookingData.isAddingNewCustomer()) {
             bindingResult.reject("error.bookingData.noCustomer", "Vui lòng chọn hoặc thêm mới khách hàng.");
         }

         // --- Validate New Customer ---
         if (bookingData.isAddingNewCustomer()) {
             if (bookingData.getNewCustomer() == null) {
                 bindingResult.reject("error.newCustomer.null", "Thông tin khách hàng mới không hợp lệ.");
             } else {
                  // Ensure nested objects are not null before validation
                  if (bookingData.getNewCustomer().getFullName() == null) {
                      bookingData.getNewCustomer().setFullName(new FullName()); // Initialize if null
                      bindingResult.rejectValue("newCustomer.fullName", "error.newCustomer.fullName.required", "Họ và tên là bắt buộc.");
                  } else {
                      if (bookingData.getNewCustomer().getFullName().getLastName() == null || bookingData.getNewCustomer().getFullName().getLastName().isBlank()) {
                         bindingResult.rejectValue("newCustomer.fullName.lastName", "error.newCustomer.lastName", "Họ là bắt buộc.");
                      }
                      if (bookingData.getNewCustomer().getFullName().getFirstName() == null || bookingData.getNewCustomer().getFullName().getFirstName().isBlank()) {
                         bindingResult.rejectValue("newCustomer.fullName.firstName", "error.newCustomer.firstName", "Tên là bắt buộc.");
                      }
                  }
                   if (bookingData.getNewCustomer().getEmail() == null || bookingData.getNewCustomer().getEmail().isBlank()) {
                     bindingResult.rejectValue("newCustomer.email", "error.newCustomer.email", "Email là bắt buộc.");
                   }
                   if (bookingData.getNewCustomer().getTel() == null || bookingData.getNewCustomer().getTel().isBlank()) {
                      bindingResult.rejectValue("newCustomer.tel", "error.newCustomer.tel", "Điện thoại là bắt buộc.");
                   }
                   if (bookingData.getNewCustomer().getUsername() == null || bookingData.getNewCustomer().getUsername().isBlank()) {
                      bindingResult.rejectValue("newCustomer.username", "error.newCustomer.username", "Tên đăng nhập là bắt buộc.");
                   }
                   if (bookingData.getNewCustomer().getPassword() == null || bookingData.getNewCustomer().getPassword().isBlank()) {
                      bindingResult.rejectValue("newCustomer.password", "error.newCustomer.password", "Mật khẩu là bắt buộc.");
                   }
                  // Initialize address if null before accessing fields (though less likely needed here)
                  if (bookingData.getNewCustomer().getAddress() == null) {
                     bookingData.getNewCustomer().setAddress(new Address());
                  }
             }
         }

         // --- Return to form if validation errors ---
          if (bindingResult.hasErrors()) {
              System.out.println("Validation Errors found:");
              bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));

              model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin đã nhập.");
              reloadBookingDataOnError(bookingData, model);
              return "booking-form";
          }

         // --- Proceed with booking creation ---
         try {
             bookingService.createBooking(
                 bookingData.getSelectedTourDetailId(),
                 bookingData.getSelectedCustomerId(),
                 bookingData.isAddingNewCustomer() ? bookingData.getNewCustomer() : null,
                 bookingData.getNumberOfTickets(),
                 bookingData.getPaymentMethod(),
                 staffId
             );
             redirectAttributes.addFlashAttribute("successMessage", "Đặt vé thành công!");
             return "redirect:/booking/new";

         } catch (BookingException e) {
             System.err.println("Booking Error: " + e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt vé: " + e.getMessage());
             String redirectParams = buildRedirectParamsOnError(bookingData);
             if (bookingData.isAddingNewCustomer()) {
                 redirectAttributes.addFlashAttribute("failedNewCustomerData", bookingData.getNewCustomer());
                 redirectAttributes.addFlashAttribute("addingNewCustomerFlag", true);
             }
             return "redirect:/booking/new" + redirectParams;

         } catch (Exception e) {
             System.err.println("Lỗi không xác định khi đặt vé: " + e.getMessage());
             e.printStackTrace();
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống không mong muốn.");
             String redirectParams = buildRedirectParamsOnError(bookingData);
             if (bookingData.isAddingNewCustomer()) {
                 redirectAttributes.addFlashAttribute("failedNewCustomerData", bookingData.getNewCustomer());
                 redirectAttributes.addFlashAttribute("addingNewCustomerFlag", true);
             }
             return "redirect:/booking/new" + redirectParams;
         }
     }

    // --- Helper Methods ---

    /**
     * Gets the ID of the staff member performing the booking.
     * Finds staff by staffCode 'NV001'.
     * @return The ID of the staff member with code 'NV001', or null if not found.
     */
    private Long getCurrentStaffId() {
        Optional<Staff> staffOpt = staffRepository.findByStaffCode("NV001");
        if (staffOpt.isPresent()) {
            System.out.println("INFO: Found staff NV001 with ID: " + staffOpt.get().getId());
            return staffOpt.get().getId();
        } else {
            System.err.println("ERROR: Default Staff with code 'NV001' not found in database!");
            return null;
        }
    }

     // Helper to reload Tour data (paginated or search results)
     private void reloadTourPageData(BookingData bookingData, Model model, int defaultPage, int defaultSize) {
         // Display selected tour info if available
         if (bookingData.getSelectedTourDetailId() != null) {
             bookingService.getTourDetailById(bookingData.getSelectedTourDetailId())
                 .ifPresent(td -> {
                     // model.addAttribute("explicitlySelectedTourDetail", td); // Example
                 });
         }

         // Show tour search results if they exist
         if (bookingData.getSearchResults() != null) {
             model.addAttribute("tourDetailPage", null);
             model.addAttribute("tourPageNumbers", Collections.emptyList());
         }
         // Otherwise, show the paginated list
         else {
             int page = defaultPage;
             int size = defaultSize;
             Pageable pageable = PageRequest.of(page - 1, size, Sort.by("timeStart").ascending());
             Page<TourDetail> tourDetailPage = bookingService.findAllAvailableTourDetailsPaginated(pageable);
             model.addAttribute("tourDetailPage", tourDetailPage);
             addPageNumbersToModel(model, "tourPageNumbers", tourDetailPage);
         }
     }

      // Helper to reload necessary data when validation fails before returning to the form view
      private void reloadBookingDataOnError(BookingData bookingData, Model model) {
           // 1. Reload Tour Data
           reloadTourPageData(bookingData, model, 1, DEFAULT_TOUR_PAGE_SIZE);

           // 2. Reload Customer Data (State depends on what the user was doing)
           if (bookingData.isAddingNewCustomer()) {
               // Keep the add new customer form populated (newCustomer is already in bookingData)
               model.addAttribute("customerPage", null);
               model.addAttribute("customerPageNumbers", Collections.emptyList());
           } else if (bookingData.getSelectedCustomerId() != null) {
               // Keep existing customer selected
               customerService.getCustomerById(bookingData.getSelectedCustomerId()).ifPresent(cust -> {
                   // model.addAttribute("explicitlySelectedCustomer", cust);
               });
               model.addAttribute("customerPage", null);
               model.addAttribute("customerPageNumbers", Collections.emptyList());
           } else if (bookingData.getCustomerSearchResults() != null) {
               // Keep customer search results displayed
               model.addAttribute("customerPage", null);
               model.addAttribute("customerPageNumbers", Collections.emptyList());
           } else if (bookingData.getSelectedTourDetailId() != null) {
               // If only tour was selected, show paginated customer list
               int customerPageNum = 1;
               Pageable customerPageable = PageRequest.of(customerPageNum - 1, DEFAULT_CUSTOMER_PAGE_SIZE, Sort.by("id").ascending());
               Page<Customer> customerPage = customerService.getAllCustomers(customerPageable);
               model.addAttribute("customerPage", customerPage);
               addPageNumbersToModel(model, "customerPageNumbers", customerPage);
           } else {
                // Default case
                model.addAttribute("customerPage", null);
                model.addAttribute("customerPageNumbers", Collections.emptyList());
           }

           // 3. Add the bookingData object (with errors) back to the model
           model.addAttribute("bookingData", bookingData);
      }

     // Helper to build URL parameters for redirecting on error (to preserve state)
     private String buildRedirectParamsOnError(BookingData bookingData) {
         StringBuilder params = new StringBuilder("?");
         boolean hasParam = false;
         if (bookingData.getSelectedTourDetailId() != null) {
             params.append("tourDetailId=").append(bookingData.getSelectedTourDetailId());
             hasParam = true;
         }
         if (bookingData.getSelectedCustomerId() != null) {
             if (hasParam) params.append("&");
             params.append("customerId=").append(bookingData.getSelectedCustomerId());
             hasParam = true;
         }
         // Add other relevant parameters like page numbers if needed

         return hasParam ? params.toString() : "";
     }


    private void addPageNumbersToModel(Model model, String attributeName, Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());
            model.addAttribute(attributeName, pageNumbers);
        } else {
            model.addAttribute(attributeName, Collections.emptyList());
        }
    }


    // Inner class to hold form data
    public static class BookingData {
        // Tour Search & Selection
        private String searchKeyword;
        private List<TourDetail> searchResults;
        private Long selectedTourDetailId;

        // Customer Search & Selection / Creation
        private String customerSearchKeyword;
        private List<Customer> customerSearchResults;
        private Long selectedCustomerId;
        private boolean addingNewCustomer = false;

        @Valid // Enable validation on the nested newCustomer object
        private Customer newCustomer = new Customer();

        // Booking Details
        private Integer numberOfTickets = 1;
        private String paymentMethod = "CASH";

        // --- Getters and Setters ---
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
        public List<TourDetail> getSearchResults() { return searchResults; }
        public void setSearchResults(List<TourDetail> searchResults) { this.searchResults = searchResults; }
        public Long getSelectedTourDetailId() { return selectedTourDetailId; }
        public void setSelectedTourDetailId(Long selectedTourDetailId) { this.selectedTourDetailId = selectedTourDetailId; }
        public String getCustomerSearchKeyword() { return customerSearchKeyword; }
        public void setCustomerSearchKeyword(String customerSearchKeyword) { this.customerSearchKeyword = customerSearchKeyword; }
        public List<Customer> getCustomerSearchResults() { return customerSearchResults; }
        public void setCustomerSearchResults(List<Customer> customerSearchResults) { this.customerSearchResults = customerSearchResults; }
        public Long getSelectedCustomerId() { return selectedCustomerId; }
        public void setSelectedCustomerId(Long selectedCustomerId) { this.selectedCustomerId = selectedCustomerId; }
        public boolean isAddingNewCustomer() { return addingNewCustomer; }
        public void setAddingNewCustomer(boolean addingNewCustomer) { this.addingNewCustomer = addingNewCustomer; }
        public Customer getNewCustomer() { return newCustomer; }
        public void setNewCustomer(Customer newCustomer) { this.newCustomer = newCustomer; }
        public Integer getNumberOfTickets() { return numberOfTickets; }
        public void setNumberOfTickets(Integer numberOfTickets) { this.numberOfTickets = numberOfTickets; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

     // Custom exception for booking errors
     public static class BookingException extends RuntimeException {
         public BookingException(String message) {
             super(message);
         }
     }
}
