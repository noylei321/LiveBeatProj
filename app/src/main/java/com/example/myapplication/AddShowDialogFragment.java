package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddShowDialogFragment extends DialogFragment {

    private static final String GOOGLE_MAPS_API_KEY = "YOUR_API_KEY"; // מפתח ה-API של גוגל

    // Handler המשמש לניהול משימות ב-UI Thread, כאן הוא מנהל את ה-Debounce למניעת הצפת בקשות API.
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final long DEBOUNCE_DELAY_MS = 500; // השהיה של חצי שנייה בין הקלדה לשליחה לשרת.

    private boolean selectedFromDropdown = false; // דגל המציין אם המשתמש בחר כתובת תקינה מהרשימה או רק הקליד טקסט חופשי.

    public AddShowDialogFragment() { }

    // פונקציית Lifecycle שיוצרת את הממשק הוויזואלי של הדיאלוג על ידי ניפוח (Inflation) של ה-XML.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_show, container, false);
    }

    // הפונקציה המרכזית להגדרת הלוגיקה לאחר שה-View נוצר. היא מקשרת בין הקוד לרכיבי הממשק.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoCompleteTextView etLocation = view.findViewById(R.id.etShowAddress);
        EditText etDate = view.findViewById(R.id.etShowDate);
        EditText etTime = view.findViewById(R.id.etShowTime);
        EditText etGenre = view.findViewById(R.id.etShowStyle);
        Button btnPublish = view.findViewById(R.id.btnConfirmAddShow);

        // מצב התחלתי של כפתור השמירה ככבוי עד שהטופס יתמלא כראוי.
        btnPublish.setEnabled(false);
        btnPublish.setAlpha(0.5f);

        // הגדרת מאזינים לפתיחת דיאלוגים לבחירת זמן ותאריך.
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        etTime.setOnClickListener(v -> showTimePickerDialog(etTime));

        // הגדרת האדפטור שמנהל את רשימת הצעות הכתובות בתוך ה-AutoCompleteTextView.
        ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        etLocation.setAdapter(addressAdapter);

        // פונקציה אנונימית המופעלת כאשר המשתמש בוחר פריט מרשימת הכתובות.
        etLocation.setOnItemClickListener((parent, v, position, id) -> {
            selectedFromDropdown = true; // סימון שהבחירה תקינה.
            updatePublishButtonState(btnPublish, etLocation, etDate, etTime, etGenre);
        });

        // מאזין לשינויי טקסט בשדה הכתובת לביצוע חיפוש אקטיבי.
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            // מופעל בכל פעם שהמשתמש מקליד תו חדש.
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedFromDropdown = false; // איפוס הבחירה כי הטקסט השתנה.
                updatePublishButtonState(btnPublish, etLocation, etDate, etTime, etGenre);

                // מנגנון ה-Debounce: אם המשתמש מקליד מהר, אנחנו מבטלים את הבקשה הקודמת ומחכים שיסיים.
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                if (s.length() < 2) return; // לא שולחים בקשה עבור פחות מ-2 תווים.

                // הגדרת הפעולה שתתבצע לאחר ההשהיה: פנייה ל-API של גוגל.
                debounceRunnable = () -> fetchSuggestions(s.toString().trim(), addressAdapter, etLocation);
                debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
            }
        });

        // מאזין כללי שבודק את מצב הכפתור בכל פעם שאחד משדות החובה משתנה.
        TextWatcher fieldsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePublishButtonState(btnPublish, etLocation, etDate, etTime, etGenre);
            }
        };
        etDate.addTextChangedListener(fieldsWatcher);
        etTime.addTextChangedListener(fieldsWatcher);
        etGenre.addTextChangedListener(fieldsWatcher);

        // לחיצה על כפתור הפרסום - מתחילה את תהליך המרת הכתובת לקואורדינטות ושמירה.
        btnPublish.setOnClickListener(v -> {
            if (selectedFromDropdown) {
                getCoordinatesAndSave(etLocation.getText().toString().trim(), etDate.getText().toString().trim(),
                        etTime.getText().toString().trim(), etGenre.getText().toString().trim());
            }
        });
    }

    // פונקציה המבצעת קריאת Retrofit אסינכרונית ל-Google Places API לקבלת רשימת כתובות מוצעות.
    private void fetchSuggestions(String query, ArrayAdapter<String> adapter, AutoCompleteTextView view) {
        PlacesApiService placesService = RetrofitPlacesClient.getApiService();
        placesService.autocomplete(query, GOOGLE_MAPS_API_KEY, "he", "country:il").enqueue(new Callback<PlacesAutoResponse>() {

            // Callback המופעל כאשר מתקבלת תשובה מהשרת.
            @Override
            public void onResponse(Call<PlacesAutoResponse> call, Response<PlacesAutoResponse> response) {
                if (!isAdded()) return; // בדיקה שהפרגמנט עדיין קיים כדי למנוע קריסה.

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "HTTP Error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                PlacesAutoResponse body = response.body();

                // בדיקה אם הסטטוס של גוגל תקין (למשל, שה-API Key לא חסום).
                if (body.getStatus() == null || !body.getStatus().equals("OK")) {
                    return;
                }

                // עדכון האדפטור ברשימת התוצאות החדשה והצגת הרשימה למשתמש.
                adapter.clear();
                for (PlacesAutoResponse.Prediction p : body.getPredictions()) {
                    adapter.add(p.getDescription());
                }
                adapter.notifyDataSetChanged();
                if (view.hasFocus()) view.showDropDown();
            }

            // Callback המופעל במקרה של שגיאת תקשורת (למשל, חוסר באינטרנט).
            @Override
            public void onFailure(Call<PlacesAutoResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Autocomplete failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // פונקציה ליצירת דיאלוג בחירת שעה.
    private void showTimePickerDialog(EditText etTime) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            // עיצוב הזמן בפורמט HH:mm (למשל 09:05 במקום 9:5).
            etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    // פונקציה ליצירת דיאלוג בחירת תאריך.
    private void showDatePickerDialog(EditText etDate) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            etDate.setText(day + "/" + (month + 1) + "/" + year);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // פונקציית וולידציה הבודקת את תקינות כל השדות ומפעילה/מכבה את כפתור הפרסום.
    private void updatePublishButtonState(Button btn, AutoCompleteTextView loc, EditText d, EditText t, EditText g) {
        boolean allFilled = !loc.getText().toString().trim().isEmpty() &&
                !d.getText().toString().trim().isEmpty() &&
                !t.getText().toString().trim().isEmpty() &&
                !g.getText().toString().trim().isEmpty();
        boolean enabled = allFilled && selectedFromDropdown;
        btn.setEnabled(enabled);
        btn.setAlpha(enabled ? 1f : 0.5f);
    }

    // פונקציה המבצעת קריאת Geocoding API כדי להפוך כתובת טקסטואלית למיקום גיאוגרפי (Lat/Lng).
    private void getCoordinatesAndSave(String address, String date, String time, String genre) {
        GeocodingApiService apiService = RetrofitClient.getApiService();
        apiService.getCoordinates(address, GOOGLE_MAPS_API_KEY).enqueue(new Callback<GeocodeResponse>() {
            @Override
            public void onResponse(Call<GeocodeResponse> call, Response<GeocodeResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                    // שליפת הקואורדינטות מהתוצאה הראשונה שחזרה מגוגל.
                    LocationModel loc = response.body().getResults().get(0).getGeometry().getLocation();
                    saveShowToFirebase(address, date, time, genre, loc.getLat(), loc.getLng());
                } else {
                    Toast.makeText(getContext(), "כתובת לא נמצאה במפה", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<GeocodeResponse> call, Throwable t) {
                Toast.makeText(getContext(), "שגיאת תקשורת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה המבצעת את השמירה הסופית ב-Firebase. היא שולפת נתוני אמן ואז דוחפת אובייקט Show חדש.
    private void saveShowToFirebase(String location, String date, String time, String genre, double lat, double lng) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // שליפת פרטי האמן כדי לקבל את שם הבמה וסוג האמן עבור אובייקט המופע.
        FirebaseDatabase.getInstance().getReference("Artists").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    // מופעל כשהנתונים חוזרים מה-Realtime Database.
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String artistType = snapshot.child("artistSubCategory").getValue(String.class);
                            String stageName = snapshot.child("stageName").getValue(String.class);

                            // יצירת מפתח ייחודי (Push Key) למופע החדש.
                            DatabaseReference showsRef = FirebaseDatabase.getInstance().getReference("Shows").push();

                            // יצירת המופע (10 פרמטרים כפי שהגדרנו בבנאי).
                            Show newShow = new Show(showsRef.getKey(), uid, stageName, artistType, location, time, genre, date, lat, lng);
                            newShow.setLive(false);

                            // כתיבת הנתונים ל-Firebase.
                            showsRef.setValue(newShow).addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "הופעה נוספה בהצלחה!", Toast.LENGTH_SHORT).show();
                                dismiss(); // סגירת הדיאלוג.
                            });
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching artist details", error.toException());
                    }
                });
    }

    // פונקציית Lifecycle המשמשת להגדרת הגודל הפיזי של חלון הדיאלוג על המסך.
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}