package transport.prototype;

import java.util.List;
import java.util.Objects;

/**
 * Concrete Vehicle prototype representing bus transport units.
 *
 * @pattern Prototype
 * @role Concrete Prototype
 */
public class BusVehicle extends Vehicle {
    private boolean airConditioned;
    private String fuelType;

    public BusVehicle(String id, String model, int capacity, boolean airConditioned,
                      String fuelType, List<String> features) {
        super(id, model, "BUS", capacity, features);
        this.airConditioned = airConditioned;
        this.fuelType = fuelType;
    }

    /**
     * Copy constructor for deep copying BusVehicle instances.
     *
     * @param other source BusVehicle to copy
     */
    public BusVehicle(BusVehicle other) {
        super(other);
        this.airConditioned = other.airConditioned;
        this.fuelType = other.fuelType;
    }

    @Override
    public Vehicle copy() {
        return new BusVehicle(this);
    }

    public boolean isAirConditioned() {
        return airConditioned;
    }

    public void setAirConditioned(boolean airConditioned) {
        this.airConditioned = airConditioned;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if (!(o instanceof BusVehicle that)) return false;
        return airConditioned == that.airConditioned &&
                Objects.equals(fuelType, that.fuelType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), airConditioned, fuelType);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Bus, %s, %s, Cap: %d, Features: %s)",
                getId(), getModel(), airConditioned ? "AC" : "Non-AC",
                fuelType, getCapacity(), getFeatures());
    }
}
