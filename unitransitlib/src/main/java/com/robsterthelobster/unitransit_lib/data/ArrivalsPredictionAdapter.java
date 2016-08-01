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

package com.robsterthelobster.unitransit_lib.data;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.robsterthelobster.unitransit_lib.ArrivalsFragment;
import com.robsterthelobster.unitransit_lib.R;
import com.robsterthelobster.unitransit_lib.Utility;
import com.robsterthelobster.unitransit_lib.data.db.BusContract;

/**
 * Created by robin
 * https://gist.github.com/ZkHaider/9bf0e1d7b8a2736fd676
 */
public class ArrivalsPredictionAdapter extends CursorRecyclerViewAdapter<ArrivalsPredictionAdapter.ViewHolder> {
    private static final String TAG = ArrivalsPredictionAdapter.class.getSimpleName();

    private static Context mContext;
    private boolean[] checks;
    private boolean checkOverride = false; // to use favorite variable or checks

    public ArrivalsPredictionAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private int originalHeight = 0;
        private int expandingHeight = 0;
        private boolean isViewExpanded = false;

        private final TextView routeView;
        private final TextView timeView;
        private final CheckBox buttonView;
        private final TextView stopView;
        private final TextView timeViewAlt;
        private final View view;

        public ViewHolder(View v) {
            super(v);
            routeView = (TextView) v.findViewById(R.id.prediction_route_name);
            timeView = (TextView) v.findViewById(R.id.prediction_arrival_time);
            timeViewAlt = (TextView) v.findViewById(R.id.prediction_arrival_time_alt);
            buttonView = (CheckBox) v.findViewById(R.id.prediction_favorite_button);
            stopView = (TextView) v.findViewById(R.id.prediction_stop_name);
            view = v;

            v.setOnClickListener(this);

            if (!isViewExpanded) {
                timeViewAlt.setVisibility(View.GONE);
                timeViewAlt.setEnabled(false);
            }
        }

        public void onClick(View v) {
            Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");

            // initialization
            if (originalHeight == 0) {
                originalHeight = view.getHeight();
                expandingHeight = (int)(originalHeight * .25);
            }

            // Declare a ValueAnimator object
            ValueAnimator valueAnimator;
            if (!isViewExpanded) {
                timeViewAlt.setVisibility(View.VISIBLE);
                timeViewAlt.setEnabled(true);
                Log.d(TAG, timeViewAlt.getHeight() + "");
                isViewExpanded = true;
                valueAnimator = ValueAnimator.ofInt(originalHeight,
                                originalHeight + expandingHeight);
            } else {
                isViewExpanded = false;
                valueAnimator = ValueAnimator.ofInt(originalHeight + expandingHeight,
                        originalHeight);

                Animation a = new AlphaAnimation(1.00f, 0.00f); // Fade out

                a.setDuration(100);
                // Set a listener to the animation and configure onAnimationEnd
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        timeViewAlt.setVisibility(View.GONE);
                        timeViewAlt.setEnabled(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                timeViewAlt.startAnimation(a);
            }
            valueAnimator.setDuration(100);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                    view.requestLayout();
                }
            });
            valueAnimator.start();
        }

        public void setBackground(String color){
            view.setBackgroundColor(Color.parseColor(color));
        }

        public TextView getRouteView() {
            return routeView;
        }

        public TextView getTimeViewAlt() {
            return timeViewAlt;
        }

        public TextView getTimeView() {
            return timeView;
        }

        public CheckBox getButtonView() {
            return buttonView;
        }

        public TextView getStopView() {
            return stopView;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.prediction_item_expanded, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        final int routeID = cursor.getInt(ArrivalsFragment.C_ROUTE_ID);
        final int stopID = cursor.getInt(ArrivalsFragment.C_STOP_ID);
        int minutes = cursor.getInt(ArrivalsFragment.C_MINUTES);
        int minutesAlt = cursor.getInt(ArrivalsFragment.C_MIN_ALT);
        double seconds = cursor.getDouble(ArrivalsFragment.C_SECONDS);
        int isFavorite = cursor.getInt(ArrivalsFragment.C_FAVORITE);
        
        final String routeName = cursor.getString(ArrivalsFragment.C_ROUTE_NAME);
        String stopName = cursor.getString(ArrivalsFragment.C_STOP_NAME);
        String color = cursor.getString(ArrivalsFragment.C_COLOR);
        String arrivalTime = mContext.getString(R.string.arrival_time,
                Utility.getArrivalTime(minutes, seconds));
        String altArrivalTime = (minutesAlt != 0) ?
                mContext.getString(R.string.arrival_time, minutesAlt+"") :
                mContext.getString(R.string.no_arrival_time);

        final int position = cursor.getPosition();

        final CheckBox checkBox = viewHolder.getButtonView();
        checkBox.setOnCheckedChangeListener(null);

        if(isFavorite == 1 && !checkOverride){
            checks[position] = true;
        }
        checkBox.setChecked(checks[position]);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ContentValues contentValues = new ContentValues();
                String where = BusContract.FavoriteEntry.ROUTE_ID + " =? " + " AND " +
                        BusContract.FavoriteEntry.STOP_ID + " =? ";
                String[] args = new String[] {String.valueOf(routeID), String.valueOf(stopID)};

                checkOverride = true;
                checks[position] = isChecked;

                if(isChecked){
                    contentValues.put(BusContract.FavoriteEntry.FAVORITE, 1);
                }else{
                    contentValues.put(BusContract.FavoriteEntry.FAVORITE, 0);
                }

                if(contentValues.size() > 0){
                    mContext.getContentResolver().update(
                            BusContract.FavoriteEntry.CONTENT_URI,
                            contentValues,
                            where,
                            args
                    );
                }
            }
        });

        viewHolder.setBackground(color);
        viewHolder.getRouteView().setText(routeName);
        viewHolder.getTimeView().setText(arrivalTime);
        viewHolder.getTimeViewAlt().setText(altArrivalTime);
        viewHolder.getStopView().setText(stopName);
    }

    @Override
    public Cursor swapCursor(Cursor cursor){
        checks = new boolean[cursor.getCount()];
        checkOverride = false;
        return super.swapCursor(cursor);
    }
}
