package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList; // 🔹 שינוי: ייבוא עבור צבעי הצ'יפים
import android.graphics.Color; // 🔹 שינוי: ייבוא לצבע לבן
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils; // 🔹 שינוי: ייבוא עבור איחוד מחרוזות
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip; // 🔹 שינוי: ייבוא Chip
import com.google.android.material.chip.ChipGroup; // 🔹 שינוי: ייבוא ChipGroup
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView; // 🔹 שינוי: ייבוא רכיב החיפוש
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList; // 🔹 שינוי: ייבוא ArrayList
import java.util.Calendar;
import java.util.List; // 🔹 שינוי: ייבוא List

public class EditUserProfileFragment extends Fragment {

    // 🔹 הצהרה על כל משתני ה-UI של המחלקה
    private ShapeableImageView imgProfile;
    private EditText etFullName, etUsername, etEmail, etBirthDate, etPassword, etConfirmPassword, etPhone, etBio;
    private TextInputLayout tilConfirmPassword;

    // 🔹 שינוי: הצהרה על רכיבי הז'אנרים החדשים (החליפו את ה-Spinner)
    private ChipGroup cgUserSelectedGenres;
    private MaterialAutoCompleteTextView actvUserGenreSearch;

    private Button btnSave;
    private ProgressBar progressBar;
    private LinearLayout layoutPasswordFields, layoutTogglePassword;
    private ImageView imgPasswordChevron;

    private Uri newImageUri = null;
    private DatabaseReference mDatabase;
    private String uid;
    private User currentUser;

