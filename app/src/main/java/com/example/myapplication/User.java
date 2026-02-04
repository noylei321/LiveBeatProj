package com.example.myapplication;

public class User {
    private String fullName;
    private String userName;
    private String email;
    private String phone;
    private String birthDate;
    private String genre;

    // הוספנו את התגית לזיהוי
    private String userType;

    // 2. בנאי ריק (חובה לפיירבייס)
    public User() {
    }

    // 3. בנאי מלא
    public User(String fullName, String userName, String email, String phone, String birthDate, String genre) {
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.genre = genre;

        // כאן אנחנו קובעים שזה בליין (קהל) באופן אוטומטי!
        this.userType = "audience";
    }

    // 4. Getters and Setters

    // --- Full Name ---
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // --- User Name ---
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    // --- Email ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // --- Phone ---
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // --- Birth Date ---
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    // --- Genre ---
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    // --- User Type (החדש!) ---
    // חובה להוסיף את אלה כדי שפיירבייס ישמור את הנתון
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}