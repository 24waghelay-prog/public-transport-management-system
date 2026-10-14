package transport.mediator;

import java.util.Objects;

/**
 * Driver colleague receiving vehicle dispatch and emergency routing directives.
 * Communicates exclusively through the ControlCenter mediator.
 *
 * @pattern Mediator
 * @role Colleague
 */
public class Driver {
    private final String driverId;
    private final String name;
    private final ControlCenter controlCenter;
    private String assignedBusId;
    private String status; // "STANDBY", "ON_DUTY", "DISPATCHED"
    private String lastDispatchMessage = "";

    public Driver(String driverId, String name, ControlCenter controlCenter) {
        if (controlCenter == null) {
            throw new IllegalArgumentException("Control center mediator cannot be null");
        }
        this.driverId = Objects.requireNonNull(driverId, "Driver ID cannot be null");
        this.name = Objects.requireNonNull(name, "Driver name cannot be null");
        this.controlCenter = controlCenter;
        this.status = "STANDBY";
        this.controlCenter.registerDriver(this);
    }

    /**
     * Called exclusively by the mediator to dispatch the driver to a bus.
     *
     * @param message dispatch instructions
     * @param busId vehicle assigned to the driver
     */
    public void receiveDispatch(String message, String busId) {
        this.lastDispatchMessage = message;
        this.assignedBusId = busId;
        this.status = "DISPATCHED";
    }

    public void reportAvailability() {
        this.status = "STANDBY";
        controlCenter.notify(this, "DRIVER_AVAILABLE", driverId);
    }

    public String getDriverId() {
        return driverId;
    }

    public String getName() {
        return name;
    }

    public String getAssignedBusId() {
        return assignedBusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastDispatchMessage() {
        return lastDispatchMessage;
    }

    @Override
    public String toString() {
        return String.format("Driver[%s: %s | Status: %s | Bus: %s]",
                driverId, name, status, assignedBusId != null ? assignedBusId : "None");
    }
}