    // הגדרת ActivityResultLauncher המטפל בבחירת קובץ מדיה (תמונה) מהמכשיר.
    // הפונקציה האנונימית (Callback) מתבצעת ברגע שהמשתמש בחר תמונה ומחזירה URI המצביע עליה.
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                // בדיקה אם נבחרה תמונה (URI אינו Null) ועדכון הממשק הוויזואלי בהתאם.
                if (uri != null) {
                    newImageUri = uri;
                    imgProfile.setImageURI(uri);
                    imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });

    // פונקציית Lifecycle האחראית על ניפוח ה-XML והפיכתו לאובייקט View.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }

    // פונקציה המופעלת לאחר יצירת ה-View. משמשת לאתחול הגישה ל-Database ווידוא סטטוס התחברות של המשתמש.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // חילוץ ה-UID הייחודי של המשתמש המחובר לצורך גישה לנתוניו האישיים ב-DB.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews(view);      // אתחול רכיבי ה-UI.
        setupGenreSearch();    // 🔹 הגדרת מנגנון החיפוש וה-Autocomplete
        loadUserData();       // שליפת המידע הקיים מהשרת.
        setupListeners();     // הגדרת מאזינים לפעולות המשתמש.
    }

    // פונקציית עזר המבצעת את ה-Binding (קישור) בין המשתנים בקוד לבין רכיבי הממשק ב-XML.
    private void initViews(View view) {
        imgProfile = view.findViewById(R.id.imgEditUserProfile);
        etFullName = view.findViewById(R.id.etEditUserFullName);
        etUsername = view.findViewById(R.id.etEditUserUsername);
        etEmail = view.findViewById(R.id.etEditUserEmail);
        etBirthDate = view.findViewById(R.id.etEditUserBirthDate);
        etPassword = view.findViewById(R.id.etEditUserPassword);
        etConfirmPassword = view.findViewById(R.id.etEditUserConfirmPassword);
        etPhone = view.findViewById(R.id.etEditUserPhone);
        etBio = view.findViewById(R.id.etEditUserBio);

        // 🔹 קישור לרכיבי הז'אנרים החדשים
        cgUserSelectedGenres = view.findViewById(R.id.cgUserSelectedGenres);
        actvUserGenreSearch = view.findViewById(R.id.actvUserGenreSearch);

        btnSave = view.findViewById(R.id.btnSaveUserChanges);
        tilConfirmPassword = view.findViewById(R.id.tilEditUserConfirmPassword);
        progressBar = view.findViewById(R.id.progressBarEditUser);
        layoutPasswordFields = view.findViewById(R.id.layoutUserPasswordFields);
        layoutTogglePassword = view.findViewById(R.id.layoutUserTogglePassword);
        imgPasswordChevron = view.findViewById(R.id.imgUserPasswordChevron);
    }

    // הגדרת המאזינים לאירועי לחיצה על רכיבי הממשק השונים.
    private void setupListeners() {
        // מאזין ללחיצה על שורת הסיסמה: מבצע אנימציית סיבוב לחץ ומשנה את נראות שדות שינוי הסיסמה.
        layoutTogglePassword.setOnClickListener(v -> {
            if (layoutPasswordFields.getVisibility() == View.GONE) {
                layoutPasswordFields.setVisibility(View.VISIBLE);
                imgPasswordChevron.setRotation(180f);
            } else {
                layoutPasswordFields.setVisibility(View.GONE);
                imgPasswordChevron.setRotation(0f);
                // ניקוי השדות במידה והמשתמש התחרט וסגר את האופציה.
                etPassword.setText("");
                etConfirmPassword.setText("");
            }
        });

        // מאזין ללחיצה על שדה תאריך הלידה: פותח את ה-DatePickerDialog ומעדכן את הטקסט בבחירת המשתמש.
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                etBirthDate.setText(day + "/" + (month + 1) + "/" + year);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // פתיחת גלריית התמונות בעת לחיצה על תמונת הפרופיל או על ה-FAB.
        imgProfile.setOnClickListener(v -> mGetContent.launch("image/*"));

        View fab = getView().findViewById(R.id.fabEditUserPhoto);
        if (fab != null) {
            fab.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        // הפעלת פונקציית השמירה הראשית בעת לחיצה על כפתור השמירה.
        btnSave.setOnClickListener(v -> saveChanges());
    }

    // פונקציה השולפת את נתוני המשתמש (User Object) מה-Firebase Realtime Database ומאכלסת את השדות בטופס.
    private void loadUserData() {
        mDatabase.child("Users").child(uid).get().addOnSuccessListener(snapshot -> {
            currentUser = snapshot.getValue(User.class); // המרת ה-JSON לאובייקט Java מסוג User.
            if (currentUser != null) {
                etFullName.setText(currentUser.getFullName());
                etUsername.setText(currentUser.getUsername());
                etEmail.setText(currentUser.getEmail());
                etBirthDate.setText(currentUser.getBirthDate());
                etPhone.setText(currentUser.getPhone());
                etBio.setText(currentUser.getBio());

                // טעינת התמונה הקיימת בעזרת Glide לניהול זיכרון אופטימלי.
                if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                    Glide.with(this).load(currentUser.getProfileImageUrl()).into(imgProfile);
                }

                // 🔹 טעינת הז'אנרים השמורים והפיכתם לצ'יפים (מפרק את המחרוזת המופרדת בפסיקים)
                cgUserSelectedGenres.removeAllViews();
                String savedGenres = currentUser.getGenre();
                if (savedGenres != null && !savedGenres.isEmpty()) {
                    String[] genresArray = savedGenres.split(", ");
                    for (String g : genresArray) {
                        addUserGenreChip(g);
                    }
                }
            }
        });
    }

    // הפונקציה המרכזית לניהול עדכוני הפרופיל. כוללת וולידציה לשדות חובה וטיפול בשינוי סיסמה ב-FirebaseAuth.
    private void saveChanges() {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false); // מניעת לחיצות חוזרות בזמן עיבוד הנתונים.

        // בדיקה האם המשתמש בחר לעדכן את סיסמתו.
        if (layoutPasswordFields.getVisibility() == View.VISIBLE) {
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            // וולידציה לחוזק הסיסמה והתאמה בין השדות.
            if (pass.length() < 6) {
                etPassword.setError("מינימום 6 תווים");
                resetUIState();
                return;
            }
            if (!pass.equals(confirm)) {
                tilConfirmPassword.setError("הסיסמאות לא תואמות");
                resetUIState();
                return;
            }

            // עדכון הסיסמה ב-Firebase Authentication.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(pass).addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "הסיסמה עודכנה במערכת", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            resetUIState();
                            // טיפול בשגיאה הדורשת התחברות מחדש (Security Rule של גוגל לשינוי סיסמה).
                            if (e instanceof com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                Toast.makeText(getContext(), "עליך להתחבר מחדש כדי לשנות סיסמה", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }

        // 🔹 עדכון אובייקט ה-User המקומי בנתונים החדשים - תיקון שמות המשתנים העקביים
        currentUser.setFullName(etFullName.getText().toString().trim());
        currentUser.setBirthDate(etBirthDate.getText().toString().trim());
        currentUser.setPhone(etPhone.getText().toString().trim());
        currentUser.setBio(etBio.getText().toString().trim());

        // 🔹 איסוף הז'אנרים המעודכנים מהצ'יפים שנבחרו
        currentUser.setGenre(getSelectedGenres());

        // קבלת החלטה: העלאת תמונה חדשה ל-Storage או שמירת נתוני טקסט בלבד ל-Database.
        if (newImageUri != null) {
            uploadImageAndSave();
        } else {
            saveToDatabase();
        }
    }

    // 🔹 פונקציה להגדרת מנגנון החיפוש והצעות הז'אנרים
    private void setupGenreSearch() {
        String[] allGenres = getResources().getStringArray(R.array.music_genres);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, allGenres);
        actvUserGenreSearch.setAdapter(adapter);

        actvUserGenreSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            addUserGenreChip(selected);
            actvUserGenreSearch.setText("");
        });
    }

    // 🔹 פונקציית עזר ליצירת צ'יפ עם כפתור מחיקה
    private void addUserGenreChip(String text) {
        for (int i = 0; i < cgUserSelectedGenres.getChildCount(); i++) {
            if (((Chip) cgUserSelectedGenres.getChildAt(i)).getText().toString().equals(text)) return;
        }

        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> cgUserSelectedGenres.removeView(chip));

        chip.setChipBackgroundColorResource(R.color.beat_pink);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));

        cgUserSelectedGenres.addView(chip);
    }

    // 🔹 פונקציה האוספת את כל הטקסטים מהצ'יפים שנבחרו למחרוזת אחת
    private String getSelectedGenres() {
        List<String> selectedList = new ArrayList<>();
        for (int i = 0; i < cgUserSelectedGenres.getChildCount(); i++) {
            Chip chip = (Chip) cgUserSelectedGenres.getChildAt(i);
            selectedList.add(chip.getText().toString());
        }
        return TextUtils.join(", ", selectedList);
    }

    // פונקציה להעלאת קובץ המדיה ל-Firebase Storage. המרת ה-URI ל-Byte Array מבטיחה תהליך העלאה יציבה יותר.
    private void uploadImageAndSave() {
        byte[] data = getBytesFromUri(newImageUri);
        if (data == null) {
            resetUIState();
            return;
        }

        StorageReference ref = FirebaseStorage.getInstance().getReference().child("user_images/" + uid + ".jpg");
        ref.putBytes(data).addOnSuccessListener(task -> {
            // שליפת ה-URL הקבוע של התמונה לאחר העלאה מוצלחת ושמירתו באובייקט המשתמש.
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                currentUser.setProfileImageUrl(uri.toString());
                saveToDatabase();
            });
        }).addOnFailureListener(e -> {
            resetUIState();
            Toast.makeText(getContext(), "שגיאה בהעלאה", Toast.LENGTH_LONG).show();
        });
    }

    // הפעולה הסופית של כתיבת הנתונים המעודכנים ל-Database וניווט חזרה למסך הפרופיל האישי.
    private void saveToDatabase() {
        mDatabase.child("Users").child(uid).setValue(currentUser).addOnSuccessListener(unused -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה!", Toast.LENGTH_LONG).show();

            Bundle bundle = new Bundle();
            bundle.putString("userId", uid);

            // ניווט לפרגמנט הפרופיל תוך העברת ה-ID של המשתמש.
            Navigation.findNavController(requireView()).navigate(R.id.userProfileFragment, bundle);
        }).addOnFailureListener(e -> {
            resetUIState();
            Toast.makeText(getContext(), "שגיאה בשמירת הנתונים", Toast.LENGTH_SHORT).show();
        });
    }

    // פונקציית עזר לאיפוס מצב ה-UI
    private void resetUIState() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (btnSave != null) btnSave.setEnabled(true);
    }

    // פונקציית עזר טכנית הקוראת את זרם הנתונים (Stream) מה-URI של התמונה וממירה אותו למערך בייטים.
    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream iStream = requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}