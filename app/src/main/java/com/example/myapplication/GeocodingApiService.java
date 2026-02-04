package com.example.myapplication;
import com.example.myapplication.GeocodeResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface GeocodingApiService {
    @GET("maps/api/geocode/json")
    Call<GeocodeResponse> getCoordinates(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}