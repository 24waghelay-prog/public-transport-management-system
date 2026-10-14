package transport.template;

import transport.builder.Ticket;

/**
 * Concrete booking implementation for metro transit using distance slab pricing and open seating.
 *
 * @pattern Template Method
 * @role Concrete Class
 */
public class MetroBooking extends BookingProcess {
    private String lastHookMessage = "";

    @Override
    protected double calculateFare(BookingRequest request) {
        double dist = Math.max(1.0, request.distanceKm());
        if (dist <= 5.0) {
            return 20.0;
        } else if (dist <= 15.0) {
            return 35.0;
        } else if (dist <= 30.0) {
            return 50.0;
        } else {
            return 70.0;
        }
    }

    @Override
    protected String reserveSeat(BookingRequest request) {
        // Metro transit operates on an open unreserved standing/seating basis
        return "Open / Standing (Unreserved)";
    }

    @Override
    protected void afterBooking(Ticket ticket) {
        this.lastHookMessage = "Metro smart token activated for gate entry: " + ticket.getTicketId();
    }

    @Override
    public String getTransportType() {
        return "METRO";
    }

    public String getLastHookMessage() {
        return lastHookMessage;
    }
}
