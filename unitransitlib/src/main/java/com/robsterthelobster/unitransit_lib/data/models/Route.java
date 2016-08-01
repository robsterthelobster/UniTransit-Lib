package com.robsterthelobster.unitransit_lib.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robin on 6/20/2016.
 *
 */
public class Route {
    @SerializedName("ID")
    private Integer id;
    @SerializedName("ArrivalsEnabled")
    private boolean arrivalsEnabled;
    @SerializedName("DisplayName")
    private String displayName;
    @SerializedName("CustomerID")
    private Integer customerID;
    @SerializedName("DirectionStops")
    private Object directionStops;
    @SerializedName("Points")
    private Object points;
    @SerializedName("Color")
    private String color;
    @SerializedName("TextColor")
    private String textColor;
    @SerializedName("ArrivalsShowVehicleNames")
    private boolean arrivalsShowVehicleNames;
    @SerializedName("IsHeadway")
    private boolean isHeadway;
    @SerializedName("ShowLine")
    private boolean showLine;
    @SerializedName("Name")
    private String name;
    @SerializedName("ShortName")
    private String shortName;
    @SerializedName("RegionIDs")
    private List<Object> regionIDs = new ArrayList<>();
    @SerializedName("ForwardDirectionName")
    private String forwardDirectionName;
    @SerializedName("BackwardDirectionName")
    private String backwardDirectionName;
    @SerializedName("NumberOfVehicles")
    private int numberOfVehicles;
    @SerializedName("Patterns")
    private Object patterns;

    public boolean isArrivalsEnabled() {
        return arrivalsEnabled;
    }

    public void setArrivalsEnabled(boolean arrivalsEnabled) {
        this.arrivalsEnabled = arrivalsEnabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Object getDirectionStops() {
        return directionStops;
    }

    public void setDirectionStops(Object directionStops) {
        this.directionStops = directionStops;
    }

    public Object getPoints() {
        return points;
    }

    public void setPoints(Object points) {
        this.points = points;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public boolean isArrivalsShowVehicleNames() {
        return arrivalsShowVehicleNames;
    }

    public void setArrivalsShowVehicleNames(boolean arrivalsShowVehicleNames) {
        this.arrivalsShowVehicleNames = arrivalsShowVehicleNames;
    }

    public boolean isHeadway() {
        return isHeadway;
    }

    public void setHeadway(boolean headway) {
        isHeadway = headway;
    }

    public boolean isShowLine() {
        return showLine;
    }

    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<Object> getRegionIDs() {
        return regionIDs;
    }

    public void setRegionIDs(List<Object> regionIDs) {
        this.regionIDs = regionIDs;
    }

    public String getForwardDirectionName() {
        return forwardDirectionName;
    }

    public void setForwardDirectionName(String forwardDirectionName) {
        this.forwardDirectionName = forwardDirectionName;
    }

    public String getBackwardDirectionName() {
        return backwardDirectionName;
    }

    public void setBackwardDirectionName(String backwardDirectionName) {
        this.backwardDirectionName = backwardDirectionName;
    }

    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    public void setNumberOfVehicles(int numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    public Object getPatterns() {
        return patterns;
    }

    public void setPatterns(Object patterns) {
        this.patterns = patterns;
    }
}
