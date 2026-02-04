package com.example.myapplication;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodeResponse {
    @SerializedName("results")
    private List<Result> results;

    public List<Result> getResults() { return results; }
}