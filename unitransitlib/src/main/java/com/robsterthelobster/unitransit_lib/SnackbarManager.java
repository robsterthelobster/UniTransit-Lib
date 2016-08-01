package com.robsterthelobster.unitransit_lib;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Used to make sure only the map page uses the route snackbar
 * Basically overrides onSetUserVisibleHint to hide snackbar unless otherwise noted(in RouteMapFragment)
 *
 * http://stackoverflow.com/questions/34465005/
 * android-multiple-snackbars-in-separate-fragments-viewpager/34750933
 */
public class SnackbarManager {
    private static final String TAG = SnackbarManager.class.getSimpleName();

    private Snackbar snackbar;
    private Create instance;
    // private boolean isMultiSnackbar;

    public interface Create {
        Snackbar create();
    }

    public SnackbarManager(Create instance) {
        // why not pass in snackbar? coz snackbar.show will fail after 1st show (it multiple snackbar), thus need to recreate it
        snackbar = instance.create();
        this.instance = instance;
    }

    public void show(Fragment fragment) {
        if (fragment.getUserVisibleHint()) {
            snackbar.show();
        }
    }

    public void onSetUserVisibleHint(boolean isVisible) {
        if (isVisible) {
            if (snackbar == null) {
                snackbar = instance.create();
            }
            snackbar.show();
            Log.d(TAG, "showSnackbar="+snackbar.isShown());
            // if snackbar.isShown()=false, if means multiple snackbar exist (might or might not be in same fragment)
            /*
            boolean isMultiSnackbar = !snackbar.isShown();
            // the following is inaccurate when I manually dismiss one of the snackbar
            // even when isShown()=true, the snackbar is not shown
            if (isMultiSnackbar) {
                snackbar = null;
                snackbar = instance.create();
                snackbar.show();
            }
             */
        }
        else {
            Log.d(TAG, "dismissSnackbar");
            snackbar.dismiss();
            // subsequent show will fail, make sure to recreate next
            snackbar = null;
        }
    }
}
