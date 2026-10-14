package transport.template;

import transport.builder.Ticket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concrete booking implementation for bus transit with per-stop pricing.
 *
 * @pattern Template Method
 * @role Concrete Class
 */
public class BusBooking extends BookingProcess {
    private static final double BASE_FARE = 10.0;
    private static final double PER_STOP_RATE = 5.0;
    private static final AtomicInteger SEAT_SEQUENCE = new AtomicInteger(1);
    private String lastHookMessage = "";

    @Override
    protected double calculateFare(BookingRequest request) {
        int stops = Math.max(1, request.stopCount());
        return BASE_FARE + (stops * PER_STOP_RATE);
    }

    @Override
    protected String reserveSeat(BookingRequest request) {
        if (request.preferredSeat() != null && !request.preferredSeat().isBlank()) {
            return "Bus-Seat-" + request.preferredSeat().trim();
        }
        int seatNum = (SEAT_SEQUENCE.getAndIncrement() % 40) + 1;
        return "Bus-Seat-" + seatNum + (seatNum % 2 == 0 ? " (Window)" : " (Aisle)");
    }

    @Override
    protected void afterBooking(Ticket ticket) {
        this.lastHookMessage = "Bus SMS notification sent to " + ticket.getPassengerName() +
                " for seat " + ticket.getSeatNumber();
    }

    @Override
    public String getTransportType() {
        return "BUS";
    }

    public String getLastHookMessage() {
        return lastHookMessage;
    }
}
