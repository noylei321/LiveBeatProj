package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

/**
 * מחלקה המייצגת תוצאה בודדת בתוך רשימת התוצאות של Google Geocoding API.
 * משמשת כחוליית קישור במבנה הנתונים המקונן של ה-Response.
 */
public class Result {

    // קישור המפתח "geometry" מה-JSON לאובייקט ה-Geometry שלנו.
    // אובייקט ה-Geometry מכיל בתוכו את המידע על המיקום הפיזי (קווי אורך ורוחב).
    @SerializedName("geometry")
    private Geometry geometry;

    /**
     * מחזירה את אובייקט הגיאומטריה של התוצאה הנוכחית.
     * דרך אובייקט זה ניתן לחלץ את הקואורדינטות (Lat/Lng) שיוצגו על המפה.
     */
    public Geometry getGeometry() {
        return geometry;
    }
}