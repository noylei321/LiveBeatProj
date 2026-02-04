package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private ArrayList<Request> dataSet;
    private String showId;

    // הבנאי מקבל את רשימת הבקשות ואת ה-ID של ההופעה הנוכחית
    public RequestsAdapter(ArrayList<Request> dataSet, String showId) {
        this.dataSet = dataSet;
        this.showId = showId;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ניפוח העיצוב של שורת בקשה בודדת
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = dataSet.get(position);

        // הצגת הנתונים ב-UI
        holder.tvContent.setText(request.getContent());
        holder.tvSender.setText("מאת: " + request.getSenderName());

        // מאזין ללחיצה ארוכה - מחיקה מה-Firebase
        holder.itemView.setOnLongClickListener(v -> {
            String rid = request.getRequestId();
            if (rid != null && showId != null) {
                deleteRequestFromFirebase(rid, v);
            } else {
                Toast.makeText(v.getContext(), "שגיאה: חסר מזהה בקשה", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void deleteRequestFromFirebase(String requestId, View view) {
        // גישה ישירה לבקשה הספציפית תחת ההופעה הספציפית
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Requests")
                .child(showId)
                .child(requestId);

        ref.removeValue().addOnSuccessListener(aVoid -> {
            // הערה: אין צורך למחוק ידנית מה-dataSet,
            // ה-ValueEventListener בפרגמנט יזהה את המחיקה ויעדכן את הרשימה לבד!
            Toast.makeText(view.getContext(), "השיר בוצע והוסר מהרשימה", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(view.getContext(), "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return dataSet != null ? dataSet.size() : 0;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvSender;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור לפקדים מה-XML (item_request.xml)
            tvContent = itemView.findViewById(R.id.tvRequestContent);
            tvSender = itemView.findViewById(R.id.tvSenderName);
        }
    }
}