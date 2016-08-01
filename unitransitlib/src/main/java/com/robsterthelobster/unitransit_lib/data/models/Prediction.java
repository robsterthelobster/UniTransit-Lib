package com.robsterthelobster.unitransit_lib.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by robin on 6/20/2016.
 */
public class Prediction {
    @SerializedName("RouteId")
    private int routeId;
    @SerializedName("RouteName")
    private String routeName;
    @SerializedName("StopId")
    private int stopId;
    @SerializedName("BusName")
    private String busName;
    @SerializedName("Minutes")
    private int minutes;
    @SerializedName("ArriveTime")
    private String arriveTime;
    @SerializedName("Direction")
    private int direction;
    @SerializedName("SchedulePrediction")
    private boolean schedulePrediction;
    @SerializedName("VehicleId")
    private int vehicleId;
    @SerializedName("IsLayover")
    private boolean isLayover;
    @SerializedName("Rules")
    private Object rules;
    @SerializedName("ScheduledTime")
    private Object scheduledTime;
    @SerializedName("SecondsToArrival")
    private double secondsToArrival;
    @SerializedName("OnBreak")
    private boolean onBreak;
    @SerializedName("ScheduledArriveTime")
    private Object scheduledArriveTime;
    @SerializedName("ScheduledMinutes")
    private int scheduledMinutes;
    @SerializedName("TripId")
    private Object tripId;
    @SerializedName("TripOrder")
    private int tripOrder;

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isSchedulePrediction() {
        return schedulePrediction;
    }

    public void setSchedulePrediction(boolean schedulePrediction) {
        this.schedulePrediction = schedulePrediction;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isLayover() {
        return isLayover;
    }

    public void setLayover(boolean layover) {
        isLayover = layover;
    }

    public Object getRules() {
        return rules;
    }

    public void setRules(Object rules) {
        this.rules = rules;
    }

    public Object getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Object scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public double getSecondsToArrival() {
        return secondsToArrival;
    }

    public void setSecondsToArrival(double secondsToArrival) {
        this.secondsToArrival = secondsToArrival;
    }

    public boolean isOnBreak() {
        return onBreak;
    }

    public void setOnBreak(boolean onBreak) {
        this.onBreak = onBreak;
    }

    public Object getScheduledArriveTime() {
        return scheduledArriveTime;
    }

    public void setScheduledArriveTime(Object scheduledArriveTime) {
        this.scheduledArriveTime = scheduledArriveTime;
    }

    public int getScheduledMinutes() {
        return scheduledMinutes;
    }

    public void setScheduledMinutes(int scheduledMinutes) {
        this.scheduledMinutes = scheduledMinutes;
    }

    public Object getTripId() {
        return tripId;
    }

    public void setTripId(Object tripId) {
        this.tripId = tripId;
    }

    public int getTripOrder() {
        return tripOrder;
    }

    public void setTripOrder(int tripOrder) {
        this.tripOrder = tripOrder;
    }
}
