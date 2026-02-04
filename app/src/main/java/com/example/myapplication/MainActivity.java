package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void registerNewArtist(String email, String password, Artist artistToSend, Uri imageUri) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("artist_images/" + uid + ".jpg");

                storageRef.putFile(imageUri).continueWithTask(taskUpload -> {
                    if (!taskUpload.isSuccessful()) throw taskUpload.getException();
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(taskUrl -> {
                    if (taskUrl.isSuccessful()) {
                        artistToSend.setProfileImageUrl(taskUrl.getResult().toString());
                        mDatabase.child("Artists").child(uid).setValue(artistToSend);
                        Toast.makeText(this, "אמן נרשם בהצלחה!", Toast.LENGTH_SHORT).show();

                        // חזרה למסך הלוגין
                        backToLogin();
                    }
                });
            } else {
                Toast.makeText(this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void registerNewUser(String email, String password, User userToSend) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                mDatabase.child("Users").child(uid).setValue(userToSend);
                Toast.makeText(this, "משתמש נרשם בהצלחה!", Toast.LENGTH_SHORT).show();

                // חזרה למסך הלוגין
                backToLogin();
            }
        });
    }

    public void login(String email, String password, boolean isArtist) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    if (isArtist) navController.navigate(R.id.action_loginFragment_to_artistDashboardFragment);
                    else navController.navigate(R.id.action_loginFragment_to_mapFragment);
                }
            } else {
                Toast.makeText(this, "פרטי התחברות שגויים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void backToLogin() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navHostFragment.getNavController().popBackStack();
        }
    }
}