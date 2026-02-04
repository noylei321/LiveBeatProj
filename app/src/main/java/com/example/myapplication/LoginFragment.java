package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. חיבור הרכיבים בדיוק לפי ה-IDs ב-XML שלך
        MaterialButtonToggleGroup toggleUserType = view.findViewById(R.id.toggleUserType);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        Button btnAction = view.findViewById(R.id.btnAction);
        TextView tvSwitchMode = view.findViewById(R.id.tvSwitchMode);

        // 2. כפתור כניסה (Login)
        btnAction.setOnClickListener(v -> {
            int checkedId = toggleUserType.getCheckedButtonId();

            if (checkedId == View.NO_ID) {
                Toast.makeText(getContext(), "נא לבחור סוג משתמש (בליין או אמן)", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show();
                return;
            }

            // בדיקה האם נבחר אמן
            boolean isArtist = (checkedId == R.id.btnTypeArtist);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).login(email, password, isArtist);
            }
        });

        // 3. כפתור "להרשמה" - הלוגיקה שביקשת
        tvSwitchMode.setOnClickListener(v -> {
            int checkedId = toggleUserType.getCheckedButtonId();

            if (checkedId == R.id.btnTypeUser) {
                // נבחר בליין -> עובר להרשמת משתמש
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerUserFragment);
            }
            else if (checkedId == R.id.btnTypeArtist) {
                // נבחר אמן -> עובר להרשמת אמן
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerArtistFragment);
            }
            else {
                // לא נבחר כלום
                Toast.makeText(getContext(), "נא לבחור קודם בליין 🎉 או אמן 🎤", Toast.LENGTH_LONG).show();
            }
        });
    }
}