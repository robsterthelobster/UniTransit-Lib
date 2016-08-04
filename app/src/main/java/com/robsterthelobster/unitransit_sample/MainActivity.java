package com.robsterthelobster.unitransit_sample;

import android.os.Bundle;

import com.robsterthelobster.unitransit_lib.ArrivalsActivity;
import com.robsterthelobster.unitransit_lib.Constants;

public class MainActivity extends ArrivalsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Constants.URL = "http://www.ucishuttles.com/";
        super.onCreate(savedInstanceState);
    }
}
