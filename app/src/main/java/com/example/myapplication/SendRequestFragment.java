package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

public class SendRequestFragment extends Fragment {

    private EditText etSenderName, etRequestContent;
    private TextView tvArtistStageName, tvArtistUsername;
    private ImageView imgArtistProfile;
    private Button btnSendRequest;
    private DatabaseReference dbRef;

    private String showId;
    private String artistId;

    public SendRequestFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // חילוץ נתונים מה-Bundle שהועבר מהמסך הקודם
        if (getArguments() != null) {
            showId = getArguments().getString("showId");
            artistId = getArguments().getString("artistId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // תיקון השגיאה: הפרמטר הראשון הוא 'view' ולא שם הפונקציה
        super.onViewCreated(view, savedInstanceState);

        // אתחול רכיבי ה-UI
        etSenderName = view.findViewById(R.id.etSenderName);
        etRequestContent = view.findViewById(R.id.etRequestContent);
        btnSendRequest = view.findViewById(R.id.btnSendRequest);
        imgArtistProfile = view.findViewById(R.id.imgArtistProfile);
        tvArtistStageName = view.findViewById(R.id.tvArtistStageName);
        tvArtistUsername = view.findViewById(R.id.tvArtistUsername);

        if (showId != null) {
            // התחברות לצומת הבקשות הספציפי למופע הזה
            dbRef = FirebaseDatabase.getInstance().getReference("Requests").child(showId);
            loadArtistDetails();
        }

        if (btnSendRequest != null) {
            btnSendRequest.setOnClickListener(v -> sendData());
        }
    }

    private void loadArtistDetails() {
        if (artistId == null) return;

        // שליפת פרטי האמן להצגה בראש המסך
        FirebaseDatabase.getInstance().getReference("Artists").child(artistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && isAdded()) { // isAdded מוודא שהפרגמנט עדיין מחובר ל-UI
                            String stageName = snapshot.child("stageName").getValue(String.class);
                            String username = snapshot.child("username").getValue(String.class);
                            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            if (stageName != null) tvArtistStageName.setText(stageName);
                            if (username != null) tvArtistUsername.setText("@" + username);

                            // שימוש ב-requireContext כדי למנוע קריסה במקרה שהקונטקסט null
                            if (imageUrl != null) {
                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_dj)
                                        .circleCrop()
                                        .into(imgArtistProfile);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void sendData() {
        String name = etSenderName.getText().toString().trim();
        String content = etRequestContent.getText().toString().trim();
        long currentTimestamp = System.currentTimeMillis();

        //   שליפת ה-UID של המשתמש הנוכחי
        String senderId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (name.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת מזהה ייחודי לבקשה
        String requestId = dbRef.push().getKey();

        // יצירת אובייקט הבקשה עם 5 הפרמטרים (כולל ה-false עבור הסטטוס played)
        //   הזרקת ה-senderId כפרמטר רביעי (לפי המודל המעודכן)
        Request newRequest = new Request(requestId, content, name, senderId, currentTimestamp, false);

        if (requestId != null) {
            dbRef.child(requestId).setValue(newRequest).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "הבקשה נשלחה לאמן!🎤", Toast.LENGTH_SHORT).show();
                    // חזרה למסך הקודם
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "שגיאה בשליחה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}