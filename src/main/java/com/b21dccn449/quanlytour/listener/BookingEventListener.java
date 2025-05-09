package com.b21dccn449.quanlytour.listener; // New package

import com.b21dccn449.quanlytour.entity.Order;
import com.b21dccn449.quanlytour.entity.Ticket;
import com.b21dccn449.quanlytour.event.BookingSuccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
// import org.springframework.scheduling.annotation.Async; // Uncomment for asynchronous execution (requires @EnableAsync)
// import org.springframework.transaction.event.TransactionPhase; // Uncomment for transactional event listener
// import org.springframework.transaction.event.TransactionalEventListener; // Uncomment for transactional event listener

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for {@link BookingSuccessEvent} and sends a confirmation email
 * to the customer.
 */
@Component
public class BookingEventListener {

    private static final Logger LOGGER = Logger.getLogger(BookingEventListener.class.getName());
    private final JavaMailSender mailSender;

    @Autowired
    public BookingEventListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Handles the {@link BookingSuccessEvent} by sending a confirmation email.
     * This method is triggered automatically by Spring when the event is published.
     *
     * Consider making this asynchronous (@Async) or transactional (@TransactionalEventListener)
     * based on requirements.
     *
     * @param event The {@link BookingSuccessEvent} containing the successful order details.
     */
    // @Async // Uncomment for asynchronous execution
    @EventListener
    // @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // Uncomment to run only after successful transaction commit
    public void handleBookingSuccess(BookingSuccessEvent event) {
        Order order = event.getOrder();
        LOGGER.log(Level.INFO, "Received BookingSuccessEvent for Order ID: {0}", order.getId());

        // Validate necessary information before attempting to send email
        if (order.getCustomer() == null || order.getCustomer().getEmail() == null || order.getCustomer().getEmail().isBlank()) {
            LOGGER.log(Level.SEVERE, "Cannot send email. Customer or email is missing for Order ID: {0}", order.getId());
            return; // Stop processing if email address is missing
        }
        if (order.getTourDetail() == null || order.getTourDetail().getTour() == null) {
             LOGGER.log(Level.SEVERE, "Cannot send complete email. Tour details are missing for Order ID: {0}", order.getId());
             // Decide if you still want to send a partial email or stop
             // return;
        }


        String recipientEmail = order.getCustomer().getEmail();
        String subject = "Xác nhận đặt tour thành công - Mã đặt vé: " + order.getBookingReference();

        try {
            // Build the HTML content for the email
            String htmlContent = buildBookingConfirmationEmail(order);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // Allows embedding images if needed later
                    StandardCharsets.UTF_8.name() // Ensure correct encoding for Vietnamese characters
            );

            // Set email details
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set to true to indicate HTML content
            // helper.setFrom("noreply@yourcompany.com"); // Optionally set a 'From' address if configured

            // Send the email
            mailSender.send(message);
            LOGGER.log(Level.INFO, "Booking confirmation email sent successfully to: {0}", recipientEmail);

        } catch (MessagingException e) {
            // Log specific mail sending errors
            LOGGER.log(Level.SEVERE, "Failed to send booking confirmation email to " + recipientEmail + ": " + e.getMessage(), e);
            // Consider adding retry logic or notifying an admin here
        } catch (Exception e) {
            // Catch any other unexpected errors during email preparation or sending
            LOGGER.log(Level.SEVERE, "Unexpected error while sending email for Order ID " + order.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to construct the HTML body of the confirmation email.
     *
     * @param order The Order object containing booking details.
     * @return A String containing the HTML email body.
     */
    private String buildBookingConfirmationEmail(Order order) {
        // Use StringBuilder for efficient string concatenation
        StringBuilder content = new StringBuilder();
        // Define a formatter for date and time display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Start HTML structure
        content.append("<!DOCTYPE html>");
        content.append("<html lang=\"vi\"><head><meta charset=\"UTF-8\"></head><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");

        // Greeting
        content.append("<h2>Chào ").append(order.getCustomer() != null && order.getCustomer().getFullName() != null ? order.getCustomer().getFullName().getFirstName() : "Quý khách").append(",</h2>");

        // Confirmation message
        content.append("<p>Cảm ơn bạn đã đặt tour tại công ty chúng tôi. Dưới đây là chi tiết đặt vé của bạn:</p>");

        // Booking Summary List
        content.append("<ul style='list-style-type: none; padding-left: 0;'>");
        content.append("<li><b>Mã đặt vé:</b> ").append(order.getBookingReference() != null ? order.getBookingReference() : "N/A").append("</li>");
        content.append("<li><b>Ngày đặt:</b> ").append(order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "N/A").append("</li>");

        // Tour Details (check for nulls)
        if (order.getTourDetail() != null && order.getTourDetail().getTour() != null) {
            content.append("<li><b>Tên tour:</b> ").append(order.getTourDetail().getTour().getName()).append("</li>");
        } else {
             content.append("<li><b>Tên tour:</b> (Không xác định)</li>");
        }
        if (order.getTourDetail() != null) {
             content.append("<li><b>Thời gian khởi hành:</b> ").append(order.getTourDetail().getTimeStart() != null ? order.getTourDetail().getTimeStart().format(formatter) : "N/A").append("</li>");
             content.append("<li><b>Thời gian kết thúc:</b> ").append(order.getTourDetail().getTimeFinish() != null ? order.getTourDetail().getTimeFinish().format(formatter) : "N/A").append("</li>");
             content.append("<li><b>Điểm tập trung:</b> ").append(order.getTourDetail().getMeetingPoint() != null ? order.getTourDetail().getMeetingPoint() : "N/A").append("</li>");
        } else {
             content.append("<li><b>Chi tiết chuyến đi:</b> (Không xác định)</li>");
        }

        // Ticket and Payment Info
        content.append("<li><b>Số lượng vé:</b> ").append(order.getTickets() != null ? order.getTickets().size() : 0).append("</li>");
        content.append("<li><b>Tổng tiền:</b> ").append(order.getTotalAmount() != null ? String.format("%,.0f VNĐ", order.getTotalAmount()) : "N/A").append("</li>");
        content.append("<li><b>Phương thức thanh toán:</b> ").append(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A").append("</li>");
        content.append("<li><b>Trạng thái thanh toán:</b> ").append(order.getPaymentStatus() != null ? order.getPaymentStatus() : "N/A").append("</li>");
        content.append("</ul>");

        // Optional: Ticket Details Table
        if (order.getTickets() != null && !order.getTickets().isEmpty()) {
            content.append("<h4>Chi tiết vé:</h4>");
            content.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse; width: 100%; max-width: 600px;'>");
            content.append("<thead style='background-color: #f2f2f2;'><tr><th>Mã vé</th><th>Hành khách</th><th>Giá vé (VNĐ)</th></tr></thead><tbody>");
            for (Ticket ticket : order.getTickets()) {
                content.append("<tr>");
                content.append("<td>").append(ticket.getId() != null ? ticket.getId() : "N/A").append("</td>");
                content.append("<td>").append(ticket.getPassengerName() != null ? ticket.getPassengerName() : "N/A").append("</td>");
                content.append("<td style='text-align: right;'>").append(ticket.getPricePaid() != null ? String.format("%,.0f", ticket.getPricePaid()) : "N/A").append("</td>");
                content.append("</tr>");
            }
            content.append("</tbody></table>");
        }

        // Closing remarks
        content.append("<p style='margin-top: 20px;'>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>");
        content.append("<p>Trân trọng,<br><b>Đội ngũ Quản lý Tour</b></p>"); // Make company name bold

        // End HTML structure
        content.append("</body></html>");
        return content.toString();
    }
}
