package com.example.myapplication;

public class User {
    private String fullName;
    private String username; // 🔹 עקביות: username באותיות קטנות לסנכרון מול ה-DB
    private String userId;
    private String email;
    private String phone;
    private String birthDate;
    private String genre;
    private String bio; // 🔹 שדה רשות
    private String userType; // לזיהוי: audience
    private String profileImageUrl;

    // 1. בנאי ריק חובה עבור Firebase
    public User() {
    }

    // 2. בנאי מלא (סוג המשתמש נקבע כאן אוטומטית)
    public User(String fullName, String username, String email, String phone, String birthDate, String genre, String bio, String profileImageUrl) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.genre = genre;
        this.bio = bio != null ? bio.trim() : "";
        this.profileImageUrl = profileImageUrl;

        // 🔹 כאן אנחנו קובעים שזה בליין באופן אוטומטי - אין סוגים שונים
        this.userType = "audience";
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}