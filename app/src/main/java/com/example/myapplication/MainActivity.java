package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;

    // נקודת הכניסה של ה-Activity. כאן מאתחלים את כל ה-Singletons של Firebase (אימות, דאטה-בייס ואחסון קבצים).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();

        // אתחול מצב: מוודאים שאין משתמש מחובר בעת פתיחת האפליקציה כדי לאלץ כניסה מסודרת דרך מסך הלוגין.
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    // פונקציה המבצעת שאילתת "בדיקת זהות" ב-Database. מכיוון ש-Firebase Auth לא שומר סוג משתמש,
    // אנחנו בודקים אם ה-UID קיים תחת צומת ה-"Artists".
    private void checkUserTypeAndNavigate(String uid) {
        mDatabase.child("Artists").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                // שליחת התוצאה (קיום או אי-קיום האמן) לפונקציית הניווט.
                navigateToDestination(snapshot.exists());
            } else {
                Toast.makeText(this, "שגיאה באימות סוג משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה לרישום אמן חדש. היא מבצעת תהליך של 3 שלבים אסינכרוניים: 1. יצירת חשבון, 2. העלאת תמונה, 3. שמירת נתונים.
    public void registerNewArtist(String email, String password, Artist artistToSend, Uri imageUri) {
        // שלב 1: יצירת המשתמש ב-Firebase Authentication.
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) return;

            String uid = mAuth.getCurrentUser().getUid();
            // יצירת רפרנס לקובץ התמונה ב-Storage תחת שם ה-UID הייחודי.
            StorageReference storageRef = mStorage.getReference().child("artist_images/" + uid + ".jpg");

            // שלב 2: העלאת התמונה לשרת.
            // אנו משתמשים ב-continueWithTask כדי לשרשר את משימת קבלת ה-Download URL מיד לאחר סיום ההעלאה.
            storageRef.putFile(imageUri)
                    .continueWithTask(taskUpload -> {
                        if (!taskUpload.isSuccessful()) throw taskUpload.getException();
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
                        // שלב 3: שמירת אובייקט האמן המלא ב-Database כולל הקישור לתמונה שהתקבל הרגע.
                        artistToSend.setProfileImageUrl(downloadUrl.toString());

                        mDatabase.child("Artists").child(uid).setValue(artistToSend)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "אמן נרשם בהצלחה!", Toast.LENGTH_SHORT).show();
                                    backToLogin(); // חזרה למסך הלוגין לאחר הצלחה.
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "שגיאה בשמירה למסד: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה בהעלאת תמונה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    // פונקציה לרישום בליין (User) חדש. מבצעת לוגיקה זהה לזו של האמן אך שומרת את הנתונים תחת צומת ה-"Users".
    public void registerNewUser(String email, String password, User userToSend, Uri imageUri) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("user_images/" + uid + ".jpg");

            storageRef.putFile(imageUri)
                    .continueWithTask(taskUpload -> {
                        if (!taskUpload.isSuccessful()) throw taskUpload.getException();
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
                        userToSend.setProfileImageUrl(downloadUrl.toString());
                        mDatabase.child("Users").child(uid).setValue(userToSend)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "משתמש נרשם בהצלחה!", Toast.LENGTH_SHORT).show();
                                    backToLogin();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "שגיאה בשמירה למסד: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה בהעלאת תמונה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    // פונקציית לוגין מרכזית המופעלת מה-LoginFragment. היא מבצעת את האימות מול Firebase Auth
    // ואז קוראת לבדיקת סוג המשתמש כדי לדעת לאן לנווט.
    public void login(String email, String password, boolean isArtistFromUI) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // המשתמש אומת ב-Auth, עכשיו בודקים את סוג המשתמש ב-Database.
                checkUserTypeAndNavigate(mAuth.getCurrentUser().getUid());
            } else {
                Toast.makeText(this, "פרטי התחברות שגויים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה המנהלת את הניווט הסופי לאחר התחברות.
    // היא משתמשת ב-NavController ומגדירה NavOptions כדי לנקות את ה-Backstack.
    private void navigateToDestination(boolean isArtist) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // בחירת ה-ID של פעולת הניווט (Action) לפי סוג המשתמש.
            int actionId = isArtist ? R.id.action_loginFragment_to_artistDashboardFragment : R.id.action_loginFragment_to_mapFragment;

            // שימוש ב-NavOptions עם setPopUpTo(..., true) מבטיח שהמשתמש לא יוכל לחזור למסך הלוגין בלחיצה על "Back".
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build();

            navController.navigate(actionId, null, options);
        }
    }

    // פונקציית עזר המבצעת ניווט חזרה למסך הלוגין ומאפסת את מחסנית הניווט.
    private void backToLogin() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) return;
        NavController navController = navHostFragment.getNavController();
        navController.navigate(R.id.loginFragment, null,
                new NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        );
    }
}