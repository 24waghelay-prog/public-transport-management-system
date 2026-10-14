package transport.builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent Builder for constructing transit Tickets with sensible defaults.
 *
 * @pattern Builder
 * @role Builder
 */
public class TicketBuilder {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1000);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    String ticketId;
    String transportType = "BUS";
    String passengerName;
    String source;
    String destination;
    String seatNumber = "Unassigned";
    double baseFare = 0.0;
    double discount = 0.0;
    double finalFare = 0.0;
    String paymentMode = "CASH";
    String bookingTime;
    String status = "CONFIRMED";

    public TicketBuilder() {
        this.ticketId = "TKT-" + ID_COUNTER.incrementAndGet();
        this.bookingTime = LocalDateTime.now().format(FORMATTER);
    }

    public TicketBuilder ticketId(String ticketId) {
        if (ticketId != null && !ticketId.isBlank()) {
            this.ticketId = ticketId.trim();
        }
        return this;
    }

    public TicketBuilder transportType(String transportType) {
        if (transportType != null && !transportType.isBlank()) {
            this.transportType = transportType.trim().toUpperCase();
        }
        return this;
    }

    public TicketBuilder passenger(String passengerName) {
        this.passengerName = passengerName;
        return this;
    }

    public TicketBuilder from(String source) {
        this.source = source;
        return this;
    }

    public TicketBuilder to(String destination) {
        this.destination = destination;
        return this;
    }

    public TicketBuilder seat(String seatNumber) {
        if (seatNumber != null && !seatNumber.isBlank()) {
            this.seatNumber = seatNumber.trim();
        }
        return this;
    }

    public TicketBuilder fare(double baseFare) {
        this.baseFare = Math.max(0.0, baseFare);
        recalculateFinalFare();
        return this;
    }

    public TicketBuilder discount(double discount) {
        this.discount = Math.max(0.0, Math.min(100.0, discount));
        recalculateFinalFare();
        return this;
    }

    public TicketBuilder paymentMode(String paymentMode) {
        if (paymentMode != null && !paymentMode.isBlank()) {
            this.paymentMode = paymentMode.trim().toUpperCase();
        }
        return this;
    }

    public TicketBuilder bookingTime(String bookingTime) {
        if (bookingTime != null && !bookingTime.isBlank()) {
            this.bookingTime = bookingTime.trim();
        }
        return this;
    }

    public TicketBuilder status(String status) {
        if (status != null && !status.isBlank()) {
            this.status = status.trim().toUpperCase();
        }
        return this;
    }

    private void recalculateFinalFare() {
        this.finalFare = Math.max(0.0, this.baseFare * (1.0 - (this.discount / 100.0)));
    }

    /**
     * Validates required fields and builds a Ticket product instance.
     *
     * @return fully constructed Ticket
     * @throws IllegalStateException if validation rules fail
     */
    public Ticket build() {
        if (passengerName == null || passengerName.isBlank()) {
            throw new IllegalStateException("Passenger name is required to build a ticket");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalStateException("Source station is required to build a ticket");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("Destination station is required to build a ticket");
        }
        if (source.trim().equalsIgnoreCase(destination.trim())) {
            throw new IllegalStateException("Source and destination cannot be identical");
        }
        if (finalFare <= 0.0 && baseFare <= 0.0) {
            throw new IllegalStateException("Ticket fare must be positive");
        }
        return new Ticket(this);
    }
}
