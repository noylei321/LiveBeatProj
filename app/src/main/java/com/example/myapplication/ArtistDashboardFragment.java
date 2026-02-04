package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ArtistDashboardFragment extends Fragment {

    private RecyclerView rvShows;
    private ShowsAdapter showsAdapter;
    private ArrayList<Show> showsList;

    private ImageView imgArtistProfile;
    private TextView tvArtistName;
    private Button btnStartLive;

    private DatabaseReference showsRef;
    private FirebaseAuth mAuth;

    public ArtistDashboardFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        showsRef = FirebaseDatabase.getInstance().getReference("Shows");

        // --- התיקון כאן: ה-IDs הותאמו ל-XML שלך ---
        imgArtistProfile = view.findViewById(R.id.imgArtistProfile);
        tvArtistName = view.findViewById(R.id.tvArtistName);
        btnStartLive = view.findViewById(R.id.btnStartLive);

        loadArtistDataFromDB();

        rvShows = view.findViewById(R.id.rvPastShows);
        showsList = new ArrayList<>();
        showsAdapter = new ShowsAdapter(showsList);

        if (rvShows != null) {
            rvShows.setLayoutManager(new LinearLayoutManager(getContext()));
            rvShows.setAdapter(showsAdapter);
        }

        readShowsFromDB();

        FloatingActionButton fabAddShow = view.findViewById(R.id.fabAddShow);
        if (fabAddShow != null) {
            fabAddShow.setOnClickListener(v -> {
                AddShowDialogFragment dialog = new AddShowDialogFragment();
                dialog.show(getChildFragmentManager(), "AddShowDialog");
            });
        }

        btnStartLive.setOnClickListener(v -> {
            Show liveShow = findCurrentLiveShow();
            if (liveShow != null) {
                showsRef.child(liveShow.getShowId()).child("live").setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "הופעה חיה התחילה!", Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putString("showId", liveShow.getShowId());
                            androidx.navigation.Navigation.findNavController(requireView())
                                    .navigate(R.id.fragment_artist_live, bundle);
                        });
            }
        });
    }

    private void loadArtistDataFromDB() {

        if (mAuth.getCurrentUser() == null) {
            Log.e("DBG", "no user");
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        Log.d("DBG", "uid=" + uid);

        DatabaseReference artistRef =
                FirebaseDatabase.getInstance().getReference("Artists").child(uid);

        artistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("DBG", "exists=" + snapshot.exists());
                Log.d("DBG", "raw=" + snapshot.getValue());

                if (!snapshot.exists()) {
                    tvArtistName.setText("");
                    imgArtistProfile.setImageResource(android.R.drawable.ic_menu_gallery);
                    return;
                }

                Artist artist = snapshot.getValue(Artist.class);
                if (artist == null) return;

                String name = artist.getStageName();
                if (name == null || name.trim().isEmpty())
                    name = artist.getFullName();

                tvArtistName.setText(name != null ? name : "");

                String url = artist.getProfileImageUrl();
                if (url != null && !url.isEmpty()) {
                    Glide.with(ArtistDashboardFragment.this)
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(imgArtistProfile);
                } else {
                    imgArtistProfile.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DBG", "err=" + error.getMessage());
            }
        });
    }



    private void readShowsFromDB() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUid = mAuth.getCurrentUser().getUid();

        showsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showsList.clear();
                boolean canGoLive = false;
                long currentTimeMillis = System.currentTimeMillis();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Show show = snapshot.getValue(Show.class);
                    if (show != null && show.getArtistId().equals(currentUid)) {
                        showsList.add(show);
                        try {
                            long showStartTime = convertTimeToMillis(show.getDate(), show.getTime());
                            if (currentTimeMillis >= (showStartTime - 600000)) canGoLive = true;
                        } catch (Exception e) { Log.e("TimeError", "Error"); }
                    }
                }

                // עדכון ויזואלי של הרשימה לעומת הודעת "אין הופעות"
                View view = getView();
                if (view != null) {
                    TextView tvNoShows = view.findViewById(R.id.tvNoShows);
                    if (showsList.isEmpty()) {
                        rvShows.setVisibility(View.GONE);
                        if (tvNoShows != null) tvNoShows.setVisibility(View.VISIBLE);
                    } else {
                        rvShows.setVisibility(View.VISIBLE);
                        if (tvNoShows != null) tvNoShows.setVisibility(View.GONE);
                    }
                }

                showsAdapter.notifyDataSetChanged();
                updateLiveButton(canGoLive);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private Show findCurrentLiveShow() {
        long currentTime = System.currentTimeMillis();
        for (Show show : showsList) {
            try {
                long showStartTime = convertTimeToMillis(show.getDate(), show.getTime());
                if (currentTime >= (showStartTime - 600000)) return show;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    private long convertTimeToMillis(String date, String time) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date mDate = sdf.parse(date + " " + time);
        return mDate != null ? mDate.getTime() : 0;
    }

    private void updateLiveButton(boolean enabled) {
        if (btnStartLive != null) {
            btnStartLive.setEnabled(enabled);
            btnStartLive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(enabled ? "#800080" : "#888888")));
            btnStartLive.setText(enabled ? "התחל הופעה חיה" : "מצב לייב זמין בקרוב");
        }
    }
}