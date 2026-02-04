package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class fragment_artist_live extends Fragment {

    private ArrayList<Request> dataSet;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RequestsAdapter adapter;

    // משתנים לחיבור ל-Firebase
    private DatabaseReference requestsRef;
    private String showId;

    public fragment_artist_live() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // קבלת ה-ID של ההופעה שהעברנו מהדאשבורד
        if (getArguments() != null) {
            showId = getArguments().getString("showId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_live, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול הנתונים
        dataSet = new ArrayList<>();

        // 2. מציאת ה-RecyclerView
        recyclerView = view.findViewById(R.id.rvLiveRequests);

        // 3. הגדרת ה-LayoutManager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        // 4. יצירת האדפטר (מעבירים גם את ה-showId לצורך מחיקה)
        adapter = new RequestsAdapter(dataSet, showId);

        // 5. חיבור האדפטר
        recyclerView.setAdapter(adapter);

        // 6. חיבור ל-Firebase והאזנה לבקשות בזמן אמת
        if (showId != null) {
            requestsRef = FirebaseDatabase.getInstance().getReference("Requests").child(showId);
            listenForRequests();
        }
    }

    private void listenForRequests() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataSet.clear(); // מנקים את הרשימה הישנה
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Request request = ds.getValue(Request.class);
                    if (request != null) {
                        dataSet.add(request); // מוסיפים את הבקשות החדשות
                    }
                }
                adapter.notifyDataSetChanged(); // מעדכנים את המסך מיד!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}