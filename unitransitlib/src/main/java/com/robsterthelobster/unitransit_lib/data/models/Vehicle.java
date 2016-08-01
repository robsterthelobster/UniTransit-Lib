package com.robsterthelobster.unitransit_lib.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by robin on 6/20/2016.
 */
public class Vehicle {
    @SerializedName("ID")
    private int id;
    @SerializedName("APCPercentage")
    private int apcPercentage;
    @SerializedName("RouteId")
    private int routeId;
    @SerializedName("PatternId")
    private int patternId;
    @SerializedName("Name")
    private String name;
    @SerializedName("HasAPC")
    private boolean hasAPC;
    @SerializedName("IconPrefix")
    private String iconPrefix;
    @SerializedName("DoorStatus")
    private int doorStatus;
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;
    @SerializedName("Coordinate")
    private Coordinate coordinate;
    @SerializedName("Speed")
    private int speed;
    @SerializedName("Heading")
    private String heading;
    @SerializedName("Updated")
    private String updated;
    @SerializedName("UpdatedAgo")
    private String updatedAgo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApcPercentage() {
        return apcPercentage;
    }

    public void setApcPercentage(int apcPercentage) {
        this.apcPercentage = apcPercentage;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasAPC() {
        return hasAPC;
    }

    public void setHasAPC(boolean hasAPC) {
        this.hasAPC = hasAPC;
    }

    public String getIconPrefix() {
        return iconPrefix;
    }

    public void setIconPrefix(String iconPrefix) {
        this.iconPrefix = iconPrefix;
    }

    public int getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(int doorStatus) {
        this.doorStatus = doorStatus;
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

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getUpdatedAgo() {
        return updatedAgo;
    }

    public void setUpdatedAgo(String updatedAgo) {
        this.updatedAgo = updatedAgo;
    }
}
