package com.robsterthelobster.unitransitlib.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.robsterthelobster.unitransitlib.ArrivalsFragment;
import com.robsterthelobster.unitransitlib.Utility;

/**
 * Created by robin on 7/4/2016.
 *
 * Used to filter (distances > threshold)
 *
 * Based on implementation
 * https://gist.github.com/ramzes642/5400792
 */
public class ArrivalsCursorWrapper extends CursorWrapper{

    private int[] index;
    private int count=0;
    private int pos=0;

    public ArrivalsCursorWrapper(Cursor cursor, double latitude, double longitude, int radius) {
        super(cursor);
        this.count = super.getCount();
        this.index = new int[this.count];

        int latColumn = ArrivalsFragment.C_LATITUDE;
        int longColumn = ArrivalsFragment.C_LONGITUDE;

        for (int i=0;i<this.count;i++) {
            super.moveToPosition(i);

            double distance = Utility.getDistanceBetweenTwoPoints(latitude, longitude,
                    getDouble(latColumn), getDouble(longColumn));
            //Log.d("distance", " " +distance);

            if (distance <= radius)
                this.index[this.pos++] = i;
        }
        this.count = this.pos;
        this.pos = 0;
        super.moveToFirst();
    }

    @Override
    public boolean move(int offset) {
        return this.moveToPosition(this.pos+offset);
    }

    @Override
    public boolean moveToNext() {
        return this.moveToPosition(this.pos+1);
    }

    @Override
    public boolean moveToPrevious() {
        return this.moveToPosition(this.pos-1);
    }

    @Override
    public boolean moveToFirst() {
        return this.moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return this.moveToPosition(this.count-1);
    }

    @Override
    public boolean moveToPosition(int position) {
        this.pos = position;
        return !(position >= this.count || position < 0)
                && super.moveToPosition(this.index[position]);
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public int getPosition() {
        return this.pos;
    }
}
