package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ShowRequestsUser extends Fragment {

    private RecyclerView rvRequests;
    private RequestsAdapter adapter;
    private ArrayList<Request> requestList;
    private DatabaseReference requestsRef;
    private String showId;
    private String artistId;
    private TextView tvNoRequests;
    private ImageView imgArtistProf;
    private TextView tvArtistStageNa, tvArtistUserna;

    // רכיבי ניהול הריאקציות (Likes/Dislikes)
    private DatabaseReference reactionsRef;
    private ImageView imgLike, imgDislike;
    private TextView tvLikeCount, tvDislikeCount;
    private String userReaction = "none"; // ניהול מצב (State) מקומי לסנכרון מול ה-UI
    private String uid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_show_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול רכיבי הממשק
        tvNoRequests = view.findViewById(R.id.tvNoRequestsUser);
        rvRequests = view.findViewById(R.id.rvRequests);
        imgArtistProf = view.findViewById(R.id.requestUserImgArtistProfile);
        tvArtistStageNa = view.findViewById(R.id.requestsUserTvArtistStageName);
        tvArtistUserna = view.findViewById(R.id.requestUserTvArtistUsername);

        // ניווט מותנה הקשר (Context-Aware Navigation):
        // לחיצה על שם המשתמש של האמן תעביר לפרופיל שלו עם דגל 'fromShowList'
        // כדי שהאפליקציה תדע לאן לחזור בלחיצה על 'Back'.
        tvArtistUserna.setOnClickListener(v -> {
            if (artistId != null) {
                Bundle b = new Bundle();
                b.putString("artistId", artistId);
                b.putBoolean("fromShowList", true);
                Navigation.findNavController(v).navigate(R.id.artistProfileFragment, b);
            } else {
                Toast.makeText(getContext(), "פרטי אמן לא זמינים", Toast.LENGTH_SHORT).show();
            }
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        imgLike = view.findViewById(R.id.imgLike);
        imgDislike = view.findViewById(R.id.imgDislike);
        tvLikeCount = view.findViewById(R.id.tvLikeCount);
        tvDislikeCount = view.findViewById(R.id.tvDislikeCount);

        // חילוץ נתונים מה-Arguments שהועברו מהמפה
        if (getArguments() != null) {
            showId = getArguments().getString("showId");
            artistId = getArguments().getString("artistId");
            String location = getArguments().getString("location");

            TextView tvTitle = view.findViewById(R.id.tvRequestsTitle);
            if (location != null) tvTitle.setText("בקשות במופע: " + location);

            loadArtistDetails();

            if (showId != null) {
                // הגדרת הרפרנס לצומת התגובות הספציפי למופע
                reactionsRef = FirebaseDatabase.getInstance().getReference("Reactions").child(showId);
                observeReactions(); // הפעלת ה-Real-time listener
            }
        }

        // הגדרת ה-RecyclerView והאדפטור
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList = new ArrayList<>();
        adapter = new RequestsAdapter(requestList, showId, false);
        rvRequests.setAdapter(adapter);

        loadRequestsFromFirebase();

        // כפתור מעבר למסך כתיבת בקשה חדשה
        view.findViewById(R.id.btnGoToSendRequest).setOnClickListener(v -> {
            if (showId != null) {
                Bundle b = new Bundle();
                b.putString("showId", showId);
                b.putString("artistId", artistId);
                Navigation.findNavController(v).navigate(R.id.action_showRequestsUser_to_sendRequestFragment, b);
            }
        });

        // ניהול הקלקה על לייק/דיסלייק
        view.findViewById(R.id.layoutLike).setOnClickListener(v -> toggleReaction("like"));
        view.findViewById(R.id.layoutDislike).setOnClickListener(v -> toggleReaction("dislike"));
    }

    // לוגיקת Toggle לניהול תגובות:
    // אם המשתמש לוחץ על אותו סוג תגובה שכבר קיים - היא מוסרת.
    // אם הוא לוחץ על סוג אחר - היא מעדכנת (דורסת) את הקודמת.
    private void toggleReaction(String type) {
        if (userReaction.equals(type)) {
            reactionsRef.child(uid).removeValue();
        } else {
            reactionsRef.child(uid).setValue(type);
        }
    }

    // מימוש ה-Observer Pattern של Firebase:
    // האזנה רציפה לצומת ה-Reactions. בכל פעם שמישהו (לא רק המשתמש הנוכחי) מגיב,
    // ה-onDataChange נקרא ומחשב מחדש את המונים (Aggregates) בזמן אמת.
    private void observeReactions() {
        reactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likes = 0;
                int dislikes = 0;
                userReaction = "none";

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String val = ds.getValue(String.class);
                    if ("like".equals(val)) likes++;
                    if ("dislike".equals(val)) dislikes++;

                    // זיהוי הבחירה האישית של המשתמש הנוכחי לצורך צביעת האייקון
                    if (ds.getKey().equals(uid)) {
                        userReaction = val;
                    }
                }

                tvLikeCount.setText(String.valueOf(likes));
                tvDislikeCount.setText(String.valueOf(dislikes));
                updateReactionIcons();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // עדכון המראה הוויזואלי של האייקונים (Visual Feedback).
    // שימוש ב-ColorFilter מאפשר לשנות צבע של Drawable קיים בצורה תוכנתית (Programmatically).
    private void updateReactionIcons() {
        if (!isAdded()) return;

        int activeColor = ContextCompat.getColor(requireContext(), R.color.beat_primary);
        int inactiveColor = Color.GRAY;
        int dislikeColor = Color.RED;

        imgLike.setColorFilter(userReaction.equals("like") ? activeColor : inactiveColor);
        imgDislike.setColorFilter(userReaction.equals("dislike") ? dislikeColor : inactiveColor);
    }

    // טעינת פרטי האמן לצורך הצגתם בראש המסך (Denormalization - שליפת נתונים מקושרים).
    private void loadArtistDetails() {
        if (artistId == null) return;

        FirebaseDatabase.getInstance().getReference("Artists").child(artistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String stageName = snapshot.child("stageName").getValue(String.class);
                            String username = snapshot.child("username").getValue(String.class);
                            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            if (stageName != null) tvArtistStageNa.setText(stageName);
                            if (username != null) tvArtistUserna.setText("@" + username);

                            if (imageUrl != null && getContext() != null) {
                                Glide.with(getContext()).load(imageUrl)
                                        .placeholder(R.drawable.ic_dj).into(imgArtistProf);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // האזנה לבקשות שירים:
    // אנו משתמשים ב-addValueEventListener כדי שהרשימה תתעדכן מעצמה ברגע שאחד הבליינים שולח בקשה.
    private void loadRequestsFromFirebase() {
        if (showId == null) return;
        requestsRef = FirebaseDatabase.getInstance().getReference("Requests").child(showId);
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Request r = ds.getValue(Request.class);
                    if (r != null) {
                        // הוספה במיקום 0 (LIFO - Last In First Out):
                        // הבקשה החדשה ביותר תופיע תמיד בראש הרשימה.
                        requestList.add(0, r);
                    }
                }

                // ניהול מצב "רשימה ריקה" (Empty State Handling) לשיפור חוויית המשתמש.
                if (requestList.isEmpty()) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                } else {
                    tvNoRequests.setVisibility(View.GONE);
                    rvRequests.scrollToPosition(0);
                }

                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}