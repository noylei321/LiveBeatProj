package com.example.myapplication;

public class Artist {
    private String fullName = "";
    private String stageName = "";
    private String birthDate = "";
    private String instrument = "";
    private String email = "";
    private String userName = "";
    private String phone = "";
    private String instaLink = "";
    private String bio = "";
    private String genre = "";
    private String userType = "artist";
    private String profileImageUrl = "";

    // חובה עבור Firebase
    public Artist() { }

    // בנאי מלא
    public Artist(String fullName, String stageName, String birthDate, String instrument,
                  String email, String userName, String phone, String instaLink,
                  String bio, String genre, String profileImageUrl) {

        this.fullName = safe(fullName);
        this.stageName = safe(stageName);
        this.birthDate = safe(birthDate);
        this.instrument = safe(instrument);
        this.email = safe(email);
        this.userName = safe(userName);
        this.phone = safe(phone);
        this.instaLink = safe(instaLink);
        this.bio = safe(bio);
        this.genre = safe(genre);
        this.profileImageUrl = safe(profileImageUrl);
        this.userType = "artist";
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = safe(fullName); }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = safe(stageName); }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = safe(birthDate); }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = safe(instrument); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = safe(email); }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = safe(userName); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = safe(phone); }

    public String getInstaLink() { return instaLink; }
    public void setInstaLink(String instaLink) { this.instaLink = safe(instaLink); }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = safe(bio); }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = safe(genre); }

    public String getUserType() { return userType; }
    public void setUserType(String userType) {
        this.userType = (userType == null || userType.trim().isEmpty()) ? "artist" : userType.trim();
    }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = safe(profileImageUrl); }

    // עזר ל-UI: מה להציג כשם ראשי
    public String getDisplayName() {
        if (stageName != null && !stageName.trim().isEmpty()) return stageName.trim();
        if (fullName != null && !fullName.trim().isEmpty()) return fullName.trim();
        return "";
    }
}
