package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * אדפטור (Adapter) לניהול רשימת הבקשות ב-RecyclerView.
 * תפקידו לקחת את אובייקטי ה-Request ולהזריק אותם לתוך ממשק המשתמש בצורה יעילה.
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private ArrayList<Request> dataSet;
    private String showId;
    private boolean isArtist;

    // בנאי המאתחל את האדפטור עם רשימת הנתונים, מזהה המופע הנוכחי,
    // ודגל 'isArtist' שקובע אילו רכיבי UI יהיו גלויים (למשל כפתור ה-"V").
    public RequestsAdapter(ArrayList<Request> dataSet, String showId, boolean isArtist) {
        this.dataSet = dataSet;
        this.showId = showId;
        this.isArtist = isArtist;
    }

    // פונקציה האחראית על יצירת ה-ViewHolder. היא "מנפחת" (Inflating) את ה-XML של שורת הבקשה הבודדת.
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    // הפונקציה המרכזית המחברת בין הנתונים ל-UI (Data Binding).
    // היא מעדכנת את הטקסטים, הזמנים, והעיצוב הוויזואלי של כל שורה ברשימה.
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = dataSet.get(position);

        // עדכון תוכן הבקשה ושם השולח.
        holder.tvContent.setText(request.getContent());
        holder.tvSender.setText("מאת: " + request.getSenderName());

        //   הוספת מאזין לחיצה למעבר לפרופיל הבליין
        holder.tvSender.setOnClickListener(v -> {
            if (request.getSenderId() != null) {
                Bundle b = new Bundle();
                b.putString("userId", request.getSenderId());
                b.putBoolean("fromArtistLive", true);
                Navigation.findNavController(v).navigate(R.id.userProfileFragment, b);
            }
        });

        // פורמט של הזמן: הפיכת ה-Timestamp (מילישניות) למחרוזת קריאה בפורמט HH:mm.
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(request.getTimestamp())));

        // ניהול מצבי תצוגה דינמיים: במידה והשיר כבר בוצע, העיצוב משתנה (מסגרת ירוקה ושקיפות).
        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (request.isPlayed()) {
            card.setStrokeWidth(4);
            card.setAlpha(0.5f);
            holder.btnDone.setVisibility(View.GONE);
        } else {
            card.setStrokeWidth(0);
            card.setAlpha(1.0f);
            // הצגת כפתור ה-"בוצע" רק אם המשתמש הוא האמן ורק אם השיר טרם נוגן.
            holder.btnDone.setVisibility(isArtist ? View.VISIBLE : View.GONE);
        }

        // מאזין ללחיצה על כפתור ה-"V" (בוצע).
        // במקום למחוק את הבקשה, אנחנו מעדכנים את השדה 'played' ב-Firebase Realtime Database.
        // בזכות ה-ValueEventListener בפרגמנט, המפה תתעדכן אוטומטית ברגע שהערך ישתנה בשרת.
        holder.btnDone.setOnClickListener(v -> {
            if (request.getRequestId() != null && showId != null) {
                FirebaseDatabase.getInstance().getReference("Requests")
                        .child(showId)
                        .child(request.getRequestId())
                        .child("played").setValue(true);
            }
        });
    }

    // מחזירה את מספר הבקשות הקיימות ברשימה.
    @Override
    public int getItemCount() { return dataSet != null ? dataSet.size() : 0; }

    /**
     * ViewHolder - מחלקה פנימית המייצגת את רכיבי הממשק של שורה אחת.
     * שימוש ב-ViewHolder חוסך קריאות findViewById יקרות ומבטיח גלילה חלקה.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvSender, tvTime;
        ImageView btnDone;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvRequestContent);
            tvSender = itemView.findViewById(R.id.tvSenderName);
            tvTime = itemView.findViewById(R.id.tvRequestTime);
            btnDone = itemView.findViewById(R.id.btnDoneRequest);
        }
    }
}