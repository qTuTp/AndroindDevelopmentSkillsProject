package com.example.iicparking.Class;

public class Vehicle {
    private String carPlateNumber;
    private String color;

    public Vehicle(String carPlateNumber, String color) {
        this.carPlateNumber = carPlateNumber;
        this.color = color;
    }

    public String getCarPlateNumber() {
        return carPlateNumber;
    }

    public void setCarPlateNumber(String carPlateNumber) {
        this.carPlateNumber = carPlateNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
