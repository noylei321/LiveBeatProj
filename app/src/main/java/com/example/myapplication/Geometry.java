package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

public class Geometry {
    @SerializedName("location")
    private LocationModel location;

    public LocationModel getLocation() { return location; }
}