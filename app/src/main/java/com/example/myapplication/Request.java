package com.example.myapplication;

/**
 * מודל נתונים (POJO) המייצג בקשת שיר או תוכן בתוך האפליקציה.
 * המחלקה מותאמת לעבודה מול Firebase Realtime Database.
 */
public class Request {
    private String requestId;
    private String content;
    private String senderName;
    private String senderId; //  קריטי: המזהה הייחודי (UID) של המשתמש ששלח את הבקשה
    private long timestamp;
    private boolean played; // סטטוס לניהול ויזואלי באדפטור (מסומן ב-V או צבוע בירוק)

    /**
     * קונסטרקטור ריק - חובה עבור Firebase לצורך הפיכת JSON לאובייקט Java.
     */
    public Request() { }

    /**
     * קונסטרקטור מלא ליצירת בקשה חדשה.
     */
    public Request(String requestId, String content, String senderName, String senderId, long timestamp, boolean played) {
        this.requestId = requestId;
        this.content = content;
        this.senderName = senderName;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.played = played;
    }

    // --- Getters & Setters ---

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderId() { return senderId; } // 🔹 משמש למעבר לפרופיל המשתמש
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isPlayed() { return played; }
    public void setPlayed(boolean played) { this.played = played; }
}