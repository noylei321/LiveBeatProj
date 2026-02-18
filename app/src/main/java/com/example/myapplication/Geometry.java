package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

/**
 * מחלקה זו מייצגת את אובייקט ה-"geometry" בתוך התשובה של Google Geocoding.
 * תפקידה המרכזי הוא להוות מעטפת (Wrapper) לנתוני המיקום המדויקים.
 */
public class Geometry {

    // השימוש ב-@SerializedName מבטיח שגם אם גוגל תשנה את מבנה ה-JSON בעתיד,
    // נוכל לעדכן רק את המחרוזת כאן מבלי לשנות את שמות המשתנים בכל האפליקציה.
    @SerializedName("location")
    private LocationModel location;

    /**
     * פונקציה המחזירה את אובייקט ה-LocationModel.
     * אובייקט זה מכיל את הנתונים הסופיים שאנחנו צריכים: קו אורך (lng) וקו רוחב (lat).
     */
    public LocationModel getLocation() {
        return location;
    }
}