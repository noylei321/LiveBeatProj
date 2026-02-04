package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView; // הוספתי בשביל להציג שגיאה על הספינר אם צריך
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Pattern;

public class RegisterUserFragment extends Fragment {

    public RegisterUserFragment() {
        // בנאי ריק
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. חיבור לשדות
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etUserName = view.findViewById(R.id.etUserName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etBirthDate = view.findViewById(R.id.etBirthDate);
        Spinner spinnerGenre = view.findViewById(R.id.spinnerGenre);
        Button btnRegister = view.findViewById(R.id.btnRegisterUser);

        // 2. לוגיקה של לוח שנה
        etBirthDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year1, monthOfYear, dayOfMonth) -> {
                        etBirthDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // 3. כפתור הרשמה
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {

                String fullName = etFullName.getText().toString().trim();
                String userName = etUserName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String birthDate = etBirthDate.getText().toString().trim();

                // שליפת הז'אנר
                String genre = "";
                if (spinnerGenre != null && spinnerGenre.getSelectedItem() != null) {
                    genre = spinnerGenre.getSelectedItem().toString();
                }

                // --- בדיקות חובה (Validation) ---
                if (fullName.isEmpty()) { etFullName.setError("חובה למלא שם"); return; }
                if (userName.isEmpty()) { etUserName.setError("חובה למלא כינוי"); return; }
                if (email.isEmpty()) { etEmail.setError("חובה למלא אימייל"); return; }
                if (phone.isEmpty()) { etPhone.setError("חובה למלא טלפון"); return; }
                if (birthDate.isEmpty()) {
                    etBirthDate.setError("חובה לבחור תאריך");
                    Toast.makeText(getContext(), "נא לבחור תאריך", Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- בדיקת הספינר החדשה! 🛑 ---
                // אם המיקום הוא 0 (הראשון ברשימה) - זה אומר שהוא לא בחר כלום
                if (spinnerGenre.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "חובה לבחור סגנון מוזיקה מהרשימה!", Toast.LENGTH_LONG).show();

                    // טריק קטן: זה פותח את הרשימה אוטומטית למשתמש כדי שייזכר לבחור
                    spinnerGenre.performClick();
                    return;
                }

                // --- בדיקות סיסמה ---
                if (password.length() < 6) {
                    etPassword.setError("סיסמה מינימום 6 תווים");
                    return;
                }
                if (!password.matches(".*[A-Z].*")) {
                    etPassword.setError("חובה אות גדולה באנגלית");
                    return;
                }
                if (!password.matches(".*[!@#$%^&*].*")) {
                    etPassword.setError("חובה סימן מיוחד (!@#$)");
                    return;
                }

                // --- הכל תקין! שולחים ל-Main ---
                User userToSend = new User(fullName, userName, email, phone, birthDate, genre);

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).registerNewUser(email, password, userToSend);
                }
            });
        }
    }
}