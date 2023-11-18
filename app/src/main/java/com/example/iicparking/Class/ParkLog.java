package com.example.iicparking.Class;

// This class is used to put dummy data in fire store for testing, it only put data without status and not double parking
public class ParkLog {
    String carPlate;
    String endTime;
    String location;
    String matriculationNo;
    String parkType;
    String startTime;

    public ParkLog(String carPlate, String endTime, String location, String matriculationNo, String parkType, String startTime) {
        this.carPlate = carPlate;
        this.endTime = endTime;
        this.location = location;
        this.matriculationNo = matriculationNo;
        this.parkType = parkType;
        this.startTime = startTime;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMatriculationNo() {
        return matriculationNo;
    }

    public void setMatriculationNo(String matriculationNo) {
        this.matriculationNo = matriculationNo;
    }

    public String getParkType() {
        return parkType;
    }

    public void setParkType(String parkType) {
        this.parkType = parkType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
