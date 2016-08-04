package com.robsterthelobster.unitransit_lib.data;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.robsterthelobster.unitransit_lib.Constants;
import com.robsterthelobster.unitransit_lib.R;
import com.robsterthelobster.unitransit_lib.data.db.BusContract;
import com.robsterthelobster.unitransit_lib.data.models.Arrivals;
import com.robsterthelobster.unitransit_lib.data.models.Prediction;
import com.robsterthelobster.unitransit_lib.data.models.Route;
import com.robsterthelobster.unitransit_lib.data.models.Stop;
import com.robsterthelobster.unitransit_lib.data.models.Vehicle;

import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 */
public class UniBusIntentService extends IntentService {

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, UniBusIntentService.class);
            context.startService(sendIntent);
        }
    }

    private final String TAG = UniBusIntentService.class.getSimpleName();

    UniBusApiEndpointInterface apiService;
    public final String BASE_URL = Constants.URL;

    public UniBusIntentService() {
        super("UniBusIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "fetching data");
        fetchData();
        updateWidgets();
    }

    private void fetchData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService =
                retrofit.create(UniBusApiEndpointInterface.class);

        fetchRoutesAndSubData(this);
    }

    private void fetchRoutesAndSubData(final Context context){
        Call<List<Route>> routeCall = apiService.getRoutes();
        routeCall.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                List<Route> routes = response.body();
                if(routes != null){
                    Vector<ContentValues> cVVector = new Vector<>(routes.size());
                    for(Route route : routes){
                        //Log.d(TAG, "route name: " + route.getName());
                        ContentValues routeValues = new ContentValues();

                        int routeID = route.getId();

                        routeValues.put(BusContract.RouteEntry.ROUTE_ID, routeID);
                        routeValues.put(BusContract.RouteEntry.ROUTE_NAME, route.getName());
                        routeValues.put(BusContract.RouteEntry.COLOR, route.getColor());

                        cVVector.add(routeValues);

                        callVehicles(routeID);
                        callStops(routeID);
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.RouteEntry.CONTENT_URI, cvArray);
                    }
                    /*
                    Tell arrivals activity to start the drawer
                    */
                    LocalBroadcastManager.getInstance(context).sendBroadcast(
                            new Intent(Constants.BUS_ROUTE_ACTION));
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, "call failed");
            }
        });
    }

    private void callStops(final int routeID){
        Call<List<Stop>> stopCall = apiService.getStops(routeID);
        stopCall.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {

                /*
                    Reset previous entries' IS_CURRENT to 0

                 */
                ContentValues contentValues = new ContentValues();
                contentValues.put(BusContract.ArrivalEntry.IS_CURRENT, 0);
                getContentResolver().update(BusContract.ArrivalEntry.CONTENT_URI, contentValues,
                        null, null);

                List<Stop> stops = response.body();
                if(stops != null){
                    Vector<ContentValues> stopVector = new Vector<>(stops.size());
                    Vector<ContentValues> fVector = new Vector<>(stops.size());
                    for(Stop stop : stops){
                        //Log.d(TAG, "Stop : " + stop.getName());
                        ContentValues stopValues = new ContentValues();
                        ContentValues favoriteValues = new ContentValues();
                        int stopID = stop.getId();
                        stopValues.put(BusContract.StopEntry.ROUTE_ID, routeID);
                        stopValues.put(BusContract.StopEntry.STOP_ID, stopID);
                        stopValues.put(BusContract.StopEntry.STOP_NAME, stop.getName());
                        stopValues.put(BusContract.StopEntry.LONGITUDE, stop.getLongitude());
                        stopValues.put(BusContract.StopEntry.LATITUDE, stop.getLatitude());

                        favoriteValues.put(BusContract.FavoriteEntry.FAV_KEY,
                                routeID + "-" + stopID);
                        favoriteValues.put(BusContract.FavoriteEntry.ROUTE_ID, routeID);
                        favoriteValues.put(BusContract.FavoriteEntry.STOP_ID, stopID);
                        favoriteValues.put(BusContract.FavoriteEntry.FAVORITE, 0);

                        stopVector.add(stopValues);
                        fVector.add(favoriteValues);

                        callArrivals(routeID, stopID);
                    }
                    if ( stopVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[stopVector.size()];
                        stopVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.StopEntry.CONTENT_URI, cvArray);

                        ContentValues[] fArray = new ContentValues[fVector.size()];
                        fVector.toArray(fArray);
                        getContentResolver().bulkInsert(BusContract.FavoriteEntry.CONTENT_URI, fArray);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Stop>> call, Throwable t) {
                Log.d(TAG, "call failed");
            }
        });
    }

    private void callArrivals(final int routeID, int stopID){
        Call<Arrivals> arrivalsCall = apiService.getArrivalTimes(routeID, stopID);
        arrivalsCall.enqueue(new Callback<Arrivals>() {
            @Override
            public void onResponse(Call<Arrivals> call, Response<Arrivals> response) {

                Arrivals arrivals = response.body();

                if(arrivals != null){
                    List<Prediction> predictions = arrivals.getPredictions();
                    String predictionTime = arrivals.getPredictionTime();

                    int size = predictions.size();
                    ContentValues arrivalValues = new ContentValues();

                    for(int i = 0; i < size; i++){
                        Prediction prediction = predictions.get(i);
                        //Log.d(TAG, "Prediction : " + prediction.getArriveTime());

                        switch (i){
                            case 0:
                                // primary + main prediction
                                arrivalValues.put(BusContract.ArrivalEntry.ROUTE_ID, prediction.getRouteId());
                                arrivalValues.put(BusContract.ArrivalEntry.ROUTE_NAME, prediction.getRouteName());
                                arrivalValues.put(BusContract.ArrivalEntry.STOP_ID, prediction.getStopId());
                                arrivalValues.put(BusContract.ArrivalEntry.PREDICTION_TIME, predictionTime);
                                arrivalValues.put(BusContract.ArrivalEntry.MINUTES, prediction.getMinutes());
                                arrivalValues.put(BusContract.ArrivalEntry.BUS_NAME, prediction.getBusName());
                                arrivalValues.put(BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL, prediction.getSecondsToArrival());
                                arrivalValues.put(BusContract.ArrivalEntry.IS_CURRENT, 1);
                                break;
                            case 1:
                                // get the second prediction
                                arrivalValues.put(BusContract.ArrivalEntry.MIN_ALT, prediction.getMinutes());
                                arrivalValues.put(BusContract.ArrivalEntry.BUS_NAME_ALT, prediction.getBusName());
                                break;
                            case 2:
                                // get the third prediction
                                arrivalValues.put(BusContract.ArrivalEntry.MIN_ALT_2, prediction.getMinutes());
                                arrivalValues.put(BusContract.ArrivalEntry.BUS_NAME_ALT_2, prediction.getBusName());
                                break;
                            default:
                                // do nothing
                                Log.d(TAG, "additional predictions beyond 2");
                        }
                    }
                    if(arrivalValues.size() > 0) {
                        getContentResolver().insert(BusContract.ArrivalEntry.CONTENT_URI, arrivalValues);
                    }
                }
            }

            @Override
            public void onFailure(Call<Arrivals> call, Throwable t) {
                Log.d(TAG, "call failed");
            }
        });
    }

    private void callVehicles(int routeID){
        getContentResolver().delete(BusContract.VehicleEntry.CONTENT_URI, null, null);

        Call<List<Vehicle>> vehiclesCall = apiService.getVehicles(routeID);
        vehiclesCall.enqueue(new Callback<List<Vehicle>>() {
            @Override
            public void onResponse(Call<List<Vehicle>> call, Response<List<Vehicle>> response) {
                List<Vehicle> vehicles = response.body();
                if(vehicles != null){
                    Vector<ContentValues> cVVector = new Vector<>(vehicles.size());
                    for(Vehicle vehicle : vehicles){
                        ContentValues vehicleValues = new ContentValues();

                        vehicleValues.put(BusContract.VehicleEntry.ROUTE_ID, vehicle.getRouteId());
                        vehicleValues.put(BusContract.VehicleEntry.BUS_NAME, vehicle.getName());
                        vehicleValues.put(BusContract.VehicleEntry.LATITUDE, vehicle.getLatitude());
                        vehicleValues.put(BusContract.VehicleEntry.LONGITUDE, vehicle.getLongitude());
                        vehicleValues.put(BusContract.VehicleEntry.PERCENTAGE, vehicle.getApcPercentage());
                        vehicleValues.put(BusContract.VehicleEntry.DIRECTION, vehicle.getHeading());

                        cVVector.add(vehicleValues);
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(BusContract.VehicleEntry.CONTENT_URI, cvArray);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Vehicle>> call, Throwable t) {
                Log.d(TAG, "call failed");
            }
        });
    }

    private void updateWidgets() {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(Constants.ACTION_DATA_UPDATED)
                .setPackage(this.getPackageName());
        this.sendBroadcast(dataUpdatedIntent);
    }
}
