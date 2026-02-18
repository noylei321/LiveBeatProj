package com.example.myapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * מחלקה זו משמשת כ-Client ייעודי עבור Google Places API.
 * היא מיישמת את תבנית העיצוב Singleton כדי להבטיח שקיים רק מופע אחד של אובייקט ה-Retrofit בזיכרון.
 */
public class RetrofitPlacesClient {

    // משתנה סטטי השומר את ה-Instance של Retrofit.
    // הגישה אליו היא private כדי למנוע שינויים חיצוניים (Encapsulation).
    private static Retrofit retrofit;

    /**
     * פונקציה סטטית (Static Factory Method) המחזירה את ה-Interface של ה-API.
     * הפונקציה משתמשת ב-Lazy Initialization (אתחול עצל) - האובייקט נוצר רק בקריאה הראשונה.
     */
    public static PlacesApiService getApiService() {
        if (retrofit == null) {
            // שימוש ב-Builder Pattern להגדרת ה-Client:
            retrofit = new Retrofit.Builder()
                    // הגדרת Base URL - כתובת הבסיס לכל הקריאות של גוגל מפות.
                    .baseUrl("https://maps.googleapis.com/")
                    // הזרקת GsonConverterFactory - רכיב זה אחראי על ה-Deserialization,
                    // כלומר הפיכה אוטומטית של ה-JSON הגולמי לאובייקטי Java (POJO).
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        // יצירת מימוש (Implementation) של ה-Interface שהגדרנו בזמן ריצה.
        // זהו לב ה-Retrofit: הוא "מזריק" את הלוגיקה של ה-HTTP לתוך המתודות שהצהרנו עליהן ב-PlacesApiService.
        return retrofit.create(PlacesApiService.class);
    }
}