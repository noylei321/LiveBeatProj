package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * ממשק (Interface) המגדיר את נקודות הקצה (Endpoints) מול ה-Geocoding API של גוגל.
 * ספריית Retrofit משתמשת בממשק זה כדי לייצר בזמן ריצה את הקוד שמבצע את קריאות ה-HTTP בפועל.
 */
public interface GeocodingApiService {

    /**
     * פונקציה המבצעת בקשת GET לכתובת היחסית המצוינת ב-Annotation.
     * * @GET - מציין שמדובר בבקשת HTTP מסוג GET לשליפת נתונים.
     * @Query("address") - מוסיף פרמטר לשאילתה ב-URL (למשל: ?address=Tel+Aviv).
     * @Query("key") - מוסיף את מפתח ה-API לצרכי אימות מול גוגל.
     * * @return Call<GeocodeResponse> - מחזיר אובייקט מסוג Call שמנהל את הבקשה האסינכרונית
     * ומבצע המרה אוטומטית (Deserialization) של תשובת ה-JSON לאובייקט GeocodeResponse.
     */
    @GET("maps/api/geocode/json")
    Call<GeocodeResponse> getCoordinates(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}