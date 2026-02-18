package com.example.myapplication;

public class Artist {
    private String fullName = "";
    private String stageName = "";
    private String artistSubCategory = ""; // DJ / Musician / Comedian
    private String birthDate = "";
    private String instrument = "";
    private String email = "";
    private String username = ""; // 🔹 שונה מ-userName ל-username לעקביות עם ה-DB
    private String phone = "";
    private String socialLink = "";
    private String bio = "";
    private String genre = "";
    private String userType = "artist";
    private String profileImageUrl = "";

    // קונסטרקטור ריק חובה עבור Firebase
    public Artist() { }

    // בנאי מלא עבור יצירת אמן חדש
    public Artist(String fullName, String stageName, String artistSubCategory, String birthDate,
                  String instrument, String email, String username, String phone,
                  String socialLink, String bio, String genre, String profileImageUrl) {

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

    public String getArtistSubCategory() { return artistSubCategory; }
    public void setArtistSubCategory(String artistSubCategory) { this.artistSubCategory = safe(artistSubCategory); }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = safe(birthDate); }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = safe(instrument); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = safe(email); }

    public String getUsername() { return username; } // 🔹 עודכן
    public void setUsername(String username) { this.username = safe(username); } // 🔹 עודכן

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = safe(phone); }

    public String getSocialLink() { return socialLink; }
    public void setSocialLink(String socialLink) { this.socialLink = safe(socialLink); }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = safe(bio); }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = safe(genre); }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = safe(userType); }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = safe(profileImageUrl); }

    public String getDisplayName() {
        if (!stageName.isEmpty()) return stageName;
        return fullName;
    }
}