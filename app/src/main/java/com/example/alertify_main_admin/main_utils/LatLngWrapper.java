package com.example.alertify_main_admin.main_utils;
import java.io.Serializable;

public class LatLngWrapper implements Serializable {
    private double latitude;
    private double longitude;

    public LatLngWrapper() {

    }

    public LatLngWrapper(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
