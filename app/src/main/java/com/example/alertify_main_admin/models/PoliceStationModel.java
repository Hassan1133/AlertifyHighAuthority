package com.example.alertify_main_admin.models;

import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

public class PoliceStationModel implements Serializable {
    private String id;
    private String policeStationName;
    private String policeStationLocation;
    private double policeStationLatitude;
    private double policeStationLongitude;

    private List<LatLngWrapper> boundaries; // List of LatLng for boundaries

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoliceStationName() {
        return policeStationName;
    }

    public void setPoliceStationName(String policeStationName) {
        this.policeStationName = policeStationName;
    }

    public String getPoliceStationLocation() {
        return policeStationLocation;
    }

    public void setPoliceStationLocation(String policeStationLocation) {
        this.policeStationLocation = policeStationLocation;
    }

    public double getPoliceStationLatitude() {
        return policeStationLatitude;
    }

    public void setPoliceStationLatitude(double policeStationLatitude) {
        this.policeStationLatitude = policeStationLatitude;
    }

    public double getPoliceStationLongitude() {
        return policeStationLongitude;
    }

    public void setPoliceStationLongitude(double policeStationLongitude) {
        this.policeStationLongitude = policeStationLongitude;
    }

    public List<LatLngWrapper> getBoundaries() {
        return boundaries;
    }
    public void setBoundaries(List<LatLngWrapper> boundaries) {
        this.boundaries = boundaries;
    }

}
