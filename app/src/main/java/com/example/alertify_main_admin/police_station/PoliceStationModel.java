package com.example.alertify_main_admin.police_station;

import java.io.Serializable;

public class PoliceStationModel implements Serializable {
    private String id;
    private String policeStationName;
    private String policeStationLocation;
    private double policeStationLatitude;
    private double policeStationLongitude;

    private String policeStationNumber;
    private String imgUrl;

    public String getPoliceStationNumber() {
        return policeStationNumber;
    }

    public void setPoliceStationNumber(String policeStationNumber) {
        this.policeStationNumber = policeStationNumber;
    }

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

}
