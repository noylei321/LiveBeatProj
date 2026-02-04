package com.example.myapplication;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Calendar;

public class RegisterArtistFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private Uri imageUri = null;

    // מנגנון בחירת תמונה מהגלריה
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imgProfile.setImageURI(uri);
                    imgProfile.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);
                }
            });

    public RegisterArtistFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_artist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- חיבור פקדים בדיוק לפי ה-IDs ב-XML שלך ---

        imgProfile = view.findViewById(R.id.imgArtistProfileUpload);
        FloatingActionButton fabAddPhoto = view.findViewById(R.id.FloatingActionButton); // תואם ל-XML

        EditText etFullName = view.findViewById(R.id.etArtistFullName);
        EditText etStageName = view.findViewById(R.id.etStageName);
        EditText etBirthDate = view.findViewById(R.id.etArtistBirthDate);
        EditText etInstrument = view.findViewById(R.id.etInstrument);
        EditText etEmail = view.findViewById(R.id.etArtistEmail);
        EditText etUserName = view.findViewById(R.id.etArtistUserName);
        EditText etPassword = view.findViewById(R.id.etArtistPassword);
        EditText etPhone = view.findViewById(R.id.etArtistPhone);
        EditText etSocialLink = view.findViewById(R.id.etSocialLink);
        EditText etDescription = view.findViewById(R.id.etDescription);

        Spinner spinnerGenre = view.findViewById(R.id.spinnerArtistGenre);
        Button btnRegister = view.findViewById(R.id.btnRegisterArtist);

        // --- הגדרת מאזינים (Listeners) ---

        // לחיצה על התמונה או הפלוס לפתיחת גלריה
        View.OnClickListener photoPicker = v -> mGetContent.launch("image/*");
        if (imgProfile != null) imgProfile.setOnClickListener(photoPicker);
        if (fabAddPhoto != null) fabAddPhoto.setOnClickListener(photoPicker);

        // לחיצה על תאריך לידה
        if (etBirthDate != null) {
            etBirthDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                    etBirthDate.setText(day + "/" + (month + 1) + "/" + year);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        // כפתור הרשמה סופי
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                if (imageUri == null) {
                    Toast.makeText(getContext(), "חובה לבחור תמונה", Toast.LENGTH_SHORT).show();
                    return;
                }

                // איסוף הנתונים לתוך אובייקט Artist
                Artist artistToSend = new Artist(
                        etFullName.getText().toString().trim(),
                        etStageName.getText().toString().trim(),
                        etBirthDate.getText().toString().trim(),
                        etInstrument.getText().toString().trim(),
                        etEmail.getText().toString().trim(),
                        etUserName.getText().toString().trim(),
                        etPhone.getText().toString().trim(),
                        etSocialLink.getText().toString().trim(),
                        etDescription.getText().toString().trim(),
                        spinnerGenre.getSelectedItem() != null ? spinnerGenre.getSelectedItem().toString() : "",
                        "" // ה-URL יישמר אחרי העלאה ל-Storage
                );

                // קריאה לפונקציית ההרשמה ב-MainActivity
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).registerNewArtist(
                            etEmail.getText().toString().trim(),
                            etPassword.getText().toString().trim(),
                            artistToSend,
                            imageUri
                    );
                }
            });
        }
    }
}