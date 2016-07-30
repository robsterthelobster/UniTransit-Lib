package com.robsterthelobster.unitransitlib.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robin on 6/20/2016.
 */
public class Arrivals {
    @SerializedName("PredictionTime")
    private String predictionTime;
    @SerializedName("Predictions")
    private List<Prediction> predictions = new ArrayList<>();

    public String getPredictionTime() {
        return predictionTime;
    }

    public void setPredictionTime(String predictionTime) {
        this.predictionTime = predictionTime;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }
}
