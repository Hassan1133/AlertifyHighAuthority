package com.example.alertify_main_admin.models;

import java.io.Serializable;
import java.util.List;

public class UserModel implements Serializable {
    private String id;
    private String name;
    private String email;
    private List<String> policeStationList;
    private List<String> depAdminList;
    private List<String> complaintList;

    public List<String> getComplaintList() {
        return complaintList;
    }

    public void setComplaintList(List<String> complaintList) {
        this.complaintList = complaintList;
    }

    public List<String> getPoliceStationList() {
        return policeStationList;
    }

    public void setPoliceStationList(List<String> policeStationList) {
        this.policeStationList = policeStationList;
    }

    public List<String> getDepAdminList() {
        return depAdminList;
    }

    public void setDepAdminList(List<String> depAdminList) {
        this.depAdminList = depAdminList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
