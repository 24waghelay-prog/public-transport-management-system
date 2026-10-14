package transport.mediator;

/**
 * Mediator interface declaring communication and colleague management protocols.
 *
 * @pattern Mediator
 * @role Mediator
 */
public interface ControlCenter {

    /**
     * Centralized event dispatch method used by Colleagues to notify the Mediator.
     * Colleagues NEVER invoke each other directly; all events route through here.
     *
     * @param sender colleague raising the event
     * @param event event identifier (e.g., "BUS_DELAYED", "BUS_BREAKDOWN")
     * @param data payload associated with the event
     */
    void notify(Object sender, String event, Object data);

    void registerBus(Bus bus);

    void registerStation(Station station);

    void registerDriver(Driver driver);

    Bus getBus(String busId);

    Station getStation(String stationId);

    Driver getDriver(String driverId);
}
