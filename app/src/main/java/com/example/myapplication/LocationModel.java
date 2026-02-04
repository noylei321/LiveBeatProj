package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

public class LocationModel {
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public double getLat() { return lat; }
    public double getLng() { return lng; }
}