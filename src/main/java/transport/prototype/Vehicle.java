package transport.prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract Vehicle prototype representing the base blueprint for transport fleet assets.
 *
 * @pattern Prototype
 * @role Prototype
 */
public abstract class Vehicle {
    private String id;
    private String model;
    private String category;
    private int capacity;
    private List<String> features;

    protected Vehicle(String id, String model, String category, int capacity, List<String> features) {
        this.id = id;
        this.model = model;
        this.category = category;
        this.capacity = capacity;
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }

    protected Vehicle(Vehicle other) {
        this.id = other.id;
        this.model = other.model;
        this.category = other.category;
        this.capacity = other.capacity;
        // Deep copy of mutable features list
        this.features = new ArrayList<>(other.features);
    }

    /**
     * Creates and returns a deep copy of this vehicle prototype.
     * Concrete subclasses must implement this without using Object.clone() or Cloneable.
     *
     * @return a new, deeply cloned Vehicle instance
     */
    public abstract Vehicle copy();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void addFeature(String feature) {
        if (feature != null && !feature.isBlank()) {
            this.features.add(feature.trim());
        }
    }

    public void removeFeature(String feature) {
        this.features.remove(feature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return capacity == vehicle.capacity &&
                Objects.equals(id, vehicle.id) &&
                Objects.equals(model, vehicle.model) &&
                Objects.equals(category, vehicle.category) &&
                Objects.equals(features, vehicle.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, category, capacity, features);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, Cap: %d, Features: %s)",
                id, model, category, capacity, features);
    }
}
