package transport.mediator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Station colleague maintaining passenger electronic display boards.
 * Communicates exclusively through the ControlCenter mediator.
 *
 * @pattern Mediator
 * @role Colleague
 */
public class Station {
    private final String stationId;
    private final String name;
    private final ControlCenter controlCenter;
    private final List<String> displayBoard = new ArrayList<>();

    public Station(String stationId, String name, ControlCenter controlCenter) {
        if (controlCenter == null) {
            throw new IllegalArgumentException("Control center mediator cannot be null");
        }
        this.stationId = Objects.requireNonNull(stationId, "Station ID cannot be null");
        this.name = Objects.requireNonNull(name, "Station name cannot be null");
        this.controlCenter = controlCenter;
        this.controlCenter.registerStation(this);
    }

    /**
     * Updates electronic departure board with new announcement.
     *
     * @param announcement message to display
     */
    public void updateDisplay(String announcement) {
        if (announcement != null && !announcement.isBlank()) {
            displayBoard.add(announcement.trim());
        }
    }

    public void clearDisplay() {
        displayBoard.clear();
    }

    public List<String> getDisplayBoard() {
        return Collections.unmodifiableList(displayBoard);
    }

    public String getLatestAnnouncement() {
        return displayBoard.isEmpty() ? "No active alerts" : displayBoard.get(displayBoard.size() - 1);
    }

    public String getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Station[%s: %s | Alerts: %d]", stationId, name, displayBoard.size());
    }
}
