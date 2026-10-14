package transport;

import java.util.stream.Collectors;

/**
 * Serializes transport system state into JSON without external dependencies.
 */
public final class WebStateSerializer {
    private WebStateSerializer() {}

    public static String serialize(TransportSystem system) {
        StringBuilder json = new StringBuilder("{");

        json.append("\"routes\":[").append(system.getRoutes().values().stream().map(r -> String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"stops\":[%s],\"baseFare\":%.2f,\"frequency\":%d}",
                r.getId(), WebJsonUtils.escape(r.getName()),
                r.getStops().stream().map(s -> "\"" + WebJsonUtils.escape(s) + "\"").collect(Collectors.joining(",")),
                r.getBaseFare(), r.getFrequencyMinutes())).collect(Collectors.joining(","))).append("],");

        json.append("\"fleet\":[").append(system.getFleet().stream().map(v -> String.format(
                "{\"id\":\"%s\",\"model\":\"%s\",\"capacity\":%d,\"features\":[%s]}",
                v.getId(), WebJsonUtils.escape(v.getModel()), v.getCapacity(),
                v.getFeatures().stream().map(f -> "\"" + WebJsonUtils.escape(f) + "\"").collect(Collectors.joining(",")))).collect(Collectors.joining(","))).append("],");

        json.append("\"tickets\":[").append(system.getTickets().values().stream().map(t -> String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"passenger\":\"%s\",\"src\":\"%s\",\"dst\":\"%s\",\"seat\":\"%s\",\"fare\":%.2f,\"status\":\"%s\"}",
                t.getTicketId(), t.getTransportType(), WebJsonUtils.escape(t.getPassengerName()),
                WebJsonUtils.escape(t.getSource()), WebJsonUtils.escape(t.getDestination()),
                WebJsonUtils.escape(t.getSeatNumber()), t.getFinalFare(), t.getStatus())).collect(Collectors.joining(","))).append("],");

        json.append("\"schedules\":[").append(system.getSchedules().values().stream().map(s -> String.format(
                "{\"id\":\"%s\",\"routeName\":\"%s\",\"dayType\":\"%s\",\"vehicleId\":\"%s\",\"departureTimes\":[%s]}",
                s.getScheduleId(), WebJsonUtils.escape(s.getRouteName()), s.getDayType(), s.getVehicleId(),
                s.getDepartureTimes().stream().map(t -> "\"" + t + "\"").collect(Collectors.joining(",")))).collect(Collectors.joining(","))).append("],");

        json.append("\"undoCount\":").append(system.getScheduleHistory().getUndoCount()).append(",");
        json.append("\"redoCount\":").append(system.getScheduleHistory().getRedoCount()).append(",");

        json.append("\"stationBoards\":[").append(system.getControlCenter().getStations().values().stream()
                .filter(st -> !st.getDisplayBoard().isEmpty())
                .map(st -> String.format("{\"station\":\"%s\",\"alert\":\"%s\"}",
                        WebJsonUtils.escape(st.getName()), WebJsonUtils.escape(st.getLatestAnnouncement()))).collect(Collectors.joining(","))).append("],");

        json.append("\"passengerAlerts\":[").append(system.getControlCenter().getPassengerNotifications().stream()
                .map(a -> "\"" + WebJsonUtils.escape(a) + "\"").collect(Collectors.joining(","))).append("]}");

        return json.toString();
    }
}
