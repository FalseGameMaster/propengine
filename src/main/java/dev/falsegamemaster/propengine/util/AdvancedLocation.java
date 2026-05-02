package dev.falsegamemaster.propengine.util;

import org.bukkit.Location;
import org.bukkit.UndefinedNullability;
import org.bukkit.World;

public class AdvancedLocation extends Location {

    private float roll;

    /**
     * Constructs a new Location with the given coordinates
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     */
    public AdvancedLocation(@UndefinedNullability final World world, final double x, final double y, final double z) { // Paper
        this(world, x, y, z, 0, 0, 0);
    }

    /**
     * Constructs a new Location with the given coordinates and direction
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     * @param yaw The absolute rotation on the x-plane, in degrees
     * @param pitch The absolute rotation on the y-plane, in degrees
     * @param roll The absolute rotation on the z-plane, in degrees
     */
    public AdvancedLocation(@UndefinedNullability final World world, final double x, final double y, final double z, final float yaw, final float pitch, final float roll) { // Paper
        super(world, x, y, z, yaw, pitch);
        this.roll = roll;
    }

    // TODO: document this (4/12/2026)
//    /**
//     * Sets the pitch of this location, measured in degrees.
//     * <ul>
//     * <li>A pitch of 0 represents level forward facing.
//     * <li>A pitch of 90 represents downward facing, or negative y
//     *     direction.
//     * <li>A pitch of -90 represents upward facing, or positive y direction.
//     * </ul>
//     * Increasing pitch values the equivalent of looking down.
//     *
//     * @param pitch new incline's pitch
//     */
    public void setRoll(float roll) {
        this.roll = roll;
    }

    // TODO: document this (4/12/2026)
//    /**
//     * Gets the pitch of this location, measured in degrees.
//     * <ul>
//     * <li>A pitch of 0 represents level forward facing.
//     * <li>A pitch of 90 represents downward facing, or negative y
//     *     direction.
//     * <li>A pitch of -90 represents upward facing, or positive y direction.
//     * </ul>
//     * Increasing pitch values the equivalent of looking down.
//     *
//     * @return the incline's pitch
//     */
    public float getRoll() {
        return roll;
    }

}
