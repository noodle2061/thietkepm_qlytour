package com.b21dccn449.quanlytour.event; // New package

import com.b21dccn449.quanlytour.entity.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Represents an event that is published when a booking (Order) is successfully created.
 * This event carries the newly created Order object.
 */
public class BookingSuccessEvent extends ApplicationEvent {
    private final Order order;

    /**
     * Create a new {@code BookingSuccessEvent}.
     *
     * @param source the object on which the event initially occurred or with
     * which the event is associated (never {@code null}). Typically 'this' from the publisher.
     * @param order  The successfully created Order object.
     */
    public BookingSuccessEvent(Object source, Order order) {
        super(source);
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null for BookingSuccessEvent");
        }
        this.order = order;
    }

    /**
     * Gets the Order associated with this event.
     * @return The successfully created Order.
     */
    public Order getOrder() {
        return order;
    }
}
