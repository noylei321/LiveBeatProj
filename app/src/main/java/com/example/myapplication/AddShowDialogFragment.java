package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddShowDialogFragment extends DialogFragment {

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyDlFPlELZDI1OC8Kx_oROaEyJHRFEC9VXU";

    public AddShowDialogFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etLocation = view.findViewById(R.id.etShowAddress);
        EditText etDate = view.findViewById(R.id.etShowDate);
        EditText etTime = view.findViewById(R.id.etShowTime);
        EditText etGenre = view.findViewById(R.id.etShowStyle);
        Button btnPublish = view.findViewById(R.id.btnConfirmAddShow);

        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));

        btnPublish.setOnClickListener(v -> {
            String location = etLocation.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String genre = etGenre.getText().toString().trim();

            if (location.isEmpty() || date.isEmpty() || time.isEmpty() || genre.isEmpty()) {
                Toast.makeText(getContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // שלב 1: הופכים כתובת לקואורדינטות דרך גוגל (REST API)
            getCoordinatesAndSave(location, date, time, genre);
        });
    }

    private void getCoordinatesAndSave(String address, String date, String time, String genre) {
        GeocodingApiService apiService = RetrofitClient.getApiService();

        apiService.getCoordinates(address, GOOGLE_MAPS_API_KEY).enqueue(new Callback<GeocodeResponse>() {
            @Override
            public void onResponse(Call<GeocodeResponse> call, Response<GeocodeResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                    LocationModel loc = response.body().getResults().get(0).getGeometry().getLocation();
                    double lat = loc.getLat();
                    double lng = loc.getLng();

                    // שלב 2: שומרים ל-Firebase כדי שיופיע נעץ במפה
                    saveShowToFirebase(address, date, time, genre, lat, lng);
                } else {
                    Toast.makeText(getContext(), "כתובת לא נמצאה במפה", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodeResponse> call, Throwable t) {
                Toast.makeText(getContext(), "שגיאת תקשורת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveShowToFirebase(String location, String date, String time, String genre, double lat, double lng) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference showsRef = FirebaseDatabase.getInstance().getReference("Shows").push();
        String showId = showsRef.getKey();

        // יצירת ההופעה - כאן היא נשמרת ב-Firebase והנעץ נוצר במפה
        Show newShow = new Show(showId, uid, location, time, genre, date, lat, lng);
        newShow.setLive(false); // ההופעה נוצרת אבל עוד לא "בשידור חי" (בלי הכפתור הסגול)

        showsRef.setValue(newShow).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "הופעה נוספה למפה!", Toast.LENGTH_SHORT).show();
                dismiss(); // פשוט סוגר את הדיאלוג וחוזר לעמוד הקודם
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void showDatePickerDialog(EditText etDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}