package com.johnberry.assignment3walkingtour;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Needs:
    //      implementation 'com.google.android.gms:play-services-maps:17.0.0'
    //      implementation 'com.google.android.gms:play-services-location:17.0.0'

    //      android:usesCleartextTraffic="true"

    private static final String TAG = "MapsActivity";
    private boolean fullScreen = false;
    private GoogleMap mMap;
    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int ACCURACY_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Polyline llHistoryPolyline;
    private Polyline llRoutePolyline;

    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private  ArrayList<LatLng> latLonPath = new ArrayList<>();
    private boolean zooming = false;
    private float oldZoom;
    private Marker carMarker;
    private FenceMgr fenceMgr;
    private Geocoder geocoder;
    private TextView addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        addressText = findViewById(R.id.addressTextView);
        checkLocationAccuracy();
        geocoder = new Geocoder(this);
    }

    public void initMap() {

        fenceMgr = new FenceMgr(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }


    /**
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
        zooming = true;

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (checkPermission()) {
            setupLocationListener();
            setupZoomListener();
        }

        //Draws route path
        new Thread(new RoutePathDownloader(this)).start();
    }


    private boolean checkPermission() {

        // If R or greater, need to ask for these separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

//    public void requestBgPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
//            }
//
//        }
//    }

    private void determineLocation() {
        if (checkPermission()) {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MyLocListener(this);

            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            }
        }
    }

    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (checkPermission() && locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 20, locationListener);
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
    }

    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(() -> {
            if (zooming) {
//                Log.d(TAG, "onCameraIdle: DONE ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = false;
                oldZoom = mMap.getCameraPosition().zoom;
            }
        });

        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().zoom != oldZoom) {
//                Log.d(TAG, "onCameraMove: ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = true;
            }
        });
    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            addressText.setText(address.getAddressLine(0));

        } catch (IOException e) {
            e.printStackTrace();
//            addressText.setText("");
        }


        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
//            mMap.addMarker(new MarkerOptions().alpha(0.25f).position(latLng).title("Me"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);


            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            float z = mMap.getCameraPosition().zoom;
            float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
            float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
            float r = factor * multiplier;

            Bitmap icon;
            float rad = getRadius();

            if (rad > 0) {

                MarkerOptions options = new MarkerOptions();
                options.rotation(location.getBearing());
                options.position(latLng);

                if(location.getBearing() > 180) {
                     icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                }
                else if(location.getBearing() < 180){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                }
                else{
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                }
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);
                options.icon(iconBitmap);


                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);
            }
        }

        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


    }

    private float getRadius() {
//        float z = mMap.getCameraPosition().zoom;
//        return 15f * z - 145f;

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        float r = factor * multiplier;
        return r;
    }

    public void showGeoFences(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked()) {
            fenceMgr.drawFences();
        } else {
            fenceMgr.eraseFences();
        }
    }

    public void showTravelPath(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked() && llHistoryPolyline != null) {
            llHistoryPolyline.setVisible(true);
        } else {
            if(llHistoryPolyline != null) {
                llHistoryPolyline.setVisible(false);
            }
        }
    }

    public void showRoutePath(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked()) {
            llRoutePolyline.setVisible(true);
        } else {
            llRoutePolyline.setVisible(false);
        }
    }

    public void showAddress(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked()) {
                addressText.setVisibility(View.VISIBLE);
        } else {
            addressText.setVisibility(View.INVISIBLE);
        }
    }


    private void checkLocationAccuracy() {

        Log.d(TAG, "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: High Accuracy Already Present");
            initMap();
        });

        /// ADD IN PROMPT IF LOCATION ACCURACY NOT SET - REQUEST?

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            initMap();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public void setRoutePath(JSONArray routeData){

        try {
            for(int i = 0; i < routeData.length(); i++){
                String currPair = routeData.getString(i);
                String[] coords = currPair.split(",");

                double lat = Double.parseDouble(coords[1]);
                double lng = Double.parseDouble(coords[0]);

                LatLng latLng = new LatLng(lat, lng);
                latLonPath.add(latLng); // Add the LL to our location history

            }
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonPath) {
                polylineOptions.add(ll);
            }

            llRoutePolyline = mMap.addPolyline(polylineOptions);
            llRoutePolyline.setEndCap(new RoundCap());
            llRoutePolyline.setWidth(8);
            llRoutePolyline.setColor(Color.GREEN);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (fullScreen) {
                hideSystemUI();
            } else {
                showSystemUI();
            }
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}