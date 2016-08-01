package com.robsterthelobster.unitransit_lib;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.robsterthelobster.unitransit_lib.data.db.BusContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RouteMapFragment extends SupportMapFragment
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener{

    private final String TAG = RouteMapFragment.class.getSimpleName();

    private final int STOP_LOADER = 0;
    private final int VEHICLE_LOADER = 1;
    private final int ARRIVAL_LOADER = 2;

    private final String[] STOP_COLUMNS = {
            BusContract.StopEntry.STOP_ID,
            BusContract.StopEntry.STOP_NAME,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR
    };
    public final int C_STOP_ID = 0;
    public final int C_STOP_NAME = 1;
    public final int C_STOP_LAT = 2;
    public final int C_STOP_LONG = 3;
    public final int C_COLOR = 4;

    private final String[] VEHICLE_COLUMNS = {
            BusContract.VehicleEntry.ROUTE_ID,
            BusContract.VehicleEntry.BUS_NAME,
            BusContract.VehicleEntry.LATITUDE,
            BusContract.VehicleEntry.LONGITUDE,
            BusContract.VehicleEntry.PERCENTAGE,
            BusContract.VehicleEntry.DIRECTION
    };
    public final int C_ROUTE_ID = 0;
    public final int C_BUS_NAME = 1;
    public final int C_BUS_LAT = 2;
    public final int C_BUS_LONG = 3;
    public final int C_PERCENTAGE = 4;
    public final int C_DIRECTION = 5;

    private final String[] ARRIVAL_COLUMNS = {
            BusContract.StopEntry.TABLE_NAME + "." + BusContract.StopEntry.STOP_NAME,
            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL,
            BusContract.ArrivalEntry.MINUTES,
            BusContract.ArrivalEntry.MIN_ALT,
            BusContract.ArrivalEntry.BUS_NAME,
            BusContract.ArrivalEntry.BUS_NAME_ALT
    };
    public final int C_ARRIVAL_STOP_NAME = 0;
    public final int C_ARRIVAL_SECONDS = 1;
    public final int C_ARRIVAL_MIN = 2;
    public final int C_ARRIVAL_MIN_ALT = 3;
    public final int C_ARRIVAL_BUS_NAME = 4;
    public final int C_ARRIVAL_BUS_NAME_ALT = 5;

    private GoogleMap mMap;
    private String routeID = "";
    private List<Marker> stopMarkers;
    private List<Marker> vehicleMarkers;
    private final int MAP_PADDING = 200;

    private Snackbar snackbar;
    private SnackbarManager snackbarManager;
    private CoordinatorLayout snackbarLayout;
    private HashMap<String, String> stopArrivalTimes;
    private HashMap<String, String> vehicleStats;
    private boolean noTimesAvailable = false;
    private boolean shouldCenter = true;

    public RouteMapFragment() {
        getMapAsync(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            routeID = arguments.getInt(Constants.ROUTE_ID_KEY) + "";
        }
        stopMarkers = new ArrayList<>();
        vehicleMarkers = new ArrayList<>();
        stopArrivalTimes = new HashMap<>();
        vehicleStats = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        View viewButtons = inflater.inflate(R.layout.fragment_map_buttons, null);
        ViewGroup mainChild = (ViewGroup) ((ViewGroup)rootView).getChildAt(0);
        mainChild.addView(viewButtons);
        Button btn1 = (Button) rootView.findViewById(R.id.button_center);
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                centerMapToMarkers(MAP_PADDING);
            }
        });
        snackbarLayout =
                (CoordinatorLayout) container.getRootView().findViewById(R.id.detail_coordinator);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOP_LOADER, null, this);
        getLoaderManager().initLoader(VEHICLE_LOADER, null, this);
        getLoaderManager().initLoader(ARRIVAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (snackbarManager != null)
            snackbarManager.onSetUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setPadding(0,0,0, getResources().getInteger(R.integer.map_bottom_dimen));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case STOP_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.StopEntry.buildStopsInRouteUri(routeID),
                        STOP_COLUMNS,
                        BusContract.RouteEntry.TABLE_NAME + "." +
                                BusContract.RouteEntry.ROUTE_ID + " = ?",
                        new String[] {routeID},
                        null
                        );
            case VEHICLE_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.VehicleEntry.buildVehiclesInRouteUri(routeID),
                        VEHICLE_COLUMNS,
                        null,
                        null,
                        null);
            case ARRIVAL_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " = ?" +
                                " AND " + BusContract.ArrivalEntry.TABLE_NAME + "." +
                                BusContract.ArrivalEntry.ROUTE_ID + " = ?",
                        new String[]{"1", routeID},
                        null);
            default:
                Log.d(TAG, "No such id for loader");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        int id = loader.getId();
        switch(id){
            case STOP_LOADER:
                for(Marker marker : stopMarkers){
                    marker.remove();
                }
                stopMarkers.clear();
                while(data.moveToNext() && mMap != null){
                    double latitude = data.getDouble(C_STOP_LAT);
                    double longitude = data.getDouble(C_STOP_LONG);
                    String stopName = data.getString(C_STOP_NAME);
                    String color = data.getString(C_COLOR);

                    LatLng latLng = new LatLng(latitude, longitude);
                    stopMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(getBitmapDescriptor(R.drawable.ic_directions_bus_black_24dp,
                                    Color.parseColor(color)))
                            .position(latLng).title(stopName)));
                }
                if(shouldCenter){
                    shouldCenter = false;
                    centerMapToMarkers(MAP_PADDING);
                }

                //drawRoutePath(color);
                break;
            case VEHICLE_LOADER:
                for(Marker marker : vehicleMarkers){
                    marker.remove();
                }
                vehicleMarkers.clear();
                while(data.moveToNext() && mMap != null){

                    double latitude = data.getDouble(C_BUS_LAT);
                    double longitude = data.getDouble(C_BUS_LONG);
                    String busName = getString(R.string.map_bus_name, data.getString(C_BUS_NAME));
                    String percentage = getString(R.string.map_percentage, data.getInt(C_PERCENTAGE));
                    String direction = data.getString(C_DIRECTION);

                    LatLng latLng = new LatLng(latitude, longitude);

                    vehicleMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(getBitmapDescriptor(R.drawable.bus_tracker,
                                    ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                            .position(latLng).rotation(Utility.getRotationFromDirection(direction))
                            .flat(true).title(busName)));

                    direction = getString(R.string.map_direction, Utility.getFullDirectionName(direction));
                    vehicleStats.put(busName, percentage + "\n" + direction);
                }
                break;
            case ARRIVAL_LOADER:
                if(data.getCount() == 0){
                    noTimesAvailable = true;
                }
                else{
                    while(data.moveToNext()){
                        String routeName = data.getString(C_ARRIVAL_STOP_NAME);
                        int minutes = data.getInt(C_ARRIVAL_MIN);
                        String altArrivalTime = ""+data.getInt(C_ARRIVAL_MIN_ALT);
                        double seconds = data.getDouble(C_ARRIVAL_SECONDS);

                        String arrivalTime = Utility.getArrivalTime(minutes, seconds);
                        String text = formatBusTime(data.getString(C_ARRIVAL_BUS_NAME), arrivalTime)
                                + "\n" +
                                formatBusTime(data.getString(C_ARRIVAL_BUS_NAME_ALT), altArrivalTime);
                        stopArrivalTimes.put(routeName, text);
                    }
                    noTimesAvailable = false;
                }
                break;
            default:
                Log.d(TAG, "No such id for loader");
        }
    }

    private String formatBusTime(String busName, String time){
        String text = "";
        if(busName != null && time != null){
            text = getString(R.string.map_arrival_time_message, busName, time);
        }
        return text;
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    // http://stackoverflow.com/questions/14828217/
    // android-map-v2-zoom-to-show-all-the-markers
    private void centerMapToMarkers(int padding){
        if(stopMarkers.size() == 0){
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : stopMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
    }

    // http://stackoverflow.com/questions/16262837/
    // how-to-draw-route-in-google-maps-api-v2-from-my-location
    private void drawRoutePath(String colorHex){
        PolylineOptions options = new PolylineOptions();

        options.color(Color.parseColor(colorHex) );
        options.width(5);
        options.visible(true);

        for ( Marker marker : stopMarkers ) {
            options.add(marker.getPosition());
        }

        mMap.addPolyline(options);
    }

    private BitmapDescriptor getBitmapDescriptor(int id, int color) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), id);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(color != -1)
                vectorDrawable.setTint(color);
        }
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void showSnackbar(final String str) {
        snackbarManager = new SnackbarManager(new SnackbarManager.Create() {
            @Override
            public Snackbar create() {
                snackbar = Snackbar.make(snackbarLayout, str, Snackbar.LENGTH_LONG);
                View snackView = snackbar.getView();
                TextView textView =
                        (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.setAction(getString(R.string.snackbar_dismiss), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbarManager = null;
                    }
                });
                return snackbar;
            }
        });
        snackbarManager.show(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (stopArrivalTimes != null && !noTimesAvailable && stopMarkers.contains(marker)){
            showSnackbar(stopArrivalTimes.get(marker.getTitle()));
        }else{
            showSnackbar(getString(R.string.empty_default_message));
        }

        if(vehicleMarkers != null && vehicleMarkers.contains(marker)){
            showSnackbar(vehicleStats.get(marker.getTitle()));
        }
        return false;
    }
}
