package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class LikersAdapter extends RecyclerView.Adapter<LikersAdapter.LikerViewHolder> {

    private ArrayList<User> likersList;
    private Context context;

    // בנאי (Constructor) המקבל את רשימת הנתונים והקונטקסט.
    // שימוש בקונטקסט כאן חיוני עבור ה-LayoutInflater ועבור ספריית Glide שזקוקה למחזור החיים של האפליקציה.
    public LikersAdapter(Context context, ArrayList<User> likersList) {
        this.context = context;
        this.likersList = likersList;
    }

    // מתודה המופעלת על ידי ה-RecyclerView ליצירת מופע חדש של ה-ViewHolder.
    // הפונקציה "מנפחת" (Inflate) את קובץ ה-XML של השורה הבודדת (item_liker) והופכת אותו לאובייקט View בזיכרון.
    @NonNull
    @Override
    public LikerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_liker, parent, false);
        return new LikerViewHolder(view);
    }

    // המתודה המרכזית האחראית על חיבור הנתונים (Data Binding) לרכיבי ה-UI בשורה ספציפית.
    // היא נקראת בכל פעם ששורה נכנסת לתצוגה במהלך הגלילה.
    @Override
    public void onBindViewHolder(@NonNull LikerViewHolder holder, int position) {
        User user = likersList.get(position);

        holder.tvName.setText("@" + user.getUsername());

        // שימוש בספריית Glide לטעינה אופטימלית של תמונת הפרופיל.
        // circleCrop() הוא Transform ויזואלי המבוצע ברמת ה-Bitmap לפני ההצגה, מה שחוסך משאבי עיבוד ב-UI.
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // הגדרת מאזין לחיצה (Click Listener) על כל השורה.
        // כאן מיושמת לוגיקת הניווט המעבירה את ה-ID של המשתמש ודגל בוליאני (fromArtistLive)
        // המאפשר לפרגמנט היעד לדעת מהו מקור הניווט ולהתאים את כפתור ה-"חזור" בהתאם.
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUserId());
            bundle.putBoolean("fromArtistLive", true);

            // שימוש ב-Navigation Component לביצוע המעבר בצורה מובנית ב-Navigation Graph.
            Navigation.findNavController(v).navigate(R.id.userProfileFragment, bundle);
        });
    }

    // מחזירה את כמות הפריטים ברשימה. ה-RecyclerView משתמש בזה כדי לדעת כמה שורות לייצר.
    @Override
    public int getItemCount() {
        return likersList != null ? likersList.size() : 0;
    }

    /**
     * מימוש ה-ViewHolder Pattern.
     * תפקידו להחזיק הפניות (References) קבועות לרכיבי ה-View בשורה.
     * זה מונע קריאות חוזרות ויקרות ל-findViewById בזמן גלילה, מה שמשפר משמעותית את ה-FPS של האפליקציה.
     */
    public static class LikerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvName;

        public LikerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgLikerProfile);
            tvName = itemView.findViewById(R.id.tvLikerUsername);
        }
    }
}