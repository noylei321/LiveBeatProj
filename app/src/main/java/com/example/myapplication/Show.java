package com.example.myapplication;

public class Show {
    private String showId;
    private String artistId;
    private String location;
    private String time;
    private String genre;
    private String date;
    private double latitude;
    private double longitude;

    // השדה ששולט על הכפתור הסגול ועל הכניסה של הבליין
    private boolean isLive;

    // בנאי ריק חובה עבור Firebase
    public Show() {
    }

    // בנאי מלא מעודכן
    public Show(String showId, String artistId, String location, String time, String genre, String date, double latitude, double longitude) {
        this.showId = showId;
        this.artistId = artistId;
        this.location = location;
        this.time = time;
        this.genre = genre;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isLive = false; // ברירת מחדל: הופעה לא פעילה ברגע היצירה
    }

    // Getters ו-Setters
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public String getArtistId() { return artistId; }
    public void setArtistId(String artistId) { this.artistId = artistId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // פונקציות ה-Live החדשות שביקשת
    public boolean isLive() { return isLive; }
    public void setLive(boolean live) { isLive = live; }
}