package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * ממשק (Interface) המגדיר את הקריאה ל-Google Places Autocomplete API.
 * Retrofit משתמש במחלקה זו כדי לייצר את הקוד שמבצע את הבקשה לרשת.
 */
public interface PlacesApiService {

    /**
     * פונקציה המבצעת בקשת GET לקבלת הצעות לכתובות בזמן אמת.
     * * @GET - מציין את הנתיב היחסי (Endpoint) של שירות ההשלמה האוטומטית של גוגל.
     * @Query("input") - הטקסט החלקי שהמשתמש הקליד (למשל "בן גו").
     * @Query("key") - מפתח ה-API לאימות מול גוגל.
     * @Query("language") - קביעת שפת התוצאות (למשל "he" לעברית).
     * @Query("components") - הגבלת התוצאות לאזור גיאוגרפי (למשל "country:il" לישראל בלבד).
     * * @return Call<PlacesAutoResponse> - אובייקט Call המנהל את הבקשה ומבצע המרה של ה-JSON
     * שחוזר לאובייקט ה-Java שיצרנו בשם PlacesAutoResponse.
     */
    @GET("maps/api/place/autocomplete/json")
    Call<PlacesAutoResponse> autocomplete(
            @Query("input") String input,
            @Query("key") String apiKey,
            @Query("language") String language,
            @Query("components") String components
    );
}