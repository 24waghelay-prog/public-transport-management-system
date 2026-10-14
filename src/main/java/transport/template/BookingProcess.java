package transport.template;

import transport.builder.Ticket;
import transport.builder.TicketBuilder;

/**
 * Abstract skeleton defining the template method for ticket booking across transport modes.
 *
 * @pattern Template Method
 * @role Abstract Class
 */
public abstract class BookingProcess {

    /**
     * Template Method orchestrating the invariant sequence of ticket booking.
     * Declared final to prevent subclasses from altering the algorithmic workflow.
     *
     * @param request passenger booking parameters
     * @return issued Ticket instance
     */
    public final Ticket book(BookingRequest request) {
        validate(request);
        double fare = calculateFare(request);
        String seat = reserveSeat(request);
        boolean paymentSuccess = processPayment(request, fare);
        if (!paymentSuccess) {
            throw new IllegalStateException("Payment failed for booking: " + request.passengerName());
        }
        Ticket ticket = issueTicket(request, fare, seat);
        afterBooking(ticket); // Hook method
        return ticket;
    }

    /**
     * Validates common booking request constraints.
     */
    protected void validate(BookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request cannot be null");
        }
        if (request.passengerName() == null || request.passengerName().isBlank()) {
            throw new IllegalArgumentException("Passenger name cannot be blank");
        }
        if (request.source() == null || request.source().isBlank()) {
            throw new IllegalArgumentException("Source location cannot be blank");
        }
        if (request.destination() == null || request.destination().isBlank()) {
            throw new IllegalArgumentException("Destination location cannot be blank");
        }
        if (request.source().trim().equalsIgnoreCase(request.destination().trim())) {
            throw new IllegalArgumentException("Source and destination must be distinct");
        }
    }

    /**
     * Abstract step: Calculates the fare based on transit-specific pricing rules.
     *
     * @param request booking parameters
     * @return calculated fare before discount
     */
    protected abstract double calculateFare(BookingRequest request);

    /**
     * Abstract step: Reserves seat allocation according to transit modality.
     *
     * @param request booking parameters
     * @return reserved seat designation
     */
    protected abstract String reserveSeat(BookingRequest request);

    /**
     * Processes passenger payment.
     */
    protected boolean processPayment(BookingRequest request, double fare) {
        // Base payment simulation: validates payment mode and positive fare
        return fare > 0 && request.paymentMode() != null && !request.paymentMode().isBlank();
    }

    /**
     * Issues the ticket using TicketBuilder.
     */
    protected Ticket issueTicket(BookingRequest request, double fare, String seat) {
        return new TicketBuilder()
                .transportType(getTransportType())
                .passenger(request.passengerName())
                .from(request.source())
                .to(request.destination())
                .seat(seat)
                .fare(fare)
                .discount(request.discountPercent())
                .paymentMode(request.paymentMode())
                .build();
    }

    /**
     * Hook method: Subclasses may optionally override this to execute post-booking actions.
     * Default implementation is an empty body.
     *
     * @param ticket the issued ticket
     */
    protected void afterBooking(Ticket ticket) {
        // Default empty hook body
    }

    /**
     * Returns the transport modality identifier.
     */
    public abstract String getTransportType();
}
