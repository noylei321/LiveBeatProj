package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ShowsAdapter extends RecyclerView.Adapter<ShowsAdapter.ShowViewHolder> {

    private ArrayList<Show> showsList;

    public ShowsAdapter(ArrayList<Show> showsList) {
        this.showsList = showsList;
    }

    @NonNull
    @Override
    public ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מחבר את ה-XML של השורה הבודדת (item_show)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
        return new ShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowViewHolder holder, int position) {
        Show show = showsList.get(position);

        // כאן אנחנו מזריקים את הנתונים האמיתיים לתוך ה-XML
        holder.tvLocation.setText(show.getLocation());
        holder.tvDate.setText(show.getDate()); // הוספנו את התאריך!
        holder.tvTime.setText(show.getTime());
        holder.tvGenre.setText(show.getGenre());
    }

    @Override
    public int getItemCount() {
        return showsList.size();
    }

    public static class ShowViewHolder extends RecyclerView.ViewHolder {
        // הוספנו tvDate לרשימת הרכיבים
        TextView tvLocation, tvDate, tvTime, tvGenre;

        public ShowViewHolder(@NonNull View itemView) {
            super(itemView);
            // כאן אנחנו מחברים בין ה-ID ב-XML למשתנה בג'אווה
            tvLocation = itemView.findViewById(R.id.tvShowLocation);
            tvDate = itemView.findViewById(R.id.tvShowDate); // הוספנו את החיבור לתאריך!
            tvTime = itemView.findViewById(R.id.tvShowTime);
            tvGenre = itemView.findViewById(R.id.tvShowGenre);
        }
    }
}