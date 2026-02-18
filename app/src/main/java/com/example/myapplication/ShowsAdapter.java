package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * אדפטור לניהול רשימת המופעים של האמן.
 * כולל לוגיקה לחישוב סטטוס המופע, עדכון מונים בזמן אמת ומחיקה מ-Firebase.
 */
public class ShowsAdapter extends RecyclerView.Adapter<ShowsAdapter.ShowViewHolder> {

    private ArrayList<Show> showsList;

    public ShowsAdapter(ArrayList<Show> showsList) {
        this.showsList = showsList;
    }

    // יצירת ה-ViewHolder על ידי ניפוח ה-XML של פריט הרשימה (item_show).
    @NonNull
    @Override
    public ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
        return new ShowViewHolder(view);
    }

    // חיבור הנתונים לרכיבי ה-UI בכל שורה ברשימה.
    @Override
    public void onBindViewHolder(@NonNull ShowViewHolder holder, int position) {
        Show show = showsList.get(position);
        String showId = show.getShowId();

        holder.tvLocation.setText(show.getLocation());
        holder.tvDate.setText(show.getDate());
        holder.tvTime.setText(show.getTime());
        holder.tvGenre.setText(show.getGenre());

        // קריאה לפונקציית עזר לניהול הצבעים והסטטוס הוויזואלי של המופע.
        updateStatusUI(holder, show);

        // שימוש ב-ValueEventListener בתוך האדפטור:
        // מאפשר לכל כרטיסייה (Card) לעדכן את מונה הלייקים והדיסלייקים שלה בצורה עצמאית בזמן אמת.
        DatabaseReference reactionsRef = FirebaseDatabase.getInstance().getReference("Reactions").child(showId);
        reactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likes = 0, dislikes = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String val = ds.getValue(String.class);
                    if ("like".equals(val)) likes++;
                    else if ("dislike".equals(val)) dislikes++;
                }
                holder.tvLikes.setText(String.valueOf(likes));
                holder.tvDislikes.setText(String.valueOf(dislikes));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // ניהול ניווט: לחיצה על המופע מעבירה את האמן למסך ה-Live או הסיכום.
        // אנו מחשבים האם המופע נחשב כהיסטורי (עברו מעל 4 שעות מתחילתו) לצורך הצגת הנתונים הנכונה.
        holder.itemView.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("showId", showId);

            boolean isPast = false;
            try {
                long currentTime = System.currentTimeMillis();
                long startTime = convertTimeToMillis(show.getDate(), show.getTime());
                // אם עברו 4 שעות (14,400,000 מילישניות), המופע נסגר אוטומטית.
                if (currentTime > (startTime + 14400000)) isPast = true;
            } catch (Exception e) { e.printStackTrace(); }

            b.putBoolean("isHistorical", isPast);
            Navigation.findNavController(v).navigate(R.id.artistLiveFragment, b);
        });

        // ניהול מחיקה: הפעלת AlertDialog לאישור המשתמש לפני הסרת הנתונים מ-Firebase.
        // המחיקה היא "מפליית" (Cascading) - מוחקת גם את המופע וגם את הבקשות המשויכות אליו.
        holder.btnDeleteShow.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("ביטול הופעה")
                    .setMessage("האם למחוק את " + show.getLocation() + "?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                        db.child("Shows").child(show.getShowId()).removeValue();
                        db.child("Requests").child(show.getShowId()).removeValue();
                        Toast.makeText(v.getContext(), "ההופעה בוטלה", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("לא", null)
                    .show();
        });

        setArtistIcon(holder, show.getArtistType());
    }

    // ניהול הלוגיקה הוויזואלית של הסטטוס: שינוי צבעים ושקיפות (Alpha) בהתאם למצב המופע.
    private void updateStatusUI(ShowViewHolder holder, Show show) {
        long currentTime = System.currentTimeMillis();
        try {
            long startTime = convertTimeToMillis(show.getDate(), show.getTime());

            if (show.isLive()) {
                holder.tvStatus.setText("🔴 LIVE");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                holder.itemView.setAlpha(1.0f);
            } else if (currentTime > (startTime + 14400000)) {
                holder.tvStatus.setText("הופעת עבר");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                // שינוי שקיפות להבחנה ויזואלית קלה בין הופעות פעילות להיסטוריות.
                holder.itemView.setAlpha(0.6f);
            } else {
                holder.tvStatus.setText("קרוב");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.itemView.setAlpha(1.0f);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // התאמת האייקון בצורה דינמית לפי סוג האמן שנשמר ב-Database.
    private void setArtistIcon(ShowViewHolder holder, String type) {
        if ("DJ".equals(type)) holder.imgShowTypeIcon.setImageResource(R.drawable.ic_dj);
        else if ("Comedian".equals(type) || "קומיקאי".equals(type)) holder.imgShowTypeIcon.setImageResource(R.drawable.ic_standup);
        else holder.imgShowTypeIcon.setImageResource(R.drawable.ic_singer);
    }

    // פונקציית עזר להמרת מחרוזות תאריך וזמן לערך מספרי במילישניות לצורך השוואה וחישובים.
    private long convertTimeToMillis(String date, String time) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date mDate = sdf.parse(date + " " + time);
        return mDate != null ? mDate.getTime() : 0;
    }

    @Override
    public int getItemCount() { return showsList != null ? showsList.size() : 0; }

    // ViewHolder המחזיק את הרפרנסים לרכיבי ה-UI של הכרטיסייה.
    public static class ShowViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvDate, tvTime, tvGenre, tvStatus;
        TextView tvLikes, tvDislikes;
        ImageView imgShowTypeIcon, btnDeleteShow;

        public ShowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvShowLocation);
            tvDate = itemView.findViewById(R.id.tvShowDate);
            tvTime = itemView.findViewById(R.id.tvShowTime);
            tvGenre = itemView.findViewById(R.id.tvShowGenre);
            tvStatus = itemView.findViewById(R.id.tvShowStatus);
            tvLikes = itemView.findViewById(R.id.tvShowCardLikes);
            tvDislikes = itemView.findViewById(R.id.tvShowCardDislikes);
            imgShowTypeIcon = itemView.findViewById(R.id.imgShowTypeIcon);
            btnDeleteShow = itemView.findViewById(R.id.btnDeleteShow);
        }
    }
}