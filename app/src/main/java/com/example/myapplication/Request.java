package com.example.myapplication;

public class Request {
    private String requestId;
    private String content;
    private String senderName;
    private long timestamp;
    private boolean played; // 🔹 חשוב: הוספתי את זה כדי שהבקשות לא יימחקו אלא רק ישנו סטטוס

    // פיירבייס חייב קונסטרקטור ריק כדי לעבוד
    public Request() { }

    public Request(String requestId, String content, String senderName, long timestamp, boolean played) {
        this.requestId = requestId;
        this.content = content;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.played = played;
    }

    // Getters ו-Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isPlayed() { return played; } // 🔹 לבדוק את זה באדפטר כדי לצבוע בירוק
    public void setPlayed(boolean played) { this.played = played; }
}