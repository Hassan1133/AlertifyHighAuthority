package com.example.alertify_main_admin.models;

import java.io.Serializable;

public class ComplaintModel implements Serializable {
    private String crimeType;
    private String evidenceType;
    private String complaintId;
    private String crimeDetails;
    private String evidenceUrl;
    private String crimeLocation;
    private double crimeLatitude;
    private double crimeLongitude;
    private String policeStation;
    private String userId;
    private String CrimeDate;
    private String CrimeTime;
    private String complaintDateTime;
    private String investigationStatus;
    private String feedback;

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getInvestigationStatus() {
        return investigationStatus;
    }

    public void setInvestigationStatus(String investigationStatus) {
        this.investigationStatus = investigationStatus;
    }

    public String getComplaintDateTime() {
        return complaintDateTime;
    }

    public void setComplaintDateTime(String complaintDateTime) {
        this.complaintDateTime = complaintDateTime;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
    }

    public String getCrimeDetails() {
        return crimeDetails;
    }

    public void setCrimeDetails(String crimeDetails) {
        this.crimeDetails = crimeDetails;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public String getCrimeLocation() {
        return crimeLocation;
    }

    public void setCrimeLocation(String crimeLocation) {
        this.crimeLocation = crimeLocation;
    }

    public double getCrimeLatitude() {
        return crimeLatitude;
    }

    public void setCrimeLatitude(double crimeLatitude) {
        this.crimeLatitude = crimeLatitude;
    }

    public double getCrimeLongitude() {
        return crimeLongitude;
    }

    public void setCrimeLongitude(double crimeLongitude) {
        this.crimeLongitude = crimeLongitude;
    }

    public String getPoliceStation() {
        return policeStation;
    }

    public void setPoliceStation(String policeStation) {
        this.policeStation = policeStation;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getCrimeDate() {
        return CrimeDate;
    }

    public void setCrimeDate(String crimeDate) {
        CrimeDate = crimeDate;
    }

    public String getCrimeTime() {
        return CrimeTime;
    }

    public void setCrimeTime(String crimeTime) {
        CrimeTime = crimeTime;
    }
}
