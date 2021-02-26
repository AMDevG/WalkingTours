package com.johnberry.assignment3walkingtour;

import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RoutePathDownloader implements Runnable {

    private static final String TAG = "FenceDataDownloader";
//    private final Geocoder geocoder;
//    private final FenceMgr fenceMgr;
    private MapsActivity mapsActivity;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";
    private JSONArray routePathArray = new JSONArray();

    RoutePathDownloader(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
    }
    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: Response code: " + connection.getResponseCode());
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            processData(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void processData(String result) {

        if (result == null)
            return;

        try {
            JSONObject jObj = new JSONObject(result);

            routePathArray = jObj.getJSONArray("path");

            mapsActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapsActivity.setRoutePath(routePathArray);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
