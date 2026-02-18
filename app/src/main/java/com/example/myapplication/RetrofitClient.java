package com.example.myapplication;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * מחלקת עזר (Utility Class) לניהול תקשורת ה-HTTP באפליקציה.
 * המחלקה משתמשת בספריית Retrofit כדי לבצע קריאות ל-REST API של גוגל בצורה יעילה.
 */
public class RetrofitClient {

    // משתנה סטטי המחזיק את המופע היחיד של אובייקט ה-Retrofit בזיכרון (Singleton Pattern).
    private static Retrofit retrofit = null;

    /**
     * פונקציה המחזירה מופע מוכן לעבודה של GeocodingApiService.
     * הפונקציה מבצעת "אתחול עצל" (Lazy Initialization) - יוצרת את האובייקט רק בפעם הראשונה שצריך אותו.
     */
    public static GeocodingApiService getApiService() {
        if (retrofit == null) {
            // שימוש ב-Builder Pattern כדי לבנות את אובייקט ה-Retrofit עם ההגדרות הנדרשות.
            retrofit = new Retrofit.Builder()
                    // כתובת הבסיס (Base URL) של ה-API של גוגל. כל ה-Endpoints יתווספו לסוף כתובת זו.
                    .baseUrl("https://maps.googleapis.com/")
                    // הוספת ConverterFactory מסוג GSON. רכיב זה אחראי להפוך אוטומטית את ה-JSON
                    // שמגיע מהשרת לאובייקטי Java (POJO) שהגדרנו.
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        // יצירת מימוש (Implementation) של ה-Interface שהגדרנו בזמן ריצה.
        return retrofit.create(GeocodingApiService.class);
    }
}