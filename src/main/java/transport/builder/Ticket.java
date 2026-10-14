package transport.builder;

import java.util.Objects;

/**
 * Represents a passenger ticket issued for public transit.
 *
 * @pattern Builder
 * @role Product
 */
public class Ticket {
    private final String ticketId;
    private final String transportType;
    private final String passengerName;
    private final String source;
    private final String destination;
    private final String seatNumber;
    private final double baseFare;
    private final double discount;
    private final double finalFare;
    private final String paymentMode;
    private final String bookingTime;
    private String status;

    Ticket(TicketBuilder builder) {
        this.ticketId = builder.ticketId;
        this.transportType = builder.transportType;
        this.passengerName = builder.passengerName;
        this.source = builder.source;
        this.destination = builder.destination;
        this.seatNumber = builder.seatNumber;
        this.baseFare = builder.baseFare;
        this.discount = builder.discount;
        this.finalFare = builder.finalFare;
        this.paymentMode = builder.paymentMode;
        this.bookingTime = builder.bookingTime;
        this.status = builder.status;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getDiscount() {
        return discount;
    }

    public double getFinalFare() {
        return finalFare;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket ticket)) return false;
        return Objects.equals(ticketId, ticket.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return String.format("[%s] Type: %s | Passenger: %s | Route: %s -> %s | Seat: %s | Fare: $%.2f | Mode: %s | Status: %s",
                ticketId, transportType, passengerName, source, destination, seatNumber, finalFare, paymentMode, status);
    }
}
