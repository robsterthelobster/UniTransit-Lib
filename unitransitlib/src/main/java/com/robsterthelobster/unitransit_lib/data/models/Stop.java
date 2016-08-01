package com.robsterthelobster.unitransit_lib.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by robin on 6/20/2016.
 */
public class Stop {
    @SerializedName("ID")
    private int id;
    @SerializedName("Image")
    private String image;
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;
    @SerializedName("Name")
    private String name;
    @SerializedName("RtpiNumber")
    private int rtpiNumber;
    @SerializedName("ShowLabel")
    private boolean showLabel;
    @SerializedName("ShowStopRtpiNumberLabel")
    private boolean showStopRtpiNumberLabel;
    @SerializedName("ShowVehicleName")
    private boolean showVehicleName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRtpiNumber() {
        return rtpiNumber;
    }

    public void setRtpiNumber(int rtpiNumber) {
        this.rtpiNumber = rtpiNumber;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isShowStopRtpiNumberLabel() {
        return showStopRtpiNumberLabel;
    }

    public void setShowStopRtpiNumberLabel(boolean showStopRtpiNumberLabel) {
        this.showStopRtpiNumberLabel = showStopRtpiNumberLabel;
    }

    public boolean isShowVehicleName() {
        return showVehicleName;
    }

    public void setShowVehicleName(boolean showVehicleName) {
        this.showVehicleName = showVehicleName;
    }
}
