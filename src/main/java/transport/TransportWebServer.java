package transport;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import transport.builder.*;
import transport.mediator.Bus;
import transport.memento.ScheduleMemento;
import transport.prototype.*;
import transport.template.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Embedded HTTP server for web dashboard interaction without external frameworks.
 */
public class TransportWebServer {
    private final TransportSystem system;
    private final int port;
    private HttpServer server;

    public TransportWebServer(TransportSystem system, int port) {
        this.system = system;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", this::handleStatic);
        server.createContext("/api/state", this::handleState);
        server.createContext("/api/route", ex -> handlePost(ex, this::apiCreateRoute));
        server.createContext("/api/create-vehicle", ex -> handlePost(ex, this::apiCreateVehicle));
        server.createContext("/api/clone-schedule", ex -> handlePost(ex, this::apiCloneSchedule));
        server.createContext("/api/book", ex -> handlePost(ex, this::apiBookTicket));
        server.createContext("/api/cancel-ticket", ex -> handlePost(ex, this::apiCancelTicket));
        server.createContext("/api/edit-schedule", ex -> handlePost(ex, this::apiEditSchedule));
        server.createContext("/api/undo", ex -> handlePost(ex, this::apiUndo));
        server.createContext("/api/redo", ex -> handlePost(ex, this::apiRedo));
        server.createContext("/api/simulate-delay", ex -> handlePost(ex, this::apiSimulateDelay));
        server.createContext("/api/simulate-breakdown", ex -> handlePost(ex, this::apiSimulateBreakdown));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/web/index.html")) {
            byte[] bytes = in != null ? in.readAllBytes() : "<h1>Not Found</h1>".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    private void handleState(HttpExchange exchange) throws IOException {
        byte[] b = WebStateSerializer.serialize(system).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, b.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
    }

    private void handlePost(HttpExchange exchange, ApiAction action) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        byte[] b = ("{\"message\":\"" + WebJsonUtils.escape(action.execute(body)) + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, b.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
    }

    private String apiCreateRoute(String body) {
        RouteBuilder rb = new RouteBuilder()
                .id(WebJsonUtils.getField(body, "id", "RT-" + System.currentTimeMillis()))
                .name(WebJsonUtils.getField(body, "name", "New Route"))
                .fareRules(Double.parseDouble(WebJsonUtils.getField(body, "fare", "20.0")), 4.0)
                .frequency(Integer.parseInt(WebJsonUtils.getField(body, "frequency", "20")));
        WebJsonUtils.getStringList(body, "stops").forEach(rb::addStop);
        Route r = rb.build();
        system.addRoute(r);
        return "Route built successfully via RouteBuilder: " + r.getName();
    }

    private String apiCreateVehicle(String body) {
        Vehicle v = system.getVehicleRegistry().createVehicle(
                WebJsonUtils.getField(body, "template", "AC Bus"),
                WebJsonUtils.getField(body, "newId", "FLEET-" + System.currentTimeMillis()));
        system.addFleetVehicle(v);
        return "Cloned vehicle from registry template: " + v.getId();
    }

    private String apiCloneSchedule(String body) {
        Schedule src = system.getSchedules().get(WebJsonUtils.getField(body, "sourceId", ""));
        if (src == null) return "Source schedule not found";
        Schedule copy = src.copy();
        String newId = WebJsonUtils.getField(body, "newId", "SCH-CLONE");
        copy.setScheduleId(newId);
        copy.setDayType(WebJsonUtils.getField(body, "dayType", "Weekend"));
        copy.addDepartureTime("20:45");
        system.getSchedules().put(newId, copy);
        return "Cloned schedule via Prototype copy(): " + copy.getScheduleId();
    }

    private String apiBookTicket(String body) {
        BookingProcess proc = switch (WebJsonUtils.getField(body, "mode", "BUS").toUpperCase()) {
            case "METRO" -> system.getMetroBooking();
            case "TRAIN" -> system.getTrainBooking();
            default -> system.getBusBooking();
        };
        Ticket t = proc.book(new BookingRequest(
                WebJsonUtils.getField(body, "passenger", "Guest"),
                WebJsonUtils.getField(body, "source", "Central"),
                WebJsonUtils.getField(body, "destination", "Airport"), 3, 12.0,
                WebJsonUtils.getField(body, "travelClass", "STANDARD"), null, "CARD",
                Double.parseDouble(WebJsonUtils.getField(body, "discount", "0.0"))));
        system.registerTicket(t);
        return "Ticket issued via Template Method: " + t.getTicketId() + " (" + t.getSeatNumber() + ")";
    }

    private String apiCancelTicket(String body) {
        Ticket t = system.getTickets().get(WebJsonUtils.getField(body, "ticketId", ""));
        if (t != null) { t.cancel(); return "Ticket cancelled: " + t.getTicketId(); }
        return "Ticket not found";
    }

    private String apiEditSchedule(String body) {
        Schedule s = system.getSchedules().get(WebJsonUtils.getField(body, "scheduleId", ""));
        if (s == null) return "Schedule not found";
        system.getScheduleHistory().saveState(s.createMemento());
        s.addDepartureTime(WebJsonUtils.getField(body, "newTiming", "22:00"));
        return "Schedule edited. Snapshot saved to Memento caretaker.";
    }

    private String apiUndo(String body) {
        if (!system.getScheduleHistory().canUndo()) return "Nothing to undo";
        Schedule s = system.getSchedules().values().iterator().next();
        s.restore(system.getScheduleHistory().undo(s.createMemento()));
        return "Undo applied. Restored memento: " + s.getDepartureTimes();
    }

    private String apiRedo(String body) {
        if (!system.getScheduleHistory().canRedo()) return "Nothing to redo";
        Schedule s = system.getSchedules().values().iterator().next();
        s.restore(system.getScheduleHistory().redo(s.createMemento()));
        return "Redo applied. Restored memento: " + s.getDepartureTimes();
    }

    private String apiSimulateDelay(String body) {
        Bus bus = system.getControlCenter().getBus(WebJsonUtils.getField(body, "busId", "BUS-VOLVO-01"));
        if (bus == null) return "Bus not found";
        bus.reportDelay(Integer.parseInt(WebJsonUtils.getField(body, "delayMinutes", "15")));
        return "Bus reported delay to Mediator.";
    }

    private String apiSimulateBreakdown(String body) {
        Bus bus = system.getControlCenter().getBus(WebJsonUtils.getField(body, "busId", "BUS-VOLVO-01"));
        if (bus == null) return "Bus not found";
        bus.reportBreakdown(WebJsonUtils.getField(body, "reason", "Mechanical failure"));
        return "Breakdown reported to Mediator. Standby replacement dispatched!";
    }

    @FunctionalInterface interface ApiAction { String execute(String body); }
}
