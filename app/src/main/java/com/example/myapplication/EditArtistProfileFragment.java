package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class EditArtistProfileFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private EditText etFullName, etStageName, etUsername, etEmail, etBirthDate, etPassword, etConfirmPassword, etPhone, etDescription, etSocialLink, etInstrument;
    private TextInputLayout tilConfirmPassword;
    private Spinner spinnerGenre;
    private Button btnSave;
    private ProgressBar progressBar;
    private FloatingActionButton fabEditPhoto;
    private LinearLayout layoutPasswordFields, layoutTogglePassword;
    private ImageView imgPasswordChevron;

    private Uri newImageUri = null;
    private DatabaseReference mDatabase;
    private String uid;
    private Artist currentArtist;

    // שימוש ב-ActivityResultLauncher כחלק מה-Activity Result API החדש (המחליף את onActivityResult).
    // הוא משמש לבחירת תמונה מהגלריה של המכשיר בצורה בטוחה ועדכון ה-UI.
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // בקשת הרשאה קבועה לקריאת ה-URI כדי למנוע אובדן גישה לתמונה לאחר סגירת האפליקציה.
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) { e.printStackTrace(); }
                    newImageUri = uri;
                    imgProfile.setImageURI(uri);
                    imgProfile.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);
                }
            });

    // פונקציית Lifecycle המנפחת את קובץ ה-XML והופכת אותו ל-View.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_artist_profile, container, false);
    }

    // פונקציה המופעלת לאחר יצירת ה-View. מאתחלת את ה-Database, שולפת את ה-UID של המשתמש ומתחילה את תהליך טעינת הנתונים.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews(view);      // קישור רכיבי הממשק.
        loadCurrentData();    // שליפת הנתונים הקיימים מהשרת.
        setupListeners();     // הגדרת מאזינים לפעולות המשתמש.
    }

    // פונקציית עזר המבצעת את ה-Binding בין רכיבי ה-XML למשתנים בקוד ה-Java.
    private void initViews(View view) {
        imgProfile = view.findViewById(R.id.imgEditArtistProfile);
        etFullName = view.findViewById(R.id.etEditFullName);
        etStageName = view.findViewById(R.id.etEditStageName);
        etUsername = view.findViewById(R.id.etEditUsername);
        etEmail = view.findViewById(R.id.etEditEmail);
        etBirthDate = view.findViewById(R.id.etEditBirthDate);
        etPassword = view.findViewById(R.id.etEditPassword);
        etConfirmPassword = view.findViewById(R.id.etEditConfirmPassword);
        etPhone = view.findViewById(R.id.etEditPhone);
        etDescription = view.findViewById(R.id.etEditDescription);
        etSocialLink = view.findViewById(R.id.etEditSocialLink);
        etInstrument = view.findViewById(R.id.etEditInstrument);
        spinnerGenre = view.findViewById(R.id.spinnerEditGenre);
        btnSave = view.findViewById(R.id.btnSaveArtistChanges);
        tilConfirmPassword = view.findViewById(R.id.tilEditConfirmPassword);
        progressBar = view.findViewById(R.id.progressBarEditArtist);
        fabEditPhoto = view.findViewById(R.id.fabEditPhoto);
        layoutPasswordFields = view.findViewById(R.id.layoutPasswordFields);
        layoutTogglePassword = view.findViewById(R.id.layoutTogglePassword);
        imgPasswordChevron = view.findViewById(R.id.imgPasswordChevron);
    }

    // הגדרת המאזינים (Listeners) לאירועים שונים במסך.
    private void setupListeners() {
        // ניהול מצבי נראות (Toggle) של שדות הסיסמה - שינוי סיסמה הוא אופציונלי בלבד.
        layoutTogglePassword.setOnClickListener(v -> {
            if (layoutPasswordFields.getVisibility() == View.GONE) {
                layoutPasswordFields.setVisibility(View.VISIBLE);
                imgPasswordChevron.setRotation(180f); // סיבוב האנימציה של החץ.
            } else {
                layoutPasswordFields.setVisibility(View.GONE);
                imgPasswordChevron.setRotation(0f);
                etPassword.setText("");
                etConfirmPassword.setText("");
            }
        });

        // פתיחת דיאלוג בחירת תאריך מובנה של אנדרואיד.
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                etBirthDate.setText(day + "/" + (month + 1) + "/" + year);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // הפעלת ה-Launcher לבחירת תמונה במידה ולחצו על הכפתור או על התמונה עצמה.
        fabEditPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        imgProfile.setOnClickListener(v -> mGetContent.launch("image/*"));

        // קריאה לפונקציית השמירה הראשית.
        btnSave.setOnClickListener(v -> saveChanges());
    }

    // פונקציה השולפת את הנתונים הנוכחיים של האמן מ-Firebase Realtime Database ומאכלסת את השדות בטופס.
    private void loadCurrentData() {
        mDatabase.child("Artists").child(uid).get().addOnSuccessListener(snapshot -> {
            currentArtist = snapshot.getValue(Artist.class);
            if (currentArtist != null) {
                etFullName.setText(currentArtist.getFullName());
                etStageName.setText(currentArtist.getStageName());
                etUsername.setText(currentArtist.getUsername());
                etEmail.setText(currentArtist.getEmail());
                etBirthDate.setText(currentArtist.getBirthDate());
                etPhone.setText(currentArtist.getPhone());
                etDescription.setText(currentArtist.getBio());
                etSocialLink.setText(currentArtist.getSocialLink());
                etInstrument.setText(currentArtist.getInstrument());

                if (currentArtist.getProfileImageUrl() != null && !currentArtist.getProfileImageUrl().isEmpty()) {
                    Glide.with(this).load(currentArtist.getProfileImageUrl()).into(imgProfile);
                }

                // עדכון ה-Spinner (ז'אנר) לערך שנשמר ב-Database.
                if (spinnerGenre.getAdapter() != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGenre.getAdapter();
                    int pos = adapter.getPosition(currentArtist.getGenre());
                    if (pos >= 0) spinnerGenre.setSelection(pos);
                }
            }
        });
    }

    // הפונקציה המרכזית לניהול תהליך השמירה. היא כוללת וולידציה, עדכון סיסמה ב-Auth, ועדכון נתונים ב-DB.
    private void saveChanges() {
        if (currentArtist == null) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // עדכון סיסמה מול Firebase Authentication - דורש טיפול מיוחד בגלל מגבלות אבטחה.
        if (layoutPasswordFields.getVisibility() == View.VISIBLE) {
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

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

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(pass).addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "הסיסמה עודכנה במערכת", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            resetUIState();
                            // טיפול במקרה של "Recent Login Required" - גוגל דורשת התחברות טרייה לשינוי סיסמה.
                            if (e instanceof com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                Toast.makeText(getContext(), "עליך להתחבר מחדש כדי לשנות סיסמה", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }

        // עדכון המידע באובייקט ה-Java הנוכחי לפני שמירתו ב-Database.
        currentArtist.setFullName(etFullName.getText().toString().trim());
        currentArtist.setStageName(etStageName.getText().toString().trim());
        currentArtist.setBirthDate(etBirthDate.getText().toString().trim());
        currentArtist.setPhone(etPhone.getText().toString().trim());
        currentArtist.setBio(etDescription.getText().toString().trim());
        currentArtist.setSocialLink(etSocialLink.getText().toString().trim());
        currentArtist.setInstrument(etInstrument.getText().toString().trim());
        currentArtist.setGenre(spinnerGenre.getSelectedItem().toString());

        // החלטה האם להעלות תמונה חדשה או רק לעדכן נתוני טקסט.
        if (newImageUri != null) {
            uploadImageAndSave();
        } else {
            saveToDatabase();
        }
    }

    // פונקציה המבצעת את העלאת התמונה ל-Firebase Storage.
    // שימוש ב-putBytes במקום ב-Uri ישיר פותר בעיות של הרשאות גישה זמניות באנדרואיד.
    private void uploadImageAndSave() {
        byte[] data = getBytesFromUri(newImageUri);
        if (data == null) {
            resetUIState();
            return;
        }

        StorageReference ref = FirebaseStorage.getInstance().getReference().child("artist_images/" + uid + ".jpg");
        ref.putBytes(data).addOnSuccessListener(task -> {
            // לאחר העלאה מוצלחת, אנו שולפים את ה-Download URL הקבוע כדי לשמור אותו ב-Database.
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                currentArtist.setProfileImageUrl(uri.toString());
                saveToDatabase();
            });
        }).addOnFailureListener(e -> {
            resetUIState();
            Toast.makeText(getContext(), "שגיאה בהעלאת התמונה", Toast.LENGTH_LONG).show();
        });
    }

    // הפעולה הסופית של כתיבת אובייקט ה-Artist המעודכן לתוך ה-Realtime Database.
    private void saveToDatabase() {
        mDatabase.child("Artists").child(uid).setValue(currentArtist).addOnSuccessListener(unused -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "הפרופיל עודכן!", Toast.LENGTH_LONG).show();

            // ניווט חזרה למסך הפרופיל לצורך הצגת השינויים שבוצעו.
            Bundle bundle = new Bundle();
            bundle.putString("artistId", uid);
            Navigation.findNavController(requireView()).navigate(R.id.artistProfileFragment, bundle);
        }).addOnFailureListener(e -> {
            resetUIState();
            Toast.makeText(getContext(), "שגיאה בשמירת הנתונים", Toast.LENGTH_SHORT).show();
        });
    }

    // פונקציית עזר המממשת את עקרון ה-DRY (Don't Repeat Yourself) לאיפוס מצב ה-UI במקרה של שגיאה או סיום.
    private void resetUIState() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (btnSave != null) btnSave.setEnabled(true);
    }

    // פונקציה טכנית הממירה את ה-URI של התמונה למערך של בייטים (byte[]) לצורך שליחה יציבה לשרת.
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
        } catch (Exception e) { return null; }
    }
}