package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * מחלקה זו מייצגת את מבנה ה-JSON שחוזר מ-Google Places Autocomplete API.
 * היא משמשת כמודל נתונים (DTO) עבור ספריית GSON לצורך דה-סריאליזציה של התשובה מהשרת.
 */
public class PlacesAutoResponse {

    // סטטוס התגובה (למשל "OK", "ZERO_RESULTS", "REQUEST_DENIED").
    // חשוב לבדיקת תקינות הקריאה לפני הגישה לנתונים.
    @SerializedName("status")
    private String status;

    // הודעת שגיאה מפורטת שגוגל מחזירה במידה ומשהו השתבש (למשל בעיית הרשאות ב-API Key).
    @SerializedName("error_message")
    private String errorMessage;

    // רשימה של אובייקטי Prediction. כל אובייקט כזה מייצג הצעה אחת לכתובת.
    @SerializedName("predictions")
    private List<Prediction> predictions;

    /**
     * מחזירה את סטטוס התגובה מגוגל.
     */
    public String getStatus() { return status; }

    /**
     * מחזירה את הודעת השגיאה במידה וקיימת.
     */
    public String getErrorMessage() { return errorMessage; }

    /**
     * מחזירה את רשימת התחזיות (הצעות הכתובת) שהתקבלו.
     */
    public List<Prediction> getPredictions() { return predictions; }

    /**
     * מחלקה פנימית סטטית (Static Inner Class) המייצגת הצעה בודדת בתוך רשימת ה-predictions.
     * שימוש במחלקה פנימית מאפשר לנו לשמור על מבנה היררכי התואם למבנה ה-JSON המקונן.
     */
    public static class Prediction {

        // התיאור המלא של הכתובת כפי שיוצג למשתמש ברשימת ה-Autocomplete.
        @SerializedName("description")
        private String description;

        /**
         * מחזירה את מחרוזת הטקסט של הכתובת המוצעת.
         */
        public String getDescription() { return description; }
    }
}