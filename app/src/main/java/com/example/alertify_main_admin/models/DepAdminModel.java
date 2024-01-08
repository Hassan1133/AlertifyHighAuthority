package com.example.alertify_main_admin.models;

import java.io.Serializable;

public class DepAdminModel implements Serializable {

    private String depAdminId;

    private String uId;
    private String depAdminImageUrl;

    private String depAdminName;

    private String depAdminPoliceStation;
    private String depAdminEmail;

    private String depAdminStatus;

    public String getDepAdminId() {
        return depAdminId;
    }

    public void setDepAdminId(String depAdminId) {
        this.depAdminId = depAdminId;
    }

    public String getDepAdminImageUrl() {
        return depAdminImageUrl;
    }

    public void setDepAdminImageUrl(String depAdminImageUrl) {
        this.depAdminImageUrl = depAdminImageUrl;
    }

    public String getDepAdminName() {
        return depAdminName;
    }

    public void setDepAdminName(String depAdminName) {
        this.depAdminName = depAdminName;
    }

    public String getDepAdminPoliceStation() {
        return depAdminPoliceStation;
    }

    public void setDepAdminPoliceStation(String depAdminPoliceStation) {
        this.depAdminPoliceStation = depAdminPoliceStation;
    }

    public String getDepAdminEmail() {
        return depAdminEmail;
    }

    public void setDepAdminEmail(String depAdminEmail) {
        this.depAdminEmail = depAdminEmail;
    }

    public String getUid() {
        return uId;
    }

    public void setUid(String uId) {
        this.uId = uId;
    }

    public String getDepAdminStatus() {
        return depAdminStatus;
    }

    public void setDepAdminStatus(String depAdminStatus) {
        this.depAdminStatus = depAdminStatus;
    }
}
