package com.example.myapplication;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * מחלקה זו מייצגת את המבנה העליון של התשובה (Response) החוזרת מ-Google Geocoding API.
 * היא משמשת את ספריית Retrofit כדי להפוך את ה-JSON שמתקבל מהשרת לאובייקט Java נגיש.
 */
public class GeocodeResponse {

    // אנו משתמשים באנוטציה @SerializedName כדי לקשר בין המפתח "results" שמופיע ב-JSON
    // לבין המשתנה results בתוך הקוד שלנו. זה הכרחי כי GSON צריך לדעת לאן למפות את הנתונים.
    @SerializedName("results")
    private List<Result> results;

    /**
     * מחזירה את רשימת התוצאות שנמצאו עבור הכתובת שנשלחה.
     * גוגל מחזירה רשימה כי לעיתים יש מספר התאמות לאותה מחרוזת טקסט.
     */
    public List<Result> getResults() {
        return results;
    }
}