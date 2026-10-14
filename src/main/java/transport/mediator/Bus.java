package transport.mediator;

import java.util.Objects;

/**
 * Bus colleague interacting exclusively through the ControlCenter mediator.
 *
 * @pattern Mediator
 * @role Colleague
 */
public class Bus {
    private final String busId;
    private final ControlCenter controlCenter;
    private String routeId;
    private String status; // "IN_SERVICE", "DELAYED", "BREAKDOWN", "STANDBY"
    private int currentDelayMinutes;

    public Bus(String busId, String routeId, ControlCenter controlCenter) {
        if (controlCenter == null) {
            throw new IllegalArgumentException("Control center mediator cannot be null");
        }
        this.busId = Objects.requireNonNull(busId, "Bus ID cannot be null");
        this.routeId = routeId;
        this.controlCenter = controlCenter;
        this.status = "IN_SERVICE";
        this.currentDelayMinutes = 0;
        this.controlCenter.registerBus(this);
    }

    /**
     * Reports a delay incident to the control center mediator.
     *
     * @param delayMinutes estimated delay duration
     */
    public void reportDelay(int delayMinutes) {
        this.currentDelayMinutes = delayMinutes;
        this.status = "DELAYED";
        // Notify mediator without referencing Stations or Passengers
        controlCenter.notify(this, "BUS_DELAYED", delayMinutes);
    }

    /**
     * Reports a mechanical failure or breakdown to the mediator.
     *
     * @param reason failure description
     */
    public void reportBreakdown(String reason) {
        this.status = "BREAKDOWN";
        // Notify mediator without referencing Drivers or Replacement Buses
        controlCenter.notify(this, "BUS_BREAKDOWN", reason);
    }

    public void assignRoute(String routeId) {
        this.routeId = routeId;
        this.status = "IN_SERVICE";
        this.currentDelayMinutes = 0;
    }

    public String getBusId() {
        return busId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrentDelayMinutes() {
        return currentDelayMinutes;
    }

    @Override
    public String toString() {
        return String.format("Bus[%s] Route: %s | Status: %s | Delay: %d min",
                busId, routeId, status, currentDelayMinutes);
    }
}
