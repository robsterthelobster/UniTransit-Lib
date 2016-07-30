package com.robsterthelobster.unitransitlib.data;

import com.robsterthelobster.unitransitlib.data.models.Arrivals;
import com.robsterthelobster.unitransitlib.data.models.Route;
import com.robsterthelobster.unitransitlib.data.models.Stop;
import com.robsterthelobster.unitransitlib.data.models.Vehicle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by robin on 6/21/2016.
 * Retrofit interface
 */
public interface UniBusApiEndpointInterface {
    @GET("Region/0/Routes")
    Call<List<Route>> getRoutes();

    @GET("Route/{route}/Direction/0/Stops")
    Call<List<Stop>> getStops(@Path("route") int route);

    @GET("Route/{route}/Stop/{stop}/Arrivals")
    Call<Arrivals> getArrivalTimes(@Path("route") int route, @Path("stop") int stop);

    @GET("Route/{route}/Vehicles")
    Call<List<Vehicle>> getVehicles(@Path("route") int route);
}