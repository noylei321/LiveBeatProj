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
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendRequestFragment extends Fragment {

    private EditText etSenderName, etRequestContent;
    private Button btnSendRequest;
    private DatabaseReference dbRef;

    // משתנה שישמור את המזהה של ההופעה הספציפית
    private String showId;

    public SendRequestFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // חילוץ ה-showId שהעברנו ב-Bundle מה-MapFragment
        if (getArguments() != null) {
            showId = getArguments().getString("showId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול הגישה ל-Firebase - יצירת תיקייה לכל הופעה בנפרד
        if (showId != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("Requests").child(showId);
        } else {
            dbRef = FirebaseDatabase.getInstance().getReference("Requests");
        }

        etSenderName = view.findViewById(R.id.etSenderName);
        etRequestContent = view.findViewById(R.id.etRequestContent);
        btnSendRequest = view.findViewById(R.id.btnSendRequest);

        if (btnSendRequest != null) {
            btnSendRequest.setOnClickListener(v -> {
                sendData();
            });
        }
    }

    private void sendData() {
        String name = etSenderName.getText().toString().trim();
        String content = etRequestContent.getText().toString().trim();

        if (name.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. יצירת מפתח ייחודי לבקשה הספציפית הזו
        String requestId = dbRef.push().getKey();

        // 2. יצירת אובייקט Request עם ה-ID החדש, התוכן והשם
        Request newRequest = new Request(requestId, content, name);

        // 3. שמירה ב-Firebase תחת ה-requestId שיצרנו
        if (requestId != null) {
            dbRef.child(requestId).setValue(newRequest).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "הבקשה נשלחה לאמן!🎤", Toast.LENGTH_SHORT).show();

                    // חזרה למפה אחרי השליחה
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}