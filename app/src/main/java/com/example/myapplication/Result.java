package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("geometry")
    private Geometry geometry;

    public Geometry getGeometry() { return geometry; }
}