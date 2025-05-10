package com.b21dccn449.quanlytour.service;

import com.b21dccn449.quanlytour.controller.BookingController.BookingException;
import com.b21dccn449.quanlytour.entity.*;
import com.b21dccn449.quanlytour.repository.*;
import com.b21dccn449.quanlytour.event.BookingSuccessEvent; // <-- IMPORT EVENT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher; // <-- IMPORT PUBLISHER
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Import PasswordEncoder if using password encryption
// import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BookingService {

    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());

    private final TourDetailRepository tourDetailRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final StaffRepository staffRepository;
    private final CustomerService customerService;
    private final ApplicationEventPublisher eventPublisher; // <-- INJECT PUBLISHER
    // Inject PasswordEncoder if needed
    // private final PasswordEncoder passwordEncoder;

    @Autowired
    public BookingService(TourDetailRepository tourDetailRepository,
                          CustomerRepository customerRepository,
                          OrderRepository orderRepository,
                          TicketRepository ticketRepository,
                          StaffRepository staffRepository,
                          CustomerService customerService,
                          ApplicationEventPublisher eventPublisher /*, PasswordEncoder passwordEncoder */) { // <-- ADD PUBLISHER TO CONSTRUCTOR
        this.tourDetailRepository = tourDetailRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.staffRepository = staffRepository;
        this.customerService = customerService;
        this.eventPublisher = eventPublisher; // <-- ASSIGN PUBLISHER
        // this.passwordEncoder = passwordEncoder;
    }

    // --- Methods for finding tours and staff (unchanged) ---
    @Transactional(readOnly = true)
    public List<TourDetail> findAvailableTourDetails(String keyword) {
        LocalDateTime currentTime = LocalDateTime.now();
        return tourDetailRepository.findAvailableTourDetailsByName(currentTime, keyword);
    }
    @Transactional(readOnly = true)
    public List<TourDetail> findAllAvailableTourDetails() {
        LocalDateTime currentTime = LocalDateTime.now();
        return tourDetailRepository.findAvailableTourDetails(currentTime);
    }
    @Transactional(readOnly = true)
    public Page<TourDetail> findAllAvailableTourDetailsPaginated(Pageable pageable) {
        LocalDateTime currentTime = LocalDateTime.now();
        return tourDetailRepository.findAvailableTourDetailsPaginated(currentTime, pageable);
    }
     @Transactional(readOnly = true)
     public Optional<TourDetail> getTourDetailById(Long id) {
         return tourDetailRepository.findById(id);
     }
     @Transactional(readOnly = true)
     public Optional<Staff> findStaffByUsername(String username) {
         return staffRepository.findByUsername(username);
     }
     // --- End of unchanged methods ---


    /**
     * Creates a new booking (Order and associated Tickets).
     * Handles both existing and new customers.
     * Publishes a {@link BookingSuccessEvent} upon successful completion.
     *
     * @param tourDetailId ID of the selected TourDetail.
     * @param customerId ID of the existing customer (null if adding new).
     * @param newCustomerData Data for the new customer (null if using existing).
     * @param numberOfTickets Number of tickets to book.
     * @param paymentMethod Selected payment method.
     * @param staffId ID of the staff member processing the booking.
     * @return The saved Order entity.
     * @throws BookingException If a business rule is violated (e.g., validation fails, tour unavailable).
     */
    @Transactional(rollbackFor = Exception.class) // Rollback on any exception
    public Order createBooking(Long tourDetailId, Long customerId, Customer newCustomerData,
                               int numberOfTickets, String paymentMethod, Long staffId) throws BookingException {

        LOGGER.log(Level.INFO, "Attempting to create booking for TourDetail ID: {0}, Customer ID: {1}, New Customer: {2}, Tickets: {3}, Staff ID: {4}",
                   new Object[]{tourDetailId, customerId, (newCustomerData != null), numberOfTickets, staffId});

        // 1. Validate Input IDs and basic data
        if (tourDetailId == null) {
            throw new BookingException("Thiếu thông tin chuyến đi (TourDetail ID is null).");
        }
        if (customerId == null && newCustomerData == null) {
            throw new BookingException("Thiếu thông tin khách hàng (Both Customer ID and New Customer Data are null).");
        }
        if (customerId != null && newCustomerData != null) {
            // This case should ideally be prevented by the controller logic, but double-check here
            throw new BookingException("Không thể vừa chọn khách hàng cũ vừa thêm khách hàng mới.");
        }
        if (numberOfTickets <= 0) {
            throw new BookingException("Số lượng vé phải lớn hơn 0.");
        }
        if (staffId == null) {
             throw new BookingException("Thiếu thông tin nhân viên xử lý (Staff ID is null).");
        }

        // 2. Fetch Staff
        Staff salesperson = staffRepository.findById(staffId)
                .orElseThrow(() -> {
                    LOGGER.log(Level.SEVERE, "Staff not found with ID: {0}", staffId);
                    return new BookingException("Không tìm thấy nhân viên xử lý với ID: " + staffId);
                });
        LOGGER.log(Level.INFO, "Processing staff found: {0}", salesperson.getUsername());

        // 3. Fetch and Validate TourDetail (within transaction for consistency)
        TourDetail tourDetail = tourDetailRepository.findById(tourDetailId)
                .orElseThrow(() -> {
                     LOGGER.log(Level.WARNING, "TourDetail not found with ID: {0}", tourDetailId);
                     return new BookingException("Không tìm thấy chuyến đi với ID: " + tourDetailId);
                });

        // Re-validate availability and status within the transaction
        if (!"AVAILABLE".equalsIgnoreCase(tourDetail.getStatus())) {
             LOGGER.log(Level.WARNING, "TourDetail ID {0} is not AVAILABLE (Status: {1})", new Object[]{tourDetailId, tourDetail.getStatus()});
             throw new BookingException("Chuyến đi này không còn ở trạng thái 'AVAILABLE'.");
        }
        if (tourDetail.getTour() == null || !"ACTIVE".equalsIgnoreCase(tourDetail.getTour().getStatus())) {
             LOGGER.log(Level.WARNING, "Associated Tour for TourDetail ID {0} is null or not ACTIVE", tourDetailId);
             throw new BookingException("Tour liên kết với chuyến đi này không hoạt động.");
        }
        if (tourDetail.getTimeStart() == null || tourDetail.getTimeStart().isBefore(LocalDateTime.now())) {
             LOGGER.log(Level.WARNING, "TourDetail ID {0} start time is in the past or null", tourDetailId);
             throw new BookingException("Chuyến đi này đã khởi hành hoặc thời gian không hợp lệ.");
        }

        // Check available seats
        int currentCustomerCount = tourDetail.getAmountCustomer() != null ? tourDetail.getAmountCustomer() : 0;
        int maxCapacity = tourDetail.getMaxCapacity() != null ? tourDetail.getMaxCapacity() : 0;
        int availableSeats = maxCapacity - currentCustomerCount;

        if (availableSeats < numberOfTickets) {
            LOGGER.log(Level.WARNING, "Not enough seats for TourDetail ID {0}. Required: {1}, Available: {2}", new Object[]{tourDetailId, numberOfTickets, availableSeats});
            throw new BookingException("Không đủ chỗ trống. Chỉ còn " + availableSeats + " chỗ.");
        }
        LOGGER.log(Level.INFO, "TourDetail ID {0} validated. Available seats: {1}", new Object[]{tourDetailId, availableSeats});


        // 4. Determine Customer (Existing or New)
        Customer customer;
        boolean isNewCustomer = (customerId == null && newCustomerData != null);

        if (isNewCustomer) {
            LOGGER.log(Level.INFO, "Processing new customer: {0}", newCustomerData.getUsername());
            // Validate essential new customer data (should align with controller validation)
             if (newCustomerData.getFullName() == null ||
                 newCustomerData.getFullName().getFirstName() == null || newCustomerData.getFullName().getFirstName().isBlank() ||
                 newCustomerData.getFullName().getLastName() == null || newCustomerData.getFullName().getLastName().isBlank() ||
                 newCustomerData.getEmail() == null || newCustomerData.getEmail().isBlank() ||
                 newCustomerData.getTel() == null || newCustomerData.getTel().isBlank() ||
                 newCustomerData.getUsername() == null || newCustomerData.getUsername().isBlank() ||
                 newCustomerData.getPassword() == null || newCustomerData.getPassword().isBlank()) {
                  LOGGER.log(Level.WARNING, "Missing required fields for new customer: {0}", newCustomerData.getUsername());
                  throw new BookingException("Vui lòng nhập đầy đủ thông tin bắt buộc (*) cho khách hàng mới.");
             }
             // Check for existing username/email using CustomerService
             if (customerService.existsByUsername(newCustomerData.getUsername())) {
                 LOGGER.log(Level.WARNING, "Username already exists: {0}", newCustomerData.getUsername());
                 throw new BookingException("Tên đăng nhập '" + newCustomerData.getUsername() + "' đã tồn tại.");
             }
             if (customerService.existsByEmail(newCustomerData.getEmail())) {
                 LOGGER.log(Level.WARNING, "Email already exists: {0}", newCustomerData.getEmail());
                 throw new BookingException("Email '" + newCustomerData.getEmail() + "' đã tồn tại.");
             }

             // IMPORTANT: Encode password before saving if using Spring Security
             // newCustomerData.setPassword(passwordEncoder.encode(newCustomerData.getPassword()));
             // LOGGER.log(Level.INFO, "Password encoded for new customer: {0}", newCustomerData.getUsername());

             // Handle potentially empty Address (set to null if both street and city are blank)
              if (newCustomerData.getAddress() != null &&
                  (newCustomerData.getAddress().getStreet() == null || newCustomerData.getAddress().getStreet().isBlank()) &&
                  (newCustomerData.getAddress().getCity() == null || newCustomerData.getAddress().getCity().isBlank())) {
                  newCustomerData.setAddress(null);
                  LOGGER.log(Level.INFO, "Address set to null for new customer {0} as it was empty.", newCustomerData.getUsername());
              }

             customer = newCustomerData; // Assign the new customer data
             customer.setActive(true); // Ensure new customers are active by default
             // Note: The new customer (and their Address/FullName if cascaded) will be saved
             // when the Order is saved due to CascadeType settings on the relationships.

        } else {
            // Fetch existing customer
            LOGGER.log(Level.INFO, "Processing existing customer ID: {0}", customerId);
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        LOGGER.log(Level.WARNING, "Existing customer not found with ID: {0}", customerId);
                        return new BookingException("Không tìm thấy khách hàng đã chọn với ID: " + customerId);
                    });
             // Check if the existing customer is active
             if (customer.isActive() == null || !customer.isActive()) {
                 LOGGER.log(Level.WARNING, "Attempted booking for inactive customer ID: {0}", customerId);
                 throw new BookingException("Khách hàng '" + (customer.getFullName() != null ? customer.getFullName().getLastName() + " " + customer.getFullName().getFirstName() : "ID "+ customerId) + "' đang không hoạt động.");
            }
             LOGGER.log(Level.INFO, "Existing customer found and active: {0}", customer.getUsername());
        }

        // 5. Create Order entity
        Order order = new Order();
        order.setCustomer(customer); // Associate Customer (either new or existing)
        order.setTourDetail(tourDetail);
        order.setSalesperson(salesperson);
        order.setOrderDate(LocalDateTime.now()); // Set order time
        order.setPaymentMethod(paymentMethod);
        // Determine payment status based on method (adjust logic if needed)
        order.setPaymentStatus("CASH".equalsIgnoreCase(paymentMethod) ? "PAID" : "PENDING");
        // Generate a unique booking reference (simple example)
        order.setBookingReference(UUID.randomUUID().toString().substring(0, 10).toUpperCase());

        // Calculate total amount
        BigDecimal pricePerTicket = tourDetail.getPrice();
        if (pricePerTicket == null) {
             LOGGER.log(Level.SEVERE, "Price is null for TourDetail ID: {0}", tourDetailId);
             throw new BookingException("Lỗi: Giá vé cho chuyến đi này chưa được thiết lập.");
        }
        BigDecimal totalAmount = pricePerTicket.multiply(new BigDecimal(numberOfTickets));
        order.setTotalAmount(totalAmount);
        LOGGER.log(Level.INFO, "Order created. Ref: {0}, Total: {1}", new Object[]{order.getBookingReference(), totalAmount});

        // 6. Create Ticket entities
        List<Ticket> tickets = new ArrayList<>();
        String primaryPassengerName = (customer.getFullName() != null)
            ? customer.getFullName().getLastName() + " " + customer.getFullName().getFirstName()
            : "Khách hàng chính";

        for (int i = 0; i < numberOfTickets; i++) {
            Ticket ticket = new Ticket();
            ticket.setOrder(order); // Link ticket to the order
            ticket.setCustomer(customer); // Link ticket to the customer
            ticket.setTourDetail(tourDetail); // Link ticket to the specific tour detail
            ticket.setPricePaid(pricePerTicket); // Set the price paid for this ticket
            ticket.setIssueDate(LocalDateTime.now()); // Set issue date
            ticket.setStatus("ISSUED"); // Default status

            // Set passenger name (use customer name for the first ticket, generic for others)
            ticket.setPassengerName((i == 0) ? primaryPassengerName : primaryPassengerName + " - Người đi cùng " + (i + 1));

            tickets.add(ticket);
        }
        order.setTickets(tickets); // Add the list of tickets to the order
        LOGGER.log(Level.INFO, "Created {0} tickets for Order Ref: {1}", new Object[]{tickets.size(), order.getBookingReference()});

        // 7. Update TourDetail capacity (IMPORTANT: do this before saving)
        tourDetail.setAmountCustomer(currentCustomerCount + numberOfTickets);
        // Update status to FULL if capacity is reached
        if (tourDetail.getAmountCustomer() >= maxCapacity) {
            tourDetail.setStatus("FULL");
             LOGGER.log(Level.INFO, "TourDetail ID {0} status updated to FULL.", tourDetailId);
        }
        // Save the updated TourDetail state immediately before saving the order
        // to minimize race conditions, though @Transactional helps manage this.
        tourDetailRepository.save(tourDetail);
        LOGGER.log(Level.INFO, "TourDetail ID {0} capacity updated. New count: {1}", new Object[]{tourDetailId, tourDetail.getAmountCustomer()});


        // 8. Save the Order (Cascade should save new Customer and Tickets)
        Order savedOrder = orderRepository.save(order);
        LOGGER.log(Level.INFO, "Order saved successfully. ID: {0}, Ref: {1}", new Object[]{savedOrder.getId(), savedOrder.getBookingReference()});


        // ---> 9. Publish the success event AFTER successful save <---
        try {
             // Pass 'this' as the source and the *saved* order
             eventPublisher.publishEvent(new BookingSuccessEvent(this, savedOrder));
             LOGGER.log(Level.INFO, "Published BookingSuccessEvent for Order ID: {0}", savedOrder.getId());
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "CRITICAL: Failed to publish BookingSuccessEvent for Order ID " + savedOrder.getId() + ". Email notification might not be sent.", e);
        }

        return savedOrder;
    }
}
