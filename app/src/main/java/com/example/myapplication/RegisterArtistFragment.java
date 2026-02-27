package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color; // 🔹 שינוי: ייבוא עבור צבע טקסט לבן
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // 🔹 שינוי: ייבוא עבור האדפטור של החיפוש
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView; // 🔹 שינוי: ייבוא רכיב החיפוש
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterArtistFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private Uri imageUri = null;
    private MaterialButtonToggleGroup artistTypeToggleGroup;

    // 🔹 שינוי: רכיבי החיפוש והתצוגה הנגללת
    private ChipGroup cgSelectedGenres;
    private MaterialAutoCompleteTextView actvGenreSearch;

    private TextInputLayout tilFullName, tilStageName, tilBirthDate, tilInstrument,
            tilEmail, tilUserName, tilPassword, tilConfirmPassword,
            tilPhone;

    // הגדרת מנגנון בחירת תמונה מהגלריה המשתמש ב-Activity Result API.
    // הפונקציה האנונימית ב-Callback מטפלת ב-URI שמתקבל, ומבקשת הרשאה קבועה (Persistable Permission)
    // כדי להבטיח שהגישה לקובץ תישמר גם כשהנתונים עוברים ל-Activity המארח לצורך העלאה ל-Storage.
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    imageUri = uri;
                    imgProfile.setImageURI(uri);
                    imgProfile.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);
                }
            });

    public RegisterArtistFragment() { }

    // פונקציית Lifecycle המנפחת את ה-XML של טופס רישום האמן.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_artist, container, false);
    }

    // הפונקציה המרכזית לאתחול הפרגמנט. היא מקשרת את כל רכיבי ה-UI,
    // מגדירה את המאזינים (Listeners) לבחירת תאריך ותמונה, ומכילה את הלוגיקה המורכבת של כפתור ההרשמה.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgProfile = view.findViewById(R.id.imgArtistProfileUpload);
        FloatingActionButton fabAddPhoto = view.findViewById(R.id.FloatingActionButton);
        artistTypeToggleGroup = view.findViewById(R.id.artistTypeToggleGroup);

        EditText etFullName = view.findViewById(R.id.etArtistFullName);
        EditText etStageName = view.findViewById(R.id.etStageName);
        EditText etBirthDate = view.findViewById(R.id.etArtistBirthDate);
        EditText etInstrument = view.findViewById(R.id.etInstrument);
        EditText etEmail = view.findViewById(R.id.etArtistEmail);
        EditText etUserName = view.findViewById(R.id.etArtistUserName);
        EditText etPassword = view.findViewById(R.id.etArtistPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etArtistConfirmPassword);
        EditText etPhone = view.findViewById(R.id.etArtistPhone);
        EditText etSocialLink = view.findViewById(R.id.etSocialLink);
        EditText etDescription = view.findViewById(R.id.etDescription);

        // 🔹 שינוי: אתחול מנגנון החיפוש והז'אנרים
        cgSelectedGenres = view.findViewById(R.id.cgSelectedGenres);
        actvGenreSearch = view.findViewById(R.id.actvGenreSearch);
        setupGenreSearch();

        Button btnRegister = view.findViewById(R.id.btnRegisterArtist);

        tilFullName = view.findViewById(R.id.tilArtistFullName);
        tilStageName = view.findViewById(R.id.tilStageName);
        tilBirthDate = view.findViewById(R.id.tilArtistBirthDate);
        tilInstrument = view.findViewById(R.id.tilInstrument);
        tilEmail = view.findViewById(R.id.tilArtistEmail);
        tilUserName = view.findViewById(R.id.tilArtistUserName);
        tilPassword = view.findViewById(R.id.tilArtistPassword);
        tilConfirmPassword = view.findViewById(R.id.tilArtistConfirmPassword);
        tilPhone = view.findViewById(R.id.tilArtistPhone);

        // הגדרת מאזין לחיצה משותף לתמונת הפרופיל ולכפתור ה-FAB להפעלת גלריית התמונות.
        View.OnClickListener photoPicker = v -> mGetContent.launch("image/*");
        if (imgProfile != null) imgProfile.setOnClickListener(photoPicker);
        if (fabAddPhoto != null) fabAddPhoto.setOnClickListener(photoPicker);

        // פתיחת דיאלוג בחירת תאריך (DatePicker). בסיום הבחירה, הטקסט מעודכן בפורמט d/M/yyyy והשגיאה ב-Layout מתנקה.
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                String selectedDate = day + "/" + (month + 1) + "/" + year;
                etBirthDate.setText(selectedDate);
                if (tilBirthDate != null) tilBirthDate.setError(null);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // לוגיקת ההרשמה: מבצעת וולידציה מקיפה לכל שדות הטופס (ריק, אורך סיסמה, התאמת סיסמאות, גיל מינימלי)
        // ורק אם הכל תקין, יוצרת אובייקט Artist ושולחת אותו ל-MainActivity לביצוע הרישום ב-Firebase.
        btnRegister.setOnClickListener(v -> {
            clearErrors(); // ניקוי אינדיקציות שגיאה קודמות מה-UI.

            String fullName = etFullName.getText().toString().trim();
            String stageName = etStageName.getText().toString().trim();
            String instrument = etInstrument.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();
            String userName = etUserName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();
            String socialLink = etSocialLink.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            boolean isValid = true;

            // סדרת בדיקות וולידציה המשתמשות ב-setError של ה-TextInputLayout כדי לספק משוב ויזואלי למשתמש.
            if (fullName.isEmpty()) {
                if (tilFullName != null) tilFullName.setError("חובה למלא שם מלא");
                isValid = false;
            }
            if (stageName.isEmpty()) {
                if (tilStageName != null) tilStageName.setError("חובה למלא שם במה");
                isValid = false;
            }
            if (birthDate.isEmpty()) {
                if (tilBirthDate != null) tilBirthDate.setError("חובה לבחור תאריך");
                isValid = false;
            }
            if (instrument.isEmpty()) {
                if (tilInstrument != null) tilInstrument.setError("חובה למלא כלי נגינה");
                isValid = false;
            }
            if (email.isEmpty()) {
                if (tilEmail != null) tilEmail.setError("חובה למלא אימייל");
                isValid = false;
            }
            if (phone.isEmpty()) {
                if (tilPhone != null) tilPhone.setError("חובה למלא טלפון");
                isValid = false;
            }

            // בדיקת שם משתמש: מניעת שימוש בתווים בעברית (RegEx) כדי לשמור על עקביות ב-DB וב-URLs עתידיים.
            if (userName.isEmpty()) {
                if (tilUserName != null) tilUserName.setError("חובה למלא שם משתמש");
                isValid = false;
            } else if (userName.matches(".*[\\u0590-\\u05FF].*")) {
                if (tilUserName != null) tilUserName.setError("שם משתמש באנגלית בלבד");
                isValid = false;
            }

            // בדיקת אבטחה בסיסית: אורך סיסמה מינימלי והשוואה בין השדות למניעת טעויות הקלדה.
            if (password.isEmpty()) {
                if (tilPassword != null) tilPassword.setError("חובה להזין סיסמה");
                isValid = false;
            } else if (password.length() < 6) {
                if (tilPassword != null) tilPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
                isValid = false;
            } else if (confirmPass.isEmpty()) {
                if (tilConfirmPassword != null) tilConfirmPassword.setError("חובה לאמת את הסיסמה");
                isValid = false;
            } else if (!password.equals(confirmPass)) {
                if (tilConfirmPassword != null) tilConfirmPassword.setError("הסיסמאות אינן תואמות");
                isValid = false;
            }

            // קריאה לפונקציית עזר לחישוב גיל לוגי.
            if (!birthDate.isEmpty() && !isAgeValid(birthDate)) {
                if (tilBirthDate != null) tilBirthDate.setError("גיל 12 ומעלה בלבד");
                isValid = false;
            }

            if (imageUri == null) {
                Toast.makeText(getContext(), "חובה להוסיף תמונת פרופיל", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            // 🔹 שינוי: איסוף ז'אנרים שנבחרו לתוך מחרוזת מופרדת בפסיקים
            String genres = getSelectedGenres();
            if (genres.isEmpty()) {
                Toast.makeText(getContext(), "נא לבחור לפחות ז'אנר אחד", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) return;

            // שליפת הערך מה-ToggleGroup לקביעת תת-הקטגוריה של האמן.
            int checkedId = artistTypeToggleGroup.getCheckedButtonId();
            String subCategory = "Musician";
            if (checkedId == R.id.btnTypeDJ) subCategory = "DJ";
            else if (checkedId == R.id.btnTypeComedian) subCategory = "Comedian";

            // בניית אובייקט ה-Artist המלא (Model) מוכן לשליחה לשכבת הנתונים.
            Artist artistToSend = new Artist(
                    fullName, stageName, subCategory, birthDate, instrument,
                    email, userName, phone, socialLink, description,
                    genres, // 🔹 שינוי: שליחת רשימת הז'אנרים
                    ""
            );

            // העברת המשימה ל-MainActivity לביצוע הרישום האמיתי מול Firebase Authentication ו-Storage.
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).registerNewArtist(email, password, artistToSend, imageUri);
            }
        });
    }

    // 🔹 שינוי: הגדרת מנגנון החיפוש והוספת צ'יפים
    private void setupGenreSearch() {
        String[] allGenres = getResources().getStringArray(R.array.music_genres);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, allGenres);
        actvGenreSearch.setAdapter(adapter);

        // הוספת ז'אנר בלחיצה על הצעה מהרשימה
        actvGenreSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            addGenreChip(selected);
            actvGenreSearch.setText(""); // ניקוי השדה
        });
    }

    // 🔹 שינוי: פונקציית עזר ליצירת צ'יפ עם כפתור מחיקה
    private void addGenreChip(String text) {
        // מניעת כפילויות
        for (int i = 0; i < cgSelectedGenres.getChildCount(); i++) {
            if (((Chip) cgSelectedGenres.getChildAt(i)).getText().toString().equals(text)) return;
        }

        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> cgSelectedGenres.removeView(chip));

        // עיצוב הצ'יפ שנבחר
        chip.setChipBackgroundColorResource(R.color.beat_primary);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));

        cgSelectedGenres.addView(chip);
    }

    // 🔹 שינוי: פונקציה האוספת את כל הטקסטים מהצ'יפים שנבחרו
    private String getSelectedGenres() {
        List<String> selectedList = new ArrayList<>();
        for (int i = 0; i < cgSelectedGenres.getChildCount(); i++) {
            Chip chip = (Chip) cgSelectedGenres.getChildAt(i);
            selectedList.add(chip.getText().toString());
        }
        return TextUtils.join(", ", selectedList);
    }

    // פונקציית עזר המאפסת את מצב השגיאה (Error State) של כל רכיבי ה-Material Design בטופס.
    private void clearErrors() {
        if (tilFullName != null) tilFullName.setError(null);
        if (tilStageName != null) tilStageName.setError(null);
        if (tilBirthDate != null) tilBirthDate.setError(null);
        if (tilInstrument != null) tilInstrument.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilUserName != null) tilUserName.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
    }

    // פונקציה המחשבת את הפרש השנים בין תאריך הלידה לזמן הנוכחי כדי לוודא עמידה בתנאי הגיל המינימלי (12).
    private boolean isAgeValid(String dateStr) {
        try {
            String[] parts = dateStr.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);
            Calendar dob = Calendar.getInstance();
            dob.set(year, month, day);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            // תיקון חישוב אם יום ההולדת טרם חל בשנה הנוכחית.
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) { age--; }
            return age >= 12;
        } catch (Exception e) { return false; }
    }
}