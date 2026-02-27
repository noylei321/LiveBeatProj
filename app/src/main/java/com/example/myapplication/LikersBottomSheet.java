package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class LikersBottomSheet extends BottomSheetDialogFragment {

    // מפתחות עבור ה-Bundle (שינוי: הוספת קבועים)
    private static final String ARG_SHOW_ID = "show_id";
    private static final String ARG_TYPE = "type";
    private String showId;
    private String type; // "like" או "dislike"
    private ArrayList<User> likersList;
    private LikersAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar; // חדש: משתנה לבר טעינה

    // בנאי (Constructor) המקבל את מזהה המופע ואת סוג הרשימה להצגה.
    // שימוש בפרמטר 'type' מאפשר לנו להשתמש באותו קלאס גם להצגת לייקים וגם להצגת דיסלייקים (Reusability).
    // שינוי: הדרך הנכונה ליצור מופע חדש של Fragment עם פרמטרים
    public static LikersBottomSheet newInstance(String showId, String type) {
        LikersBottomSheet fragment = new LikersBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SHOW_ID, showId);
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }
    // שינוי: שליפת הנתונים מה-Bundle בזמן יצירת ה-Fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showId = getArguments().getString(ARG_SHOW_ID);
            type = getArguments().getString(ARG_TYPE);
        }
    }

    // פונקציית Lifecycle המנפחת את ה-XML של הדיאלוג.
    // ה-BottomSheetDialogFragment מספק חוויית משתמש שבה התוכן מחליק מלמטה למעלה.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_likers_sheet, container, false);

        // עדכון הכותרת באופן דינמי לפי הסוג שנשלח בבנאי.
        TextView tvTitle = v.findViewById(R.id.tvSheetTitle);
        if (tvTitle != null) {
            tvTitle.setText("like".equals(type) ? "מי פרגן במופע? ⭐" : "מי פחות התחבר? 💔");
        }

        // חדש: אתחול ה-ProgressBar
        progressBar = v.findViewById(R.id.progressBar);

        // אתחול מערכת הרשימה (RecyclerView).
        recyclerView = v.findViewById(R.id.rvLikers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        likersList = new ArrayList<>();

        // יצירת האדפטור המקשר בין רשימת המשתמשים לתצוגה הגרפית.
        adapter = new LikersAdapter(requireContext(), likersList);
        recyclerView.setAdapter(adapter);

        // התחלת תהליך שליפת הנתונים מהשרת.
        loadLikersData();

        return v;
    }

    // פונקציה המבצעת את השלב הראשון בשליפת הנתונים:
    // היא ניגשת לצומת ה-Reactions ומחפשת את כל ה-UIDs של המשתמשים שביצעו פעולה התואמת ל-type המבוקש.
    private void loadLikersData() {
        if (showId == null) return; // בדיקת בטיחות

        // חדש: הצגת ה-ProgressBar לפני תחילת השליפה
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        DatabaseReference reactionsRef = FirebaseDatabase.getInstance().getReference("Reactions").child(showId);

        // מאזין לאירוע חד פעמי כדי לקבל תמונת מצב (Snapshot) של כל הריאקציות למופע הספציפי.
        reactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // חדש: אם אין נתונים בכלל, נסתיר את הבר כבר עכשיו
                if (!snapshot.exists() && progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                // מעבר בלולאה על כל הילדים (התגובות) בתוך ה-Snapshot.
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // בדיקה האם הערך (like/dislike) תואם לסוג שהדיאלוג אמור להציג.
                    if (type.equals(ds.getValue(String.class))) {
                        String userId = ds.getKey(); // המפתח הוא ה-UID של המשתמש.
                        fetchUserDetail(userId); // שלב 2: שליפת הפרטים המלאים של אותו משתמש.
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                // חדש: הסתרת הבר במקרה של שגיאה
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    // פונקציה המבצעת את השלב השני (ה-Join):
    // עבור כל UID שנמצא בשלב הקודם, היא ניגשת לצומת ה-Users כדי לשלוף את אובייקט המשתמש המלא (שם, תמונה וכו').
    private void fetchUserDetail(String uid) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            user.setUserId(uid); // שמירת ה-ID בתוך האובייקט לצורך ניווט עתידי.
                            likersList.add(user);

                            //  שינוי: בדיקת בטיחות לפני עדכון ה-UI
                            if (isAdded()) {
                                adapter.notifyItemInserted(likersList.size() - 1);
                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });
    }
}