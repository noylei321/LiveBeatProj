package com.example.myapplication;

import android.graphics.Color; // 🔹 שינוי: ייבוא לצבע טקסט לבן
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip; // 🔹 שינוי: ייבוא Chip
import com.google.android.material.chip.ChipGroup; // 🔹 שינוי: ייבוא ChipGroup
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileFragment extends Fragment {

    private ShapeableImageView imgUserProfile;
    private TextView tvUserFullName, tvUserUsername, tvUserBio;
    private ChipGroup cgUserGenres; // 🔹 שינוי: החלפת TextView ב-ChipGroup
    private TextView tvUserPhone, tvUserEmail, tvUserBirthDate;

    private String userId;
    private boolean fromArtistLive = false;

    public UserProfileFragment() { }

    // פונקציית Lifecycle שבו מתבצעת שליפת הארגומנטים.
    // אנו בודקים האם הועבר ID ספציפי (צפייה בפרופיל של אחר) או שצריך להציג את הפרופיל האישי של המשתמש המחובר.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            fromArtistLive = getArguments().getBoolean("fromArtistLive", false);
        }

        // Fallback: אם אין userId בארגומנטים, נשלוף את ה-UID של המשתמש הנוכחי מ-FirebaseAuth.
        if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    // אתחול רכיבי הממשק והגדרת לוגיקת החזרה (Back Navigation).
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgUserProfile = view.findViewById(R.id.imgUserProfile);
        tvUserFullName = view.findViewById(R.id.tvUserFullName);
        tvUserUsername = view.findViewById(R.id.tvUserUsername);
        tvUserBio = view.findViewById(R.id.tvUserBio);

        // 🔹 שינוי: Binding לרכיב ה-ChipGroup החדש
        cgUserGenres = view.findViewById(R.id.cgUserGenres);

        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserBirthDate = view.findViewById(R.id.tvUserBirthDate);

        TextView tvBackUser = view.findViewById(R.id.tvBackToMain);
        // הוספת קו תחתי לטקסט החזרה (Underline) בצורה תוכנתית.
        tvBackUser.setPaintFlags(tvBackUser.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        // ניהול הניווט חזור:
        // 1. אם הגענו מהלייקים במופע (האמן צופה בבליין) -> נחזור אחורה במחסנית בעזרת popBackStack.
        // 2. אם הגענו כמשתמש רגיל לצפות בפרופיל של עצמנו -> נחזור למפה הראשית.
        if (fromArtistLive) {
            tvBackUser.setText("חזרה למופע 🎤");
            tvBackUser.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        } else {
            tvBackUser.setText("חזרה למסך הראשי");
            tvBackUser.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_userProfileFragment_to_mapFragment);
            });
        }

        if (userId != null) {
            loadUserData(userId);
        } else {
            Toast.makeText(getContext(), "לא נמצא משתמש מחובר", Toast.LENGTH_LONG).show();
        }
    }

    // פונקציה השולפת את נתוני המשתמש מהצומת "Users" ב-Realtime Database.
    // שימוש ב-addListenerForSingleValueEvent הוא יעיל יותר כאן כי מדובר בתצוגת פרופיל סטטית שלא דורשת האזנה קבועה.
    private void loadUserData(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // המרת ה-Snapshot לאובייקט Java מסוג User.
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvUserFullName.setText(user.getFullName());
                    tvUserUsername.setText("@" + user.getUsername());

                    // טיפול ב-Empty States: אם למשתמש אין ביוגרפיה, נציג טקסט ברירת מחדל.
                    if (user.getBio() == null || user.getBio().isEmpty()) {
                        tvUserBio.setText("עדיין לא נכתבה ביוגרפיה...");
                    } else {
                        tvUserBio.setText(user.getBio());
                    }

                    // 🔹 שינוי: הצגת הז'אנרים המועדפים כצ'יפים מעוצבים
                    populateUserGenreChips(user.getGenre());

                    tvUserPhone.setText(user.getPhone());
                    tvUserEmail.setText(user.getEmail());
                    tvUserBirthDate.setText(user.getBirthDate());

                    // טעינת תמונת הפרופיל באמצעות Glide.
                    // שימוש ב-placeholder מבטיח שהמשתמש יראה אייקון זמני בזמן שהתמונה נטענת מהשרת.
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        try {
                            Glide.with(requireContext())
                                    .load(user.getProfileImageUrl())
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(imgUserProfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 🔹 שינוי: פונקציית עזר לפירוק המחרוזת מה-DB ויצירת צ'יפים לתצוגה בלבד
    private void populateUserGenreChips(String genres) {
        if (cgUserGenres == null) return;
        cgUserGenres.removeAllViews();
        if (genres == null || genres.isEmpty()) return;

        String[] genresArray = genres.split(", ");
        for (String g : genresArray) {
            Chip chip = new Chip(requireContext());
            chip.setText(g);
            chip.setClickable(false);
            chip.setCheckable(false);

            // עיצוב הצ'יפ (שימוש בצבע הורוד של המותג)
            chip.setChipBackgroundColorResource(R.color.beat_pink);
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeWidth(0f);

            cgUserGenres.addView(chip);
        }
    }
}