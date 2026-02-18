package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

/**
 * מחלקה זו מייצגת את אובייקט המיקום (Location) הגיאוגרפי בתוך תשובת ה-JSON.
 * זהו המדרג הנמוך ביותר במבנה הנתונים שמתקבל מגוגל, והוא מכיל את הנקודות המדויקות על הגלובוס.
 */
public class LocationModel {

    // השימוש ב-double הכרחי כאן כיוון שקואורדינטות דורשות דיוק גבוה מאוד (Floating point precision)
    // כדי למקם את המרקר ברמת הרחוב והבית.

    // @SerializedName מקשר בין המפתח הקצר ב-JSON ("lat") לבין המשתנה שלנו.
    @SerializedName("lat")
    private double lat;

    // @SerializedName מקשר בין המפתח הקצר ב-JSON ("lng") לבין המשתנה שלנו.
    @SerializedName("lng")
    private double lng;

    /**
     * מחזירה את קו הרוחב (Latitude).
     * ערך זה נע בין 90- ל-90 ומציין את המיקום צפונית או דרומית לקו המשווה.
     */
    public double getLat() {
        return lat;
    }

    /**
     * מחזירה את קו האורך (Longitude).
     * ערך זה נע בין 180- ל-180 ומציין את המיקום מזרחית או מערבית לקו גריניץ'.
     */
    public double getLng() {
        return lng;
    }
}