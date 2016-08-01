package com.robsterthelobster.unitransit_lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by robin on 6/28/2016.
 */
public class Utility {

    public static float DEFAULT_LATLONG = 0F;

    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains(context.getString(R.string.pref_location_latitude))
                && prefs.contains(context.getString(R.string.pref_location_longitude));
    }

    public static float getLocationLatitude(Context context) {
        SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(context.getString(R.string.pref_location_latitude),
                DEFAULT_LATLONG);
    }

    public static float getLocationLongitude(Context context) {
        SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(context.getString(R.string.pref_location_longitude),
                DEFAULT_LATLONG);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /*
        append minutes/seconds with min
        more detailed if given seconds
     */
    public static String getArrivalTime(int minutes, double seconds){
        if(seconds > 60){
            return minutes + "";
        }else if(seconds > 0){
            return "<1";
        }else{
            return "0";
        }
    }

    /*
        convert hex string into a hue
     */
    public static float hexToHue(String colorStr) {
        colorStr = colorStr.replace("#", "");
        int color = (int)Long.parseLong(colorStr, 16);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color >> 0) & 0xFF;
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        return hsv[0];
    }

    /*
        distance between two latlng
        implementation of haversine formula

        http://stackoverflow.com/questions/3695224/
        sqlite-getting-nearest-locations-with-latitude-and-longitude
     */
    public static double getDistanceBetweenTwoPoints(double locationLatitude,
                                                     double locationLongitude,
                                                     double latitude, double longitude) {
        double EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(locationLatitude - latitude);
        double dLon = Math.toRadians(locationLongitude - longitude);
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(locationLatitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = EARTH_RADIUS * c;

        return d;
    }

    /*
        Direction comes in shorthand N,E,S,W and combination
        The icon used is already facing right so E = 0
     */
    public static float getRotationFromDirection(String direction){
        switch (direction){
            case "N":
                return 270f;
            case "NW":
                return 225f;
            case "NE":
                return 315f;
            case "E":
                return 0f;
            case "S":
                return 90f;
            case "SE":
                return 45f;
            case "SW":
                return 135f;
            case "W":
                return 180f;
            default:
                return 0f;
        }
    }

    public static String getFullDirectionName(String direction){
        switch (direction){
            case "N":
                return "north";
            case "NW":
                return "northwest";
            case "NE":
                return "northeast";
            case "E":
                return "east";
            case "S":
                return "south";
            case "SE":
                return "southeast";
            case "SW":
                return "southwest";
            case "W":
                return "west";
            default:
                return direction;
        }
    }

}
