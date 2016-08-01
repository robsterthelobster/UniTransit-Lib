package com.robsterthelobster.unitransit_lib.data.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class BusContentProvider extends ContentProvider {

    private BusDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int ROUTES = 100;
    private static final int STOPS = 200;
    private static final int STOPS_ROUTE = 201;
    private static final int ARRIVALS = 300;
    private static final int ARRIVALS_ROUTE_STOP = 301;
    private static final int VEHICLES = 400;
    private static final int VEHICLES_ROUTE = 401;
    private static final int FAVORITES = 500;

    private static final SQLiteQueryBuilder arrivalsRouteStopBuilder;
    private static final SQLiteQueryBuilder stopsRouteBuilder;

    static {
        arrivalsRouteStopBuilder = new SQLiteQueryBuilder();

        /* Arrivals
        // INNER JOIN Routes ON Arrivals.route_id = Routes.route_id
        // INNER JOIN Stops ON Arrivals.stop_id = Stops.stop_id
        // INNER JOIN Stops ON Arrivals + Favorites where ids are equal
        // JOIN to get color and stop name
        */
        arrivalsRouteStopBuilder.setTables(
                BusContract.ArrivalEntry.TABLE_NAME +
                        " INNER JOIN " + BusContract.RouteEntry.TABLE_NAME +
                        " ON " + BusContract.ArrivalEntry.TABLE_NAME +
                        "." + BusContract.ArrivalEntry.ROUTE_ID +
                        " = " + BusContract.RouteEntry.TABLE_NAME +
                        "." + BusContract.RouteEntry.ROUTE_ID +
                        " INNER JOIN " + BusContract.StopEntry.TABLE_NAME +
                        " ON " + BusContract.ArrivalEntry.TABLE_NAME +
                        "." + BusContract.ArrivalEntry.STOP_ID +
                        " = " + BusContract.StopEntry.TABLE_NAME +
                        "." + BusContract.StopEntry.STOP_ID +
                        " INNER JOIN " + BusContract.FavoriteEntry.TABLE_NAME +
                        " ON " + BusContract.ArrivalEntry.TABLE_NAME +
                        "." + BusContract.ArrivalEntry.ROUTE_ID +
                        " = " + BusContract.FavoriteEntry.TABLE_NAME +
                        "." + BusContract.FavoriteEntry.ROUTE_ID +
                        " AND " + BusContract.ArrivalEntry.TABLE_NAME +
                        "." + BusContract.ArrivalEntry.STOP_ID +
                        " = " + BusContract.FavoriteEntry.TABLE_NAME +
                        "." + BusContract.FavoriteEntry.STOP_ID);
    }

    static {
        stopsRouteBuilder = new SQLiteQueryBuilder();

        stopsRouteBuilder.setTables(
                BusContract.StopEntry.TABLE_NAME +
                        " INNER JOIN " +
                        BusContract.RouteEntry.TABLE_NAME +
                        " ON " + BusContract.StopEntry.TABLE_NAME +
                        "." + BusContract.StopEntry.ROUTE_ID +
                        " = " + BusContract.RouteEntry.TABLE_NAME +
                        "." + BusContract.RouteEntry.ROUTE_ID);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BusContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, BusContract.PATH_ROUTES, ROUTES);
        matcher.addURI(authority, BusContract.PATH_STOPS, STOPS);
        matcher.addURI(authority, BusContract.PATH_ARRIVALS, ARRIVALS);
        matcher.addURI(authority, BusContract.PATH_VEHICLES, VEHICLES);
        matcher.addURI(authority, BusContract.PATH_FAVORITE, FAVORITES);

        matcher.addURI(authority, BusContract.PATH_STOPS + "/*", STOPS_ROUTE);
        matcher.addURI(authority, BusContract.PATH_ARRIVALS + "/*/*", ARRIVALS_ROUTE_STOP);
        matcher.addURI(authority, BusContract.PATH_VEHICLES + "/*", VEHICLES_ROUTE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new BusDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        switch (match){
            case ROUTES:
                rowsDeleted =
                        db.delete(BusContract.RouteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOPS:
                rowsDeleted =
                        db.delete(BusContract.StopEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARRIVALS:
                rowsDeleted =
                        db.delete(BusContract.ArrivalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VEHICLES:
                rowsDeleted =
                        db.delete(BusContract.VehicleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITES:
                rowsDeleted =
                        db.delete(BusContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case ROUTES:
                return BusContract.RouteEntry.CONTENT_TYPE;
            case STOPS:
                return BusContract.StopEntry.CONTENT_TYPE;
            case STOPS_ROUTE:
                return BusContract.StopEntry.CONTENT_TYPE;
            case ARRIVALS:
                return BusContract.ArrivalEntry.CONTENT_TYPE;
            case ARRIVALS_ROUTE_STOP:
                return BusContract.ArrivalEntry.CONTENT_TYPE;
            case VEHICLES:
                return BusContract.VehicleEntry.CONTENT_TYPE;
            case VEHICLES_ROUTE:
                return BusContract.VehicleEntry.CONTENT_TYPE;
            case FAVORITES:
                return BusContract.FavoriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch(match){
            case ROUTES:
                _id = db.insertWithOnConflict(BusContract.RouteEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = BusContract.RouteEntry.buildRouteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case STOPS:
                _id = db.insertWithOnConflict(BusContract.StopEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = BusContract.StopEntry.buildStopUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case ARRIVALS:
                _id = db.insert(BusContract.ArrivalEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BusContract.ArrivalEntry.buildArrivalUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case VEHICLES:
                _id = db.insert(BusContract.VehicleEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BusContract.VehicleEntry.buildVehicleUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case FAVORITES:
                // can only be one entry
                _id = db.insertWithOnConflict(BusContract.FavoriteEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = BusContract.FavoriteEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCurosr;

        switch(match) {
            case ROUTES:
                retCurosr = db.query(
                        BusContract.RouteEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case STOPS:
                retCurosr = db.query(
                        BusContract.StopEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case STOPS_ROUTE:
                retCurosr = stopsRouteBuilder.query(db,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ARRIVALS:
                retCurosr = arrivalsRouteStopBuilder.query(db,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ARRIVALS_ROUTE_STOP:
                //TODO
                retCurosr = db.query(
                        BusContract.ArrivalEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case VEHICLES:
                retCurosr = db.query(
                        BusContract.VehicleEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case VEHICLES_ROUTE:
                //TODO
                retCurosr = getVehiclesWithRoute(uri, projection, sortOrder);
                break;
            case FAVORITES:
                retCurosr = db.query(
                        BusContract.FavoriteEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCurosr.setNotificationUri(getContext().getContentResolver(), uri);
        return retCurosr;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case ROUTES:
                rowsUpdated = db.update(BusContract.RouteEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case STOPS:
                rowsUpdated = db.update(BusContract.StopEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case ARRIVALS:
                rowsUpdated = db.update(BusContract.ArrivalEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case VEHICLES:
                rowsUpdated = db.update(BusContract.VehicleEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case FAVORITES:
                rowsUpdated = db.update(BusContract.FavoriteEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        int returnCount;

        switch (match) {
            case ROUTES:
                returnCount = bulkInsert(BusContract.RouteEntry.TABLE_NAME,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case STOPS:
                returnCount = bulkInsert(BusContract.StopEntry.TABLE_NAME,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case ARRIVALS:
                // keep only 500 records
                mDbHelper.getWritableDatabase().execSQL(
                        "DELETE FROM " + BusContract.ArrivalEntry.TABLE_NAME + " WHERE " +
                                BusContract.ArrivalEntry._ID + " IN (SELECT " +
                                BusContract.ArrivalEntry._ID + " FROM " +
                                BusContract.ArrivalEntry.TABLE_NAME + " ORDER BY " +
                                BusContract.ArrivalEntry._ID + " DESC LIMIT -1 OFFSET 500)");
                returnCount = bulkInsert(BusContract.ArrivalEntry.TABLE_NAME,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case VEHICLES:
                returnCount = bulkInsert(BusContract.VehicleEntry.TABLE_NAME,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case FAVORITES:
                returnCount = bulkInsert(BusContract.FavoriteEntry.TABLE_NAME,
                        values, SQLiteDatabase.CONFLICT_IGNORE);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        return returnCount;
    }

    private int bulkInsert(String tableName, ContentValues[] values, int conflictCode){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int returnCount = 0;
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insertWithOnConflict(tableName, null, value, conflictCode);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return returnCount;
    }

    private Cursor getVehiclesWithRoute(Uri uri, String[] projection, String sortOrder){
        String selection = BusContract.VehicleEntry.ROUTE_ID + "=?";
        String[] selectionArgs = new String[]{BusContract.VehicleEntry.getIdFromUri(uri)};

        Cursor retCursor = mDbHelper.getReadableDatabase().query(
                BusContract.VehicleEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return retCursor;
    }
}
