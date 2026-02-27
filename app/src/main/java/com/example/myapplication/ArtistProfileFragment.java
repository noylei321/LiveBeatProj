package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ArtistProfileFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private TextView tvStageName, tvArtistType, tvBio, tvInstrument, tvFullName, tvBirthDate;
    private ChipGroup cgGenres;

    // 🔹 שינוי: הוספת רכיבי רשימת היסטוריית הופעות
    private RecyclerView rvHistory;
    private ShowsAdapter historyAdapter;
    private ArrayList<Show> historyList;

    private MaterialButton btnInstagram, btnPhone, btnEmail;

    private String instagramLink = "";
    private String phoneNumber = "";
    private String emailAddress = "";

    private String artistId;
    private boolean fromShowList = false;

    public ArtistProfileFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistId = getArguments().getString("artistId");
            fromShowList = getArguments().getBoolean("fromShowList", false);
        }

        if (artistId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            artistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgProfile = view.findViewById(R.id.profileImg);
        tvStageName = view.findViewById(R.id.tvStageName);
        tvArtistType = view.findViewById(R.id.tvArtistType);
        tvBio = view.findViewById(R.id.tvBio);
        cgGenres = view.findViewById(R.id.cgProfileGenres);
        tvInstrument = view.findViewById(R.id.tvInstrument);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);

        btnInstagram = view.findViewById(R.id.btnInstagram);
        btnPhone = view.findViewById(R.id.btnPhone);
        btnEmail = view.findViewById(R.id.btnEmail);

        // 🔹 שינוי: אתחול ה-RecyclerView להיסטוריה
        rvHistory = view.findViewById(R.id.rvArtistHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList = new ArrayList<>();
        // העברת 'false' כי אנחנו לא רוצים לאפשר מחיקה מהפרופיל
        historyAdapter = new ShowsAdapter(historyList, false);
        rvHistory.setAdapter(historyAdapter);

        TextView tvBack = view.findViewById(R.id.tvBackToDashboard);
        tvBack.setPaintFlags(tvBack.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        if (fromShowList) {
            tvBack.setText("חזרה לבקשות 🎸");
            tvBack.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        } else {
            tvBack.setText("חזרה למסך הראשי");
            tvBack.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_artistProfileFragment_to_artistDashboardFragment);
            });
        }

        if (artistId != null) {
            loadArtistData(artistId);
            loadArtistHistory(); // 🔹 שינוי: טעינת ההופעות של האמן
        } else {
            Toast.makeText(getContext(), "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
        }

        setupActionButtons();
    }

    // 🔹 שינוי: פונקציה חדשה לשליפת ההופעות ששייכות לאמן זה בלבד
    private void loadArtistHistory() {
        FirebaseDatabase.getInstance().getReference("Shows")
                .orderByChild("artistId").equalTo(artistId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Show s = ds.getValue(Show.class);
                            if (s != null) historyList.add(0, s); // החדש ביותר למעלה
                        }
                        historyAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadArtistData(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Artists").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Artist artist = snapshot.getValue(Artist.class);
                if (artist != null) {
                    tvStageName.setText(artist.getStageName());
                    tvArtistType.setText(artist.getArtistSubCategory() + " • @" + artist.getUsername());
                    tvBio.setText(artist.getBio().isEmpty() ? "אין תיאור זמין" : artist.getBio());

                    populateGenreChips(artist.getGenres());

                    tvInstrument.setText(artist.getInstrument());
                    tvFullName.setText(artist.getFullName());
                    tvBirthDate.setText(artist.getBirthDate());

                    instagramLink = artist.getSocialLink();
                    phoneNumber = artist.getPhone();
                    emailAddress = artist.getEmail();

                    if (artist.getProfileImageUrl() != null && !artist.getProfileImageUrl().isEmpty()) {
                        try {
                            Glide.with(requireContext()).load(artist.getProfileImageUrl()).into(imgProfile);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void populateGenreChips(String genres) {
        if (cgGenres == null) return;
        cgGenres.removeAllViews();
        if (genres == null || genres.isEmpty()) return;

        String[] genresArray = genres.split(", ");
        for (String g : genresArray) {
            Chip chip = new Chip(requireContext());
            chip.setText(g);
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.beat_primary);
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeWidth(0f);
            cgGenres.addView(chip);
        }
    }

    private void setupActionButtons() {
        btnInstagram.setOnClickListener(v -> {
            if (instagramLink != null && !instagramLink.isEmpty()) {
                if (!instagramLink.startsWith("http")) instagramLink = "https://" + instagramLink;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramLink));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "אין קישור לאינסטגרם", Toast.LENGTH_SHORT).show();
            }
        });

        btnPhone.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "אין מספר טלפון", Toast.LENGTH_SHORT).show();
            }
        });

        btnEmail.setOnClickListener(v -> {
            if (emailAddress != null && !emailAddress.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + emailAddress));
                startActivity(intent);
            }
        });
    }
}