package com.sjbt.sdk.sample.model;

public class DeviceLocation {

    public double longitude;
    public double latitude;
    public int timeZone;

    public DeviceLocation(double longitude, double latitude, int timeZone) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.timeZone = timeZone;
    }
}
