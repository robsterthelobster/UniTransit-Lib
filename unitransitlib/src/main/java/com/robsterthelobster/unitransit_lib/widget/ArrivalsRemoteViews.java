package com.robsterthelobster.unitransit_lib.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.robsterthelobster.unitransit_lib.R;
import com.robsterthelobster.unitransit_lib.Utility;
import com.robsterthelobster.unitransit_lib.data.db.BusContract;

/**
 * Created by robin on 5/16/2016.
 */
public class ArrivalsRemoteViews extends RemoteViewsService {
    public final String LOG_TAG = ArrivalsRemoteViews.class.getSimpleName();
    private final String[] ARRIVAL_COLUMNS = {
            BusContract.ArrivalEntry._ID,
            BusContract.ArrivalEntry.TABLE_NAME + "." + BusContract.ArrivalEntry.ROUTE_NAME,
            BusContract.ArrivalEntry.MINUTES,
            BusContract.ArrivalEntry.MIN_ALT,
            BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL,
            BusContract.RouteEntry.TABLE_NAME + "." + BusContract.RouteEntry.COLOR,
            BusContract.StopEntry.TABLE_NAME + "." + BusContract.StopEntry.STOP_NAME,
    };
    public static final int C_ID = 0;
    public static final int C_ROUTE_NAME = 1;
    public static final int C_MINUTES = 2;
    public static final int C_MIN_ALT = 3;
    public static final int C_SECONDS = 4;
    public static final int C_COLOR = 5;
    public static final int C_STOP_NAME = 6;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                String location = "";
                Context context = ArrivalsRemoteViews.this;
                if(Utility.isLocationLatLonAvailable(context)){
                    double latitude = Utility.getLocationLatitude(context);
                    double longitude = Utility.getLocationLongitude(context);

                    double fudge = Math.pow(Math.cos(Math.toRadians(latitude)),2);

                    String latOrder = "(" + latitude + " - " + BusContract.StopEntry.LATITUDE + ")";
                    String longOrder = "(" + longitude + " - " + BusContract.StopEntry.LONGITUDE + ")";
                    location = "(" + latOrder + "*" + latOrder +
                            "+" + longOrder + "*" + longOrder + "*" + fudge + "), ";
                }
                String sortOrder = BusContract.FavoriteEntry.FAVORITE + " DESC, " +
                        location +
                        BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " ASC";
                data = getContentResolver().query(
                        BusContract.ArrivalEntry.CONTENT_URI,
                        ARRIVAL_COLUMNS,
                        BusContract.ArrivalEntry.IS_CURRENT + " = ?",
                        new String[]{"1"},
                        sortOrder);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_arrivals_item);
                Context context = ArrivalsRemoteViews.this;

                String routeName = data.getString(C_ROUTE_NAME);
                String stopName = data.getString(C_STOP_NAME);
                int minutesAlt = data.getInt(C_MIN_ALT);

                String arrivalTime = context.getString(R.string.arrival_time,
                        Utility.getArrivalTime(data.getInt(C_MINUTES), data.getDouble(C_SECONDS)));
                if(minutesAlt != 0){
                    arrivalTime +=  " & " + getString(R.string.arrival_time, minutesAlt+"");
                }

                views.setTextViewText(R.id.widget_arrivals_route_text, routeName);
                views.setTextViewText(R.id.widget_arrivals_stop_text, stopName);
                views.setTextViewText(R.id.widget_arrivals_time_text, arrivalTime);
                views.setInt(R.id.widget_item, "setBackgroundColor",
                        Color.parseColor(data.getString(C_COLOR)));

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_arrivals_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(C_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
