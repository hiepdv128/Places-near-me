package com.placesnearme.models;

/**
 * Created by TruongPQ on 4/9/16.
 */
public class Geometry {
    private Location location;

    public Geometry(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
