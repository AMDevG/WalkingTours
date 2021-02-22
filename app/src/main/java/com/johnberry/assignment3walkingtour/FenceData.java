package com.johnberry.assignment3walkingtour;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class FenceData implements Serializable {

    private final String id;
    private final double lat;
    private final double lon;
    private final String address;
    private final float radius;
//    private final int type;
    private final String fenceColor;

    FenceData(String id, double lat, double lon, String address, float radius, String fenceColor) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.radius = radius;

        this.fenceColor = fenceColor;
    }

    String getId() {
        return id;
    }

    String getAddress() {
        return address;
    }

    float getRadius() {
        return radius;
    }



    String getFenceColor() {
        return fenceColor;
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }


    @NonNull
    @Override
    public String toString() {
        return "FenceData{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", radius=" + radius +
                ", fenceColor='" + fenceColor + '\'' +
                '}';
    }
}
