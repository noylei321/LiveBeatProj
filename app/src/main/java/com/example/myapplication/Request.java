package com.example.myapplication;

public class Request {

    private String requestId;   // מזהה ייחודי (לצורך מחיקה וניהול)
    private String content;     // תוכן הבקשה
    private String senderName;  // שם השולח

    // 1. בנאי ריק (חובה בשביל פיירבייס!)
    public Request() {
    }

    // 2. בנאי מלא מעודכן
    public Request(String requestId, String content, String senderName) {
        this.requestId = requestId;
        this.content = content;
        this.senderName = senderName;
    }

    // Getters ו-Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}