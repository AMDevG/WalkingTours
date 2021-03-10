package com.johnberry.assignment3walkingtour;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.NotificationManager;
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

    private static final int SPLASH_TIME_OUT = 2000;
    private LocationManager locationManager;
    private MapsActivity mapsActivity;
    private MyLocListener locationListener;
    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        checkPermission();
        if(checkPermission()){
            System.out.println("\nCheck Permission is true\n\n");
            makeTransition();
        }
        else{
            System.out.println("\nCheck Permission is false\n\n");
        }


    }

    private void makeTransition(){
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

    private boolean checkPermission() {

        // If R or greater, need to ask for these separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.out.println(" \n IN CHECK PERMISSION, PERMISSION NOTGRANTED");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_ONLY_PERM_REQUEST);
                return false;
            }

            return true;

        } else {

            ArrayList<String> perms = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                System.out.println("Access fine location not present");
                perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }

            if (!perms.isEmpty()) {
                String[] array = perms.toArray(new String[0]);
                ActivityCompat.requestPermissions(this,
                        array, LOC_COMBO_REQUEST);
                return false;
            }
        }

        return true;
    }

    public void requestBgPermission() {
        System.out.println("In BGPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
            }

        }
    }

    private void determineLocation() {
        if (checkPermission()) {
            System.out.println("Check permission true; In determine locate");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MyLocListener(mapsActivity);

            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            }
            makeTransition();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOC_ONLY_PERM_REQUEST && permissions.length > 0) {
            System.out.println("Request is LOC ONLY, permissions > 0");
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBgPermission();
//                makeTransition();
            }
            else if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_DENIED){
                System.out.println("***-------- \n Fine location is denied");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Required");
                builder.setMessage("Location Services Required");
                builder.setPositiveButton("OK", (dialog, id) -> finish());
                AlertDialog dialog = builder.create();
                dialog.show();

            }
            //USER DENIES PERMISSIONS
            else{
                Toast.makeText(this,
                        "Location is a required permission to use this app.",
                        Toast.LENGTH_LONG).show();
//                checkPermission();
                finish();
            }
        } else if (requestCode == LOC_COMBO_REQUEST) {
            System.out.println("Request is LOC COMBO");
            int permCount = permissions.length;
            int permSum = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permSum++;
                } else {
                    sb.append(permissions[i]).append(", ");
                }
            }
            if (permSum == permCount) {
                determineLocation();
            } else {
                Toast.makeText(this,
                        "Required permissions not granted: " + sb.toString(),
                        Toast.LENGTH_LONG).show();
//                finish();
            }
        } else if (requestCode == BGLOC_ONLY_PERM_REQUEST) {
            System.out.println("Request is BGLOC ONLY");
            if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                determineLocation();
            }
            else{
                Toast.makeText(this,
                        "Required permissions not granted: Background Location ",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }



}