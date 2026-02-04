package com.example.myapplication;

import java.util.ArrayList;

public class MyData {
    // פונקציה שמחזירה בקשות דמה - מופרדת לחלוטין
    public static ArrayList<Request> getFakeRequests() {
        ArrayList<Request> list = new ArrayList<>();

        // 3 פרמטרים: requestId, content, senderName
        list.add(new Request("1", "אפשר בבקשה את 'Golden Boy'?", "שיר"));
        list.add(new Request("2", "תשיר משהו של סטטיק ובן אל", "עומר"));
        list.add(new Request("3", "בא לנו שיר קצבי!", "מיכל"));

        return list;
    }
}