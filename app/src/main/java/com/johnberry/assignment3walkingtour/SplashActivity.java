package com.johnberry.assignment3walkingtour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 5000;
    private LocationManager locationManager;
    private MyLocListener locationListener;
    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Possibly chck perm's here
        // Possibly load required resources here

        // Handler is used to execute something in the future

            new Handler().postDelayed(() -> {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i =
                        new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
                // close this activity
                finish();
            }, SPLASH_TIME_OUT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }
}