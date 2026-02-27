package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList; // 🔹 שינוי: ייבוא עבור צבעי הצ'יפים
import android.graphics.Color; // 🔹 שינוי: ייבוא לצבע טקסט לבן
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils; // 🔹 שינוי: ייבוא עבור איחוד מחרוזות
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
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip; // 🔹 שינוי: ייבוא Chip
import com.google.android.material.chip.ChipGroup; // 🔹 שינוי: ייבוא ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView; // 🔹 שינוי: ייבוא רכיב החיפוש
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List; // 🔹 שינוי: ייבוא List

public class RegisterUserFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private Uri imageUri = null;

    private TextInputLayout tilFullName, tilUsername, tilPassword, tilConfirmPassword, tilEmail, tilPhone, tilBirthDate, tilBio;

    // 🔹 שינוי: הוספת רכיבי החיפוש והבחירה החדשים
    private ChipGroup cgUserSelectedGenres;
    private MaterialAutoCompleteTextView actvUserGenreSearch;

    // הגדרת משגר (Launcher) לטיפול בתוצאה של פעילות חיצונית (בחירת תמונה מהגלריה).
    // הפונקציה מבקשת הרשאה קבועה (Persistable URI) כדי להבטיח שהגישה לקובץ לא תתבטל במעבר בין ה-Fragment ל-Activity.
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

    public RegisterUserFragment() { }

    // פונקציית Lifecycle המנפחת (Inflating) את קובץ ה-XML של טופס רישום המשתמש לתוך הזיכרון.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_user, container, false);
    }

    // הפונקציה המרכזית להגדרת הממשק לאחר יצירתו. מקשרת את כל רכיבי ה-UI (Binding),
    // מגדירה מאזינים להקלקות ומטפלת בלוגיקה של אימות הטופס (Validation).
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgProfile = view.findViewById(R.id.imgUserProfileUpload);
        FloatingActionButton fabAddPhoto = view.findViewById(R.id.FloatingActionButtonUser);

        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etUserName = view.findViewById(R.id.etUserName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etBirthDate = view.findViewById(R.id.etBirthDate);
        EditText etBio = view.findViewById(R.id.etBio);

        tilFullName = view.findViewById(R.id.tilFullName);
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilBirthDate = view.findViewById(R.id.tilBirthDate);
        tilBio = view.findViewById(R.id.tilBio);

        // 🔹 שינוי: אתחול רכיבי הז'אנרים החדשים
        cgUserSelectedGenres = view.findViewById(R.id.cgUserSelectedGenres);
        actvUserGenreSearch = view.findViewById(R.id.actvUserGenreSearch);
        setupGenreSearch();

        Button btnRegister = view.findViewById(R.id.btnRegisterUser);

        // הגדרת מאזין לחיצה משותף להפעלת בחירת תמונה מהגלריה.
        View.OnClickListener photoPicker = v -> mGetContent.launch("image/*");
        imgProfile.setOnClickListener(photoPicker);
        fabAddPhoto.setOnClickListener(photoPicker);

        // מאזין ללחיצה על שדה התאריך המפעיל דיאלוג בחירה (DatePicker).
        // בסיום הבחירה, הוא מעדכן את השדה ומנקה הודעות שגיאה קודמות במידה והיו.
        etBirthDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                String selectedDate = day + "/" + (month + 1) + "/" + year;
                etBirthDate.setText(selectedDate);
                if (tilBirthDate != null) tilBirthDate.setError(null);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // הפונקציה המרכזית המופעלת בעת לחיצה על כפתור הרישום.
        // היא מבצעת וולידציה (תיקוף) לכל שדות החובה בטופס, בודקת תקינות סיסמאות ובחירת תמונה.
        // אם הכל תקין, היא יוצרת אובייקט User ומעבירה אותו ל-MainActivity לביצוע הרישום בפועל.
        btnRegister.setOnClickListener(v -> {
            clearErrors(); // ניקוי אינדיקציות שגיאה קודמות מהממשק.

            String fullName = etFullName.getText().toString().trim();
            String username = etUserName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            // 🔹 שינוי: איסוף הז'אנרים הנבחרים מהצ'יפים
            String genre = getSelectedGenres();

            boolean isValid = true;

            // בדיקות וולידציה לשדות חובה. שימוש ב-setError של TextInputLayout מספק משוב ויזואלי למשתמש.
            if (fullName.isEmpty()) { tilFullName.setError("חובה למלא שם"); isValid = false; }
            if (username.isEmpty()) { tilUsername.setError("חובה למלא כינוי"); isValid = false; }
            if (email.isEmpty()) { tilEmail.setError("חובה למלא אימייל"); isValid = false; }
            if (phone.isEmpty()) { tilPhone.setError("חובה למלא טלפון"); isValid = false; }
            if (birthDate.isEmpty()) { tilBirthDate.setError("חובה לבחור תאריך"); isValid = false; }

            // 🔹 שינוי: וולידציה לבחירת ז'אנר (בדיקה אם המחרוזת ריקה)
            if (genre.isEmpty()) {
                Toast.makeText(getContext(), "חובה לבחור לפחות סגנון מוזיקה אחד!", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            // וולידציה לקיום תמונת פרופיל (הכרחי להצגה במפה ובדירוגים).
            if (imageUri == null) {
                Toast.makeText(getContext(), "חובה להוסיף תמונת פרופיל", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            // לוגיקת אימות סיסמה: אורך מינימלי והתאמה בין השדות למניעת טעויות הקלדה.
            if (password.length() < 6) {
                tilPassword.setError("סיסמה מינימום 6 תווים");
                isValid = false;
            } else if (confirmPass.isEmpty()) {
                tilConfirmPassword.setError("חובה לאמת סיסמה");
                isValid = false;
            } else if (!password.equals(confirmPass)) {
                tilConfirmPassword.setError("הסיסמאות אינן תואמות");
                isValid = false;
            }

            if (!isValid) return;

            // יצירת אובייקט Model המכיל את כל נתוני הבליין.
            User userToSend = new User(fullName, username, email, phone, birthDate, genre, bio, "");

            // העברת המשימה האסינכרונית ל-Activity המארח (Delegation) לביצוע הרישום ב-Firebase.
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).registerNewUser(email, password, userToSend, imageUri);
            }
        });
    }

    // 🔹 שינוי: פונקציה להגדרת מנגנון החיפוש (Autocomplete)
    private void setupGenreSearch() {
        String[] allGenres = getResources().getStringArray(R.array.music_genres);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, allGenres);
        actvUserGenreSearch.setAdapter(adapter);

        actvUserGenreSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            addGenreChip(selected);
            actvUserGenreSearch.setText(""); // ניקוי השדה להזנה הבאה
        });
    }

    // 🔹 שינוי: פונקציית עזר ליצירת צ'יפ עם כפתור מחיקה (X)
    private void addGenreChip(String text) {
        for (int i = 0; i < cgUserSelectedGenres.getChildCount(); i++) {
            if (((Chip) cgUserSelectedGenres.getChildAt(i)).getText().toString().equals(text)) return;
        }

        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> cgUserSelectedGenres.removeView(chip));

        // עיצוב הצ'יפ שנבחר (שימוש ב-beat_pink כפי שמופיע בכפתור הרישום)
        chip.setChipBackgroundColorResource(R.color.beat_pink);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));

        cgUserSelectedGenres.addView(chip);
    }

    // 🔹 שינוי: פונקציה האוספת את כל הטקסטים מהצ'יפים שנבחרו למחרוזת אחת
    private String getSelectedGenres() {
        List<String> selectedList = new ArrayList<>();
        for (int i = 0; i < cgUserSelectedGenres.getChildCount(); i++) {
            Chip chip = (Chip) cgUserSelectedGenres.getChildAt(i);
            selectedList.add(chip.getText().toString());
        }
        return TextUtils.join(", ", selectedList);
    }

    // פונקציית עזר המנקה את כל הודעות השגיאה מה-TextInputLayouts במסך לקראת בדיקה חדשה.
    private void clearErrors() {
        if (tilFullName != null) tilFullName.setError(null);
        if (tilUsername != null) tilUsername.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilBirthDate != null) tilBirthDate.setError(null);
        if (tilBio != null) tilBio.setError(null);
    }
}