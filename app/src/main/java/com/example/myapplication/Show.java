package com.example.myapplication;

public class Show {
    private String showId;
    private String artistId;
    private String stageName;
    private String artistType; // DJ / Musician / Comedian
    private String location;
    private String time;
    private String genre;
    private String date;
    private double latitude;
    private double longitude;
    private boolean live; // 🔹 תזכורת: לקרוא לזה 'live' כדי שיתאים לעדכונים מהדאשבורד

    // חובה בשביל Firebase
    public Show() { }

    public Show(String showId, String artistId, String stageName, String artistType, String location, String time, String genre, String date, double latitude, double longitude) {
        this.showId = showId;
        this.artistId = artistId;
        this.stageName= stageName;
        this.artistType = artistType;
        this.location = location;
        this.time = time;
        this.genre = genre;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.live = false;
    }

    // Getters ו-Setters
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public String getArtistId() { return artistId; }
    public void setArtistId(String artistId) { this.artistId = artistId; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getArtistType() { return artistType; }
    public void setArtistType(String artistType) { this.artistType = artistType; }

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

    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }
}