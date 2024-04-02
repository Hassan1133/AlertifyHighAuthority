package com.example.alertify_main_admin.models;

import java.io.Serializable;

public class DepAdminModel implements Serializable {

    private String depAdminId;
    private String depAdminUid;
    private String depAdminName;

    private String depAdminPoliceStation;
    private String depAdminEmail;

    private String depAdminStatus;

    private String depAdminFCMToken;

    private String highAuthorityId;

    public String getHighAuthorityId() {
        return highAuthorityId;
    }

    public void setHighAuthorityId(String highAuthorityId) {
        this.highAuthorityId = highAuthorityId;
    }

    public String getDepAdminId() {
        return depAdminId;
    }

    public void setDepAdminId(String depAdminId) {
        this.depAdminId = depAdminId;
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

    public String getDepAdminUid() {
        return depAdminUid;
    }

    public void setDepAdminUid(String depAdminUid) {
        this.depAdminUid = depAdminUid;
    }

    public String getDepAdminStatus() {
        return depAdminStatus;
    }

    public void setDepAdminStatus(String depAdminStatus) {
        this.depAdminStatus = depAdminStatus;
    }

    public String getDepAdminFCMToken() {
        return depAdminFCMToken;
    }

    public void setDepAdminFCMToken(String depAdminFCMToken) {
        this.depAdminFCMToken = depAdminFCMToken;
    }
}
