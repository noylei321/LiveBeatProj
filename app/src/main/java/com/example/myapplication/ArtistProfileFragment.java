package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ArtistProfileFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private TextView tvStageName, tvArtistType, tvBio, tvGenres, tvInstrument, tvFullName, tvBirthDate;
    private MaterialButton btnInstagram, btnPhone, btnEmail;

    private String instagramLink = "";
    private String phoneNumber = "";
    private String emailAddress = "";

    private String artistId;
    private boolean fromShowList = false;

    public ArtistProfileFragment() { }

    // פונקציית Lifecycle המופעלת בעת יצירת האובייקט בזיכרון.
    // היא משמשת לשליפת ארגומנטים (Arguments) שהועברו לפרגמנט, כמו ה-ID של האמן והדגל שמציין מאיפה הגיע המשתמש.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistId = getArguments().getString("artistId");
            fromShowList = getArguments().getBoolean("fromShowList", false);
        }

        // מנגנון הגנה (Fallback): אם לא הועבר ID של אמן (למשל בכניסה ישירה לפרופיל האישי), נשתמש ב-UID של המשתמש המחובר כרגע.
        if (artistId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            artistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    // פונקציה המנפחת את קובץ ה-XML והופכת אותו ל-View.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_profile, container, false);
    }

    // פונקציה המופעלת לאחר שה-View נוצר. כאן מתבצע הקישור (Binding) בין הקוד לרכיבי הממשק והגדרת הלוגיקה של הניווט וכפתורי הפעולה.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgProfile = view.findViewById(R.id.profileImg);
        tvStageName = view.findViewById(R.id.tvStageName);
        tvArtistType = view.findViewById(R.id.tvArtistType);
        tvBio = view.findViewById(R.id.tvBio);
        tvGenres = view.findViewById(R.id.tvGenres);
        tvInstrument = view.findViewById(R.id.tvInstrument);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);

        btnInstagram = view.findViewById(R.id.btnInstagram);
        btnPhone = view.findViewById(R.id.btnPhone);
        btnEmail = view.findViewById(R.id.btnEmail);

        TextView tvBack = view.findViewById(R.id.tvBackToDashboard);
        // הוספת קו תחתון לטקסט באופן תכנותי כדי לסמן למשתמש שמדובר באלמנט לחיץ.
        tvBack.setPaintFlags(tvBack.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        // ניהול זרימת הניווט (Navigation Flow):
        // אם המשתמש הגיע מרשימת ההופעות, נשתמש ב-popBackStack כדי לחזור אחורה במחסנית הניווט ולשמור על מצב המסך הקודם.
        if (fromShowList) {
            tvBack.setText("חזרה לבקשות 🎸");
            tvBack.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        } else {
            // אם זו כניסה רגילה, נבצע ניווט מפורש (Explicit Navigation) חזרה לדאשבורד.
            tvBack.setText("חזרה למסך הראשי");
            tvBack.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_artistProfileFragment_to_artistDashboardFragment);
            });
        }

        if (artistId != null) {
            loadArtistData(artistId);
        } else {
            Toast.makeText(getContext(), "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
        }

        setupActionButtons(); // אתחול המאזינים לכפתורי יצירת הקשר.
    }

    // פונקציה המבצעת קריאה חד-פעמית (Single Value Event) ל-Firebase Realtime Database.
    // היא שולפת את אובייקט ה-Artist המלא וממפה אותו אוטומטית למחלקת ה-Java (POJO) שלנו.
    private void loadArtistData(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Artists").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Artist artist = snapshot.getValue(Artist.class);
                if (artist != null) {
                    // עדכון שדות הטקסט ב-UI עם המידע שנשלף מה-Database.
                    tvStageName.setText(artist.getStageName());
                    tvArtistType.setText(artist.getArtistSubCategory() + " • @" + artist.getUsername());
                    tvBio.setText(artist.getBio().isEmpty() ? "אין תיאור זמין" : artist.getBio());
                    tvGenres.setText(artist.getGenre());
                    tvInstrument.setText(artist.getInstrument());
                    tvFullName.setText(artist.getFullName());
                    tvBirthDate.setText(artist.getBirthDate());

                    // שמירת נתוני יצירת קשר במשתנים מקומיים לצורך שימוש ב-Intents בהמשך.
                    instagramLink = artist.getSocialLink();
                    phoneNumber = artist.getPhone();
                    emailAddress = artist.getEmail();

                    // שימוש בספריית Glide לטעינה אסינכרונית של תמונות מה-Web.
                    // Glide מטפלת אוטומטית ב-Caching (שמירה בזיכרון) ובהקטנת התמונה לממדי ה-View כדי לחסוך ב-RAM.
                    if (artist.getProfileImageUrl() != null && !artist.getProfileImageUrl().isEmpty()) {
                        try {
                            Glide.with(requireContext()).load(artist.getProfileImageUrl()).into(imgProfile);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // פונקציה המגדירה את ה-Implicit Intents עבור כפתורי יצירת הקשר.
    // Implicit Intent מבקש מהמערכת "לבצע פעולה" מבלי לציין אפליקציה ספציפית, מה שמאפשר למשתמש לבחור את הדפדפן או חייגן הטלפון המועדף עליו.
    private void setupActionButtons() {

        // כפתור אינסטגרם: משתמש ב-ACTION_VIEW כדי לפתוח URL בדפדפן או באפליקציית האינסטגרם.
        btnInstagram.setOnClickListener(v -> {
            if (instagramLink != null && !instagramLink.isEmpty()) {
                // בדיקת תקינות הפרוטוקול - חובה להוסיף http/https כדי שה-Uri.parse לא יקרוס.
                if (!instagramLink.startsWith("http")) {
                    instagramLink = "https://" + instagramLink;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramLink));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "אין קישור לאינסטגרם", Toast.LENGTH_SHORT).show();
            }
        });

        // כפתור טלפון: משתמש ב-ACTION_DIAL כדי להעביר את המספר ישירות לחייגן הטלפון (בניגוד ל-ACTION_CALL, פעולה זו לא דורשת הרשאות מסוכנות).
        btnPhone.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "אין מספר טלפון", Toast.LENGTH_SHORT).show();
            }
        });

        // כפתור אימייל: משתמש ב-ACTION_SENDTO עם ה-Scheme של "mailto:" כדי לפתוח אפליקציות דואר אלקטרוני בלבד.
        btnEmail.setOnClickListener(v -> {
            if (emailAddress != null && !emailAddress.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + emailAddress));
                startActivity(intent);
            }
        });
    }
}