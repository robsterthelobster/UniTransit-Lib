package com.robsterthelobster.unitransitlib.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by robin on 6/20/2016.
 */
public class Coordinate {
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
