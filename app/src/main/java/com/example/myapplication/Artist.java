package com.example.myapplication;

public class Artist {
    private String fullName = "";
    private String stageName = "";
    private String artistSubCategory = ""; // DJ / Musician / Comedian
    private String birthDate = "";
    private String instrument = "";
    private String email = "";
    private String username = "";
    private String phone = "";
    private String socialLink = "";
    private String bio = "";
    private String genres = ""; // 🔹 שונה ל-genres (מכיל מחרוזת מופרדת בפסיקים)
    private String userType = "artist";
    private String profileImageUrl = "";

    public Artist() { }

    public Artist(String fullName, String stageName, String artistSubCategory, String birthDate,
                  String instrument, String email, String username, String phone,
                  String socialLink, String bio, String genres, String profileImageUrl) {

        this.fullName = safe(fullName);
        this.stageName = safe(stageName);
        this.artistSubCategory = safe(artistSubCategory);
        this.birthDate = safe(birthDate);
        this.instrument = safe(instrument);
        this.email = safe(email);
        this.username = safe(username);
        this.phone = safe(phone);
        this.socialLink = safe(socialLink);
        this.bio = safe(bio);
        this.genres = safe(genres); // 🔹 עודכן
        this.profileImageUrl = safe(profileImageUrl);
        this.userType = "artist";
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = safe(fullName); }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = safe(stageName); }

    public String getArtistSubCategory() { return artistSubCategory; }
    public void setArtistSubCategory(String artistSubCategory) { this.artistSubCategory = safe(artistSubCategory); }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = safe(birthDate); }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = safe(instrument); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = safe(email); }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = safe(username); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = safe(phone); }

    public String getSocialLink() { return socialLink; }
    public void setSocialLink(String socialLink) { this.socialLink = safe(socialLink); }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = safe(bio); }

    public String getGenres() { return genres; } // 🔹 עודכן
    public void setGenres(String genres) { this.genres = safe(genres); } // 🔹 עודכן

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = safe(userType); }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = safe(profileImageUrl); }

    public String getDisplayName() {
        if (!stageName.isEmpty()) return stageName;
        return fullName;
    }
}