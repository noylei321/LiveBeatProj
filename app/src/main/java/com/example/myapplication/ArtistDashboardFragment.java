package com.example.myapplication;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ArtistDashboardFragment extends Fragment {

    private RecyclerView rvShows;
    private ShowsAdapter showsAdapter;
    private ArrayList<Show> showsList;
    private Chip artLogout, chipEditProfile;
    private ImageView imgArtistProfile, imgArtistTypeIcon;
    private TextView tvArtistName, tvArtistUsername, tvArtistType, tvArtistFullName;
    private Button btnStartLive;
    private DatabaseReference showsRef;
    private FirebaseAuth mAuth;

    public ArtistDashboardFragment() { }

    // פונקציית Lifecycle המנפחת (Inflate) את ה-Layout של הדאשבורד ומחזירה את ה-View למערכת.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_dashboard, container, false);
    }

    // פונקציה המופעלת לאחר יצירת ה-View. היא משמשת לאתחול אובייקטי Firebase, קישור רכיבי UI, והגדרת ה-RecyclerView.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        showsRef = FirebaseDatabase.getInstance().getReference("Shows");

        imgArtistProfile = view.findViewById(R.id.imgArtistProfile);
        tvArtistName = view.findViewById(R.id.tvArtistName);
        imgArtistTypeIcon = view.findViewById(R.id.imgArtistTypeIcon);
        tvArtistUsername = view.findViewById(R.id.tvArtistUsername);
        tvArtistType = view.findViewById(R.id.tvArtistType);
        tvArtistFullName = view.findViewById(R.id.tvArtistFullName);
        btnStartLive = view.findViewById(R.id.btnStartLive);
        chipEditProfile = view.findViewById(R.id.chipEditProfile);

        loadArtistDataFromDB(); // טעינת נתוני פרופיל.

        // 🔹 שינוי: קריאה לפונקציית תיקון נתונים חסרים ב-Firebase
        repairMissingShowIds();

        rvShows = view.findViewById(R.id.rvPastShows);
        showsList = new ArrayList<>();

        // 🔹 התיקון בוצע כאן: הוספת הפרמטר true כדי להתאים לבנאי החדש ולאפשר עריכה בדאשבורד
        showsAdapter = new ShowsAdapter(showsList, true);

        if (rvShows != null) {
            rvShows.setLayoutManager(new LinearLayoutManager(getContext()));
            rvShows.setAdapter(showsAdapter);
        }

        readShowsFromDB(); // טעינת רשימת ההופעות.

        FloatingActionButton fabAddShow = view.findViewById(R.id.fabAddShow);
        if (fabAddShow != null) {
            // מאזין הפותח את הדיאלוג להוספת הופעה חדשה.
            fabAddShow.setOnClickListener(v -> {
                AddShowDialogFragment dialog = new AddShowDialogFragment();
                dialog.show(getChildFragmentManager(), "AddShowDialog");
            });
        }

        // הפעלת לוגיקת הניווט החכמה למצב LIVE.
        btnStartLive.setOnClickListener(v -> {
            findClosestShowAndNavigate();
        });

        artLogout = view.findViewById(R.id.UserLogout);
        artLogout.setOnClickListener(v -> showLogoutDialog());

        // מאזין לניווט למסך עריכת הפרופיל באמצעות ה-Navigation Component.
        view.findViewById(R.id.chipEditProfile).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_artistDashboardFragment_to_editArtistProfileFragment);
        });
    }

    // פונקציה המבצעת שאילתה ל-Firebase כדי למצוא הופעה פעילה (isLive) או את ההופעה הקרובה ביותר בזמן הנוכחי.
    private void findClosestShowAndNavigate() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Shows");

        // ביצוע שאילתה מסוננת לפי ה-ID של האמן.
        ref.orderByChild("artistId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String liveShowId = null;
                String closestShowId = null;
                long minDiff = Long.MAX_VALUE;
                long currentTime = System.currentTimeMillis();

                final long FOUR_HOURS_IN_MILLIS = 14400000;
                final long TWO_HOURS_IN_MILLIS = 7200000;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Show show = ds.getValue(Show.class);
                    if (show != null) {
                        // בדיקת עדיפות עליונה: האם יש מופע שמוגדר כבר כפעיל בשרת.
                        if (show.isLive()) {
                            liveShowId = show.getShowId();
                            break;
                        }

                        try {
                            long showTime = convertTimeToMillis(show.getDate(), show.getTime());

                            // התעלמות מהופעות שהסתיימו לפני יותר מ-4 שעות.
                            if (currentTime > (showTime + FOUR_HOURS_IN_MILLIS)) continue;

                            // חישוב ההפרש הקטן ביותר למציאת המופע הקרוב ביותר (עתידי או נוכחי).
                            long diff = Math.abs(currentTime - showTime);
                            if (diff < minDiff) {
                                minDiff = diff;
                                closestShowId = show.getShowId();
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }

                // לוגיקת החלטת הניווט המבוססת על תוצאות הסריקה.
                if (liveShowId != null) {
                    navigateToLive(liveShowId);
                } else if (closestShowId != null) {
                    // אם המופע הקרוב רחוק ביותר משעתיים, נבקש אישור מהמשתמש.
                    if (minDiff > TWO_HOURS_IN_MILLIS) {
                        final String finalShowId = closestShowId;
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("שים לב ⚠️")
                                .setMessage("לא נמצאה הופעה פעילה כעת. האם להתחיל את המופע הקרוב בכל זאת?")
                                .setPositiveButton("כן, התחל", (dialog, which) -> navigateToLive(finalShowId))
                                .setNegativeButton("ביטול", null)
                                .show();
                    } else {
                        navigateToLive(closestShowId);
                    }
                } else {
                    Toast.makeText(getContext(), "לא נמצאו הופעות פעילות או קרובות", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // פונקציית עזר המבצעת את הניווט הפיזי לפרגמנט ה-Live תוך העברת ה-ID של המופע כארגומנט ב-Bundle.
    private void navigateToLive(String showId) {
        Bundle b = new Bundle();
        b.putString("showId", showId);
        b.putBoolean("isHistorical", false);
        // 🔹 שינוי: שימוש ב-Action ID המלא כדי למנוע קריסות ב-Navigation Component
        Navigation.findNavController(requireView()).navigate(R.id.action_artistDashboardFragment_to_artistLiveFragment, b);
    }

    // פונקציה המושכת את נתוני הפרופיל של האמן מה-DB ומעדכנת את רכיבי ה-UI (שם, תמונה וסוג אמן).
    private void loadArtistDataFromDB() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference artistRef = FirebaseDatabase.getInstance().getReference("Artists").child(uid);

        artistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Artist artist = snapshot.getValue(Artist.class);
                if (artist == null) return;

                // בחירת השם להצגה: עדיפות לשם במה, אם לא קיים - שם מלא.
                String name = (artist.getStageName() != null && !artist.getStageName().isEmpty())
                        ? artist.getStageName() : artist.getFullName();

                tvArtistName.setText(name);
                tvArtistUsername.setText("@" + artist.getUsername());
                tvArtistFullName.setText(artist.getFullName());
                tvArtistType.setText(artist.getArtistSubCategory());

                if (imgArtistTypeIcon != null) setArtistTypeIcon(artist.getArtistSubCategory());

                // טעינת תמונת הפרופיל באמצעות ספריית Glide לניהול זיכרון וטעינה אופטימלית.
                if (artist.getProfileImageUrl() != null && !artist.getProfileImageUrl().isEmpty()) {
                    Glide.with(requireContext()).load(artist.getProfileImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery).into(imgArtistProfile);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // פונקציית עזר לעדכון האייקון הוויזואלי של סוג האמן בהתאם לקטגוריה שלו ב-DB.
    private void setArtistTypeIcon(String subCat) {
        switch (subCat) {
            case "DJ": imgArtistTypeIcon.setImageResource(R.drawable.ic_dj); break;
            case "Comedian": case "קומיקאי": imgArtistTypeIcon.setImageResource(R.drawable.ic_standup); break;
            default: imgArtistTypeIcon.setImageResource(R.drawable.ic_singer); break;
        }
    }

    // פונקציה המאזינה לשינויים בזמן אמת (Real-time) בטבלת ההופעות ומעדכנת את ה-RecyclerView.
    private void readShowsFromDB() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUid = mAuth.getCurrentUser().getUid();

        showsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Show show = snapshot.getValue(Show.class);
                    // סינון הופעות השייכות לאמן המחובר בלבד.
                    if (show != null && show.getArtistId().equals(currentUid)) {
                        showsList.add(show);
                    }
                }

                // מיון הרשימה כך שההופעות החדשות ביותר יופיעו בראש הרשימה (סדר כרונולוגי יורד).
                Collections.sort(showsList, (s1, s2) -> {
                    try {
                        return Long.compare(convertTimeToMillis(s2.getDate(), s2.getTime()),
                                convertTimeToMillis(s1.getDate(), s1.getTime()));
                    } catch (Exception e) { return 0; }
                });

                toggleNoShowsMessage(); // הצגת הודעה אם הרשימה ריקה.
                showsAdapter.notifyDataSetChanged();

                // עדכון מצב כפתור ה-LIVE בהתאם לקיום הופעה רלוונטית להיום.
                Show target = findActiveOrUpcomingShow();
                updateLiveButton(target != null);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // פונקציית UI המנהלת את נראות הודעת ה-"אין הופעות" אל מול רשימת ה-RecyclerView.
    private void toggleNoShowsMessage() {
        View view = getView();
        if (view == null) return;
        TextView tvNoShows = view.findViewById(R.id.tvNoShows);
        if (showsList.isEmpty()) {
            rvShows.setVisibility(View.GONE);
            if (tvNoShows != null) tvNoShows.setVisibility(View.VISIBLE);
        } else {
            rvShows.setVisibility(View.VISIBLE);
            if (tvNoShows != null) tvNoShows.setVisibility(View.GONE);
        }
    }

    // פונקציה המחפשת הופעה שמתרחשת היום ונמצאת בטווח זמן סביר (שעה לפני או עד 4 שעות אחרי ההתחלה).
    private Show findActiveOrUpcomingShow() {
        long currentTime = System.currentTimeMillis();
        long oneHour = 3600000;
        String todayDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

        for (Show show : showsList) {
            if (show.isLive()) return show; // עדיפות למופע שכבר פעיל.
        }

        for (Show show : showsList) {
            try {
                if (show.getDate().equals(todayDate)) {
                    long startTime = convertTimeToMillis(show.getDate(), show.getTime());
                    if (currentTime >= (startTime - oneHour) && currentTime <= (startTime + 14400000)) {
                        return show;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    // פונקציית עזר המבצעת Parsing של מחרוזות תאריך ושעה לערך Long של מילישניות לצורך חישובים מתמטיים.
    private long convertTimeToMillis(String date, String time) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date mDate = sdf.parse(date + " " + time);
        return mDate != null ? mDate.getTime() : 0;
    }

    // פונקציה המעדכנת את הצבע, הטקסט והזמינות של כפתור ה-LIVE בהתאם למצב ההופעות.
    private void updateLiveButton(boolean enabled) {
        if (btnStartLive != null) {
            btnStartLive.setEnabled(enabled);
            btnStartLive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(enabled ? "#800080" : "#888888")));

            boolean isAnyLive = false;
            for(Show s : showsList) { if(s.isLive()) isAnyLive = true; }

            if (isAnyLive) {
                btnStartLive.setText("המשך הופעה חיה 🔴");
            } else {
                btnStartLive.setText(enabled ? "התחל הופעה חיה" : "אין הופעה קרובה להיום");
            }
        }
    }

    // פונקציה המציגה דיאלוג אישור לפני ביצוע ניתוק (Sign Out) מהמערכת.
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("התנתקות")
                .setMessage("אתה בטוח שברצונך להתנתק?")
                .setPositiveButton("כן", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
                })
                .setNegativeButton("לא", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🔹 שינוי: פונקציה לתיקון נתונים ישנים ב-Firebase (השלמת שדות showId חסרים)
    private void repairMissingShowIds() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUid = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Shows");

        ref.orderByChild("artistId").equalTo(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (!ds.hasChild("showId")) {
                        String actualKey = ds.getKey();
                        ds.getRef().child("showId").setValue(actualKey);
                        Log.d("DataRepair", "Fixed missing showId for: " + actualKey);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}