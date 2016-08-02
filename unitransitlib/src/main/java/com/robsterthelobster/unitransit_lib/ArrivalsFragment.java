/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.robsterthelobster.unitransit_lib;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robsterthelobster.unitransit_lib.data.ArrivalsCursorWrapper;
import com.robsterthelobster.unitransit_lib.data.ArrivalsPredictionAdapter;
import com.robsterthelobster.unitransit_lib.data.UniBusIntentService;
import com.robsterthelobster.unitransit_lib.data.db.BusContract;

import java.util.Calendar;

public class ArrivalsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = ArrivalsFragment.class.getSimpleName();

    private final int ARRIVAL_LOADER = 0;
    private final int STOP_ARRIVAL_LOADER = 1;
    private final int NO_LOCATION_LOADER = 2;

    private final String[] ARRIVAL_COLUMNS = {
            BusContract.ArrivalEntry._ID,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.ROUTE_ID,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.ROUTE_NAME,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.STOP_ID,
            BusContract.ArrivalEntry.PREDICTION_TIME,
            BusContract.ArrivalEntry.MINUTES,
            BusContract.ArrivalEntry.MIN_ALT,
            BusContract.ArrivalEntry.MIN_ALT_2,
            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR,
            BusContract.StopEntry.TABLE_NAME + "." + BusContract.StopEntry.STOP_NAME,
            BusContract.FavoriteEntry.TABLE_NAME + "." + BusContract.FavoriteEntry.FAVORITE,
            BusContract.StopEntry.LATITUDE,
            BusContract.StopEntry.LONGITUDE
    };
    public static final int C_ROUTE_ID = 1;
    public static final int C_ROUTE_NAME = 2;
    public static final int C_STOP_ID = 3;
    public static final int C_PREDICTION_TIME = 4;
    public static final int C_MINUTES = 5;
    public static final int C_MIN_ALT = 6;
    public static final int C_MIN_ALT2 = 7;
    public static final int C_SECONDS = 8;
    public static final int C_COLOR = 9;
    public static final int C_STOP_NAME = 10;
    public static final int C_FAVORITE = 11;
    public static final int C_LATITUDE = 12;
    public static final int C_LONGITUDE = 13;

    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mySwipeRefreshLayout;
    protected ArrivalsPredictionAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected TextView emptyView;
    //Handler mHandler = new Handler();
    Activity mActivity;

    private String routeName;
    private boolean hasRoute = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            routeName = arguments.getString(Constants.ROUTE_NAME_KEY);
            hasRoute = true;
        }
        if(hasRoute)
            updateRouteData();

        startLoaders();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity){
        mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_arrivals, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        mySwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                        updateRouteDataImmediately();
                    }
                }
        );

        mAdapter = new ArrivalsPredictionAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);
        setHasOptionsMenu(true);

        updateRouteDataImmediately();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.arrivals_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            Log.i(TAG, "Refresh menu item selected");
            mySwipeRefreshLayout.setRefreshing(true);
            updateRouteDataImmediately();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String sortOrder;
        String location = "";
        if(Utility.isLocationLatLonAvailable(getContext())){
            double latitude = Utility.getLocationLatitude(getContext());
            double longitude = Utility.getLocationLongitude(getContext());

            double fudge = Math.pow(Math.cos(Math.toRadians(latitude)),2);

            String latOrder = "(" + latitude + " - " + BusContract.StopEntry.LATITUDE + ")";
            String longOrder = "(" + longitude + " - " + BusContract.StopEntry.LONGITUDE + ")";
            location = "(" + latOrder + "*" + latOrder +
                    "+" + longOrder + "*" + longOrder + "*" + fudge + "), ";
        }

        switch (id) {
            case ARRIVAL_LOADER:
                // FAVORITES, THEN ARRIVAL TIME
                sortOrder = BusContract.FavoriteEntry.FAVORITE + " DESC, " +
                        location +
                        BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " ASC";
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " =? ",
                        new String[]{"1"},
                        sortOrder);
            case STOP_ARRIVAL_LOADER:
                return new CursorLoader(getContext(),
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " = ?" +
                                " AND " + BusContract.ArrivalEntry.TABLE_NAME + "." +
                                BusContract.ArrivalEntry.ROUTE_NAME + " = ?",
                        new String[]{"1", routeName},
                        BusContract.StopEntry.TABLE_NAME + "." + BusContract.StopEntry.STOP_ID);
            default:
                Log.d(TAG, "Not valid id: " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor != null){
            int id = loader.getId();
            switch(id){
                // Only the arrival_loader needs to have the cursor filtered
                case ARRIVAL_LOADER:
                    if(cursor != null) {
                        cursor = new ArrivalsCursorWrapper(cursor,
                                Utility.getLocationLatitude(getContext()),
                                Utility.getLocationLongitude(getContext()),
                                getContext().getResources().getInteger(R.integer.nearby_distance));
                    }
                case STOP_ARRIVAL_LOADER:
                    mAdapter.swapCursor(cursor);
                    break;
                default:
                    Log.d(TAG, "Not valid id: " + id);
            }
        }

        updateEmptyView();

        // stop refreshing after finish
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    private void updateRouteDataImmediately() {
        Intent intent = new Intent(getActivity(), UniBusIntentService.class);
        getActivity().startService(intent);
    }

    private void updateRouteData(){
        Intent alarmIntent = new Intent(getActivity(), UniBusIntentService.AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);

        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 0);
        long frequency = 60 * 1000; // one minute, which is the minimum

        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), frequency, pi);
    }

    private void startLoaders(){
        if(hasRoute){
            startLoader(STOP_ARRIVAL_LOADER);
        }else{
            startLoader(ARRIVAL_LOADER);
        }
    }

    private void startLoader(int loader_id){
        Loader loader = getLoaderManager().getLoader(loader_id);
        if(loader != null){
            getLoaderManager().restartLoader(loader_id, null, this);
        }else{
            getLoaderManager().initLoader(loader_id, null, this);
        }
    }

    private void updateEmptyView(){
        if(mAdapter.getItemCount() == 0) {
            int message = R.string.empty_no_data_from_server;
            if (!Utility.isNetworkAvailable(getContext())) {
                message = R.string.empty_no_connection_message;
            } else if (mAdapter.getCursor() instanceof ArrivalsCursorWrapper
                    && Utility.isLocationLatLonAvailable(getContext())) {
                message = R.string.empty_no_nearby_message;
            }
            emptyView.setText(message);
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
