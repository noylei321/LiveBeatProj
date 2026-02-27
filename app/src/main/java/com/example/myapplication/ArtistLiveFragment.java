package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ArtistLiveFragment extends Fragment {

    private ArrayList<Request> dataSet;
    private RecyclerView recyclerView;
    private RequestsAdapter adapter;
    private String showId;
    private boolean isHistorical = false;
    private DatabaseReference requestsRef;
    private DatabaseReference reactionsRef;
    private TextView tvLikeCount, tvDislikeCount;
    private ImageView imgLike;
    private int lastLikeCount = 0;
    private int lastDislikeCount = 0;

    // פונקציית Lifecycle המופעלת עם יצירת הפרגמנט.
    // היא משמשת לשליפת נתונים (Arguments) שהועברו מהמסך הקודם דרך ה-Bundle, כמו ה-ID של המופע והאם הוא הסתיים.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showId = getArguments().getString("showId");
            isHistorical = getArguments().getBoolean("isHistorical", false);
        }
    }

    // פונקציה המנפחת (Inflating) את קובץ ה-XML והופכת אותו לאובייקט View בזיכרון.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_live, container, false);
    }

    // הפונקציה המרכזית לעיצוב הלוגיקה של ה-UI לאחר יצירתו.
    // מאתחלת את ה-RecyclerView, מחברת מאזינים לרכיבי הלייקים, ומגדירה את מצב התצוגה (LIVE מול היסטוריה).
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 שינוי כאן: בדיקת בטיחות ראשונית למניעת קריסה בשל נתונים חסרים
        if (showId == null || showId.isEmpty()) {
            Toast.makeText(getContext(), "שגיאה: נתוני הופעה לא נטענו כראוי", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        recyclerView = view.findViewById(R.id.rvLiveRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataSet = new ArrayList<>();

        tvLikeCount = view.findViewById(R.id.tvArtLikeCount);
        tvDislikeCount = view.findViewById(R.id.tvArtDislikeCount);
        imgLike = view.findViewById(R.id.imgArtLike);

        // מאזין לחיצה הפותח BottomSheet להצגת רשימת המשתמשים שנתנו לייק.
        tvLikeCount.setOnClickListener(v -> {
            if (lastLikeCount > 0) {
                // 🔹 שינוי כאן: שימוש ב-newInstance במקום בבנאי (Constructor) הישן
                LikersBottomSheet bottomSheet = LikersBottomSheet.newInstance(showId, "like");
                bottomSheet.show(getChildFragmentManager(), "LikersList");
            } else {
                Toast.makeText(getContext(), "עדיין אין לייקים", Toast.LENGTH_SHORT).show();
            }
        });

        // מאזין לחיצה הפותח BottomSheet להצגת רשימת המשתמשים שנתנו דיסלייק.
        tvDislikeCount.setOnClickListener(v -> {
            if (lastDislikeCount > 0) {
                // 🔹 שינוי כאן: שימוש ב-newInstance במקום בבנאי (Constructor) הישן
                LikersBottomSheet bottomSheet = LikersBottomSheet.newInstance(showId, "dislike");
                bottomSheet.show(getChildFragmentManager(), "DislikersList");
            } else {
                Toast.makeText(getContext(), "אין דיסלייקים", Toast.LENGTH_SHORT).show();
            }
        });

        // אתחול האדפטור. הפרמטר 'true' מסמן לאדפטור שהמשתמש הנוכחי הוא האמן, מה שמאפשר לו לסמן שירים שבוצעו.
        adapter = new RequestsAdapter(dataSet, showId, true);
        recyclerView.setAdapter(adapter);

        // בדיקה לוגית לשינוי נראות רכיבים במידה והמופע כבר הסתיים (מצב היסטוריה).
        if (isHistorical) {
            TextView tvHeader = view.findViewById(R.id.tvLiveStatusHeader);
            if (tvHeader != null) tvHeader.setText("סיכום מופע עבר 📜");

            View btnEnd = view.findViewById(R.id.btnEndShow);
            if (btnEnd != null) btnEnd.setVisibility(View.GONE);
        }

        // 🔹 שינוי כאן: כל הלוגיקה של Firebase והכפתורים עטופה בבדיקה שה-showId קיים
        if (showId != null) {
            requestsRef = FirebaseDatabase.getInstance().getReference("Requests").child(showId);
            listenForRequests(); // התחלת האזנה לבקשות שירים.

            reactionsRef = FirebaseDatabase.getInstance().getReference("Reactions").child(showId);
            observeReactions(); // התחלת האזנה ללייקים ודיסלייקים.

            // 🔹 שינוי כאן: העברתי את ה-Listener לתוך ה-IF כדי למנוע קריסה אם showId הוא null
            View btnEndShow = view.findViewById(R.id.btnEndShow);
            if (btnEndShow != null) {
                btnEndShow.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference("Shows").child(showId).child("live").setValue(false)
                            .addOnCompleteListener(task -> Navigation.findNavController(requireView()).popBackStack());
                });
            }
        }
    }

    // פונקציה המממשת מודל Reactive UI. היא מאזינה לצומת ה-Reactions ב-Firebase ומעדכנת את הממשק בזמן אמת.
    private void observeReactions() {
        reactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 🔹 שינוי: בדיקה שהפרגמנט עדיין "חי" למניעת קריסה בזמן עדכון אסינכרוני
                if (!isAdded() || getContext() == null) return;

                int currentLikes = 0;
                int currentDislikes = 0;

                // סריקת כל הריאקציות וספירת לייקים מול דיסלייקים.
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String val = ds.getValue(String.class);
                    if ("like".equals(val)) currentLikes++;
                    else if ("dislike".equals(val)) currentDislikes++;
                }

                // טריגר לאנימציה ויזואלית במידה והתווסף לייק חדש מאז העדכון האחרון.
                if (currentLikes > lastLikeCount) {
                    animateLikePop();
                }

                tvLikeCount.setText(String.valueOf(currentLikes));
                tvDislikeCount.setText(String.valueOf(currentDislikes));

                // עדכון אינדיקציה ויזואלית של אייקון הלב (צבע וצורה) לפי קיום לייקים.
                if (currentLikes > 0) {
                    imgLike.setImageResource(android.R.drawable.btn_star_big_on);
                    // 🔹 שינוי: שימוש ב-getContext() בטוח במקום requireContext()
                    imgLike.setColorFilter(ContextCompat.getColor(getContext(), R.color.beat_primary));
                } else {
                    imgLike.setImageResource(android.R.drawable.btn_star_big_off);
                    imgLike.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }

                lastLikeCount = currentLikes;
                lastDislikeCount = currentDislikes;
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // פונקציה המשתמשת ב-Property Animation System כדי ליצור אפקט "דופק" (Pulse) על אייקון הלייק.
    private void animateLikePop() {
        // שימוש ב-PropertyValuesHolder כדי להריץ אנימציית SCALE בצירי X ו-Y בו-זמנית.
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.4f, 1.0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.4f, 1.0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imgLike, pvhX, pvhY);
        animator.setDuration(400); // משך האנימציה במילישניות.
        animator.start();
    }

    // פונקציה המאזינה לצומת הבקשות (Requests) ב-Firebase.
    // כל בקשה חדשה שנכנסת מתווספת לראש הרשימה (אינדקס 0) כדי שהאמן יראה קודם את הבקשות העדכניות ביותר.
    private void listenForRequests() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataSet.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Request r = ds.getValue(Request.class);
                    if (r != null) dataSet.add(0, r);
                }
                // עדכון האדפטור על שינוי בנתונים לריענון הרשימה ב-UI.
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}