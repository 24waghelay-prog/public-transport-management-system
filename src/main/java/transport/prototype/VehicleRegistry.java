package transport.prototype;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry storing pre-configured vehicle prototypes for on-demand cloning.
 *
 * @pattern Prototype
 * @role Prototype Registry
 */
public class VehicleRegistry {
    private final Map<String, Vehicle> templates = new HashMap<>();

    public VehicleRegistry() {
        initializeDefaultTemplates();
    }

    /**
     * Initializes standard public transport vehicle templates.
     */
    private void initializeDefaultTemplates() {
        // AC Bus Template
        registerTemplate("AC Bus", new BusVehicle(
                "TEMPLATE-AC", "Volvo 9400 B11R", 45, true, "DIESEL",
                List.of("Air Conditioning", "WiFi", "Reclining Seats", "CCTV", "GPS")
        ));

        // Non-AC Bus Template
        registerTemplate("Non-AC Bus", new BusVehicle(
                "TEMPLATE-NON-AC", "Tata Starbus Ultra", 55, false, "CNG",
                List.of("Standard Seating", "CCTV", "GPS", "Digital Signage")
        ));

        // Electric Bus Template
        registerTemplate("Electric Bus", new BusVehicle(
                "TEMPLATE-EV", "BYD K9 Electric", 38, true, "ELECTRIC",
                List.of("Zero Emission", "Air Conditioning", "USB Ports", "Wheelchair Ramp", "CCTV")
        ));
    }

    /**
     * Registers a new prototype under a key.
     *
     * @param key unique template identifier
     * @param prototype prototype vehicle
     */
    public void registerTemplate(String key, Vehicle prototype) {
        if (key == null || prototype == null) {
            throw new IllegalArgumentException("Template key and prototype cannot be null");
        }
        templates.put(key, prototype);
    }

    /**
     * Creates a new Vehicle instance by cloning the registered template.
     *
     * @param key template key
     * @param newId new unique identifier for the cloned vehicle
     * @return cloned Vehicle with new ID
     */
    public Vehicle createVehicle(String key, String newId) {
        Vehicle prototype = templates.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("No vehicle template registered under: " + key);
        }
        Vehicle copy = prototype.copy();
        copy.setId(newId);
        return copy;
    }

    public boolean hasTemplate(String key) {
        return templates.containsKey(key);
    }

    public Set<String> getAvailableTemplateKeys() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public Map<String, Vehicle> getAllTemplates() {
        return Collections.unmodifiableMap(templates);
    }
}
