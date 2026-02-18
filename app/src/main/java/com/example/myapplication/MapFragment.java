package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference showsRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private Location currentUserLocation;
    private boolean isFirstLocationUpdate = true;

    private String selectedArtistTypeFilter = "הכל";
    private boolean onlyLiveFilter = false;

    // ניהול אנימציה: Handler ו-Runnable מאפשרים הרצת קוד חוזרת ב-UI Thread ליצירת אפקט ה"דופק" של המרקרים.
    private Handler pulseHandler = new Handler(Looper.getMainLooper());
    private Runnable pulseRunnable;
    private float pulseAlpha = 1.0f;
    private boolean pulseDirectionUp = false;
    private List<Marker> liveMarkers = new ArrayList<>();

    // פונקציית Lifecycle המנפחת את ה-XML של המפה.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    // אתחול רכיבים מיד לאחר יצירת ה-View: הרשאות, שירותי מיקום, מאזינים לפילטרים וטעינת המפה האסינכרונית.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // שימוש ב-Activity Result API לניהול בקשות הרשאה (Permissions) בצורה מודרנית ובטוחה.
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted || coarseLocationGranted) {
                startLocationUpdate();
            }
        });

        setUpLocationCallback();

        // טעינת המפה ברקע. הפונקציה onMapReady תופעל כשהמפה תהיה מוכנה.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        showsRef = FirebaseDatabase.getInstance().getReference("Shows");

        // ניהול פילטרים דרך ChipGroup: משנה את משתני הסינון ומרענן את המרקרים על המפה בכל בחירה.
        ChipGroup chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            selectedArtistTypeFilter = "הכל";
            onlyLiveFilter = false;

            if (checkedId == R.id.chipLiveNow) onlyLiveFilter = true;
            else if (checkedId == R.id.chipStandupOnly) selectedArtistTypeFilter = "Comedian";
            else if (checkedId == R.id.chipSingersOnly) selectedArtistTypeFilter = "Musician";
            else if (checkedId == R.id.chipDjOnly) selectedArtistTypeFilter = "DJ";

            loadShowsFromFirebase();
        });

        view.findViewById(R.id.btnRefreshMap).setOnClickListener(v -> startLocationUpdate());
        view.findViewById(R.id.UserLogout).setOnClickListener(v -> showLogoutDialog());
        view.findViewById(R.id.chipEditProfile).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mapFragment_to_editUserProfileFragment);
        });

        checkPermissionsAndStartLocation();
        startPulseAnimation(); // התחלת לוגיקת האנימציה.
    }

    // בדיקה האם הרשאות המיקום כבר ניתנו. אם לא - הפעלת ה-Launcher לבקשתן.
    private void checkPermissionsAndStartLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdate();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    // מופעלת כשה-Google Map מוכן לשימוש. מגדירה את עיצוב חלון המידע (Custom Info Window) ומאזיני הקלקה על מרקרים.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        loadShowsFromFirebase();

        // מאזין ללחיצה על ה-InfoWindow: מעביר את המשתמש למסך הבקשות של המופע הספציפי.
        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.getTag() instanceof Show) {
                Show show = (Show) marker.getTag();
                Bundle bundle = new Bundle();
                bundle.putString("showId", show.getShowId());
                bundle.putString("location", show.getLocation());
                bundle.putString("artistId", show.getArtistId());
                Navigation.findNavController(requireView()).navigate(R.id.action_mapFragment_to_showRequestsUser, bundle);
            }
        });
    }

    // לב המערכת: שליפת הופעות מ-Firebase, סינונן לפי בחירת המשתמש, וחישוב סטטוס המופע (LIVE/SOON) בזמן אמת.
    private void loadShowsFromFirebase() {
        showsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mMap == null) return;
                mMap.clear();
                liveMarkers.clear();

                long currentTime = System.currentTimeMillis();
                String todayDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Show show = ds.getValue(Show.class);
                    if (show != null && show.getLatitude() != 0) {

                        // יישום הפילטרים: דילוג על הופעות שלא עומדות בקריטריונים של המשתמש.
                        if (onlyLiveFilter && !show.isLive()) continue;
                        if (!selectedArtistTypeFilter.equals("הכל") && (show.getArtistType() == null || !show.getArtistType().equals(selectedArtistTypeFilter))) continue;

                        try {
                            if (show.getDate().equals(todayDate)) {
                                long startTime = 0;
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
                                    Date mDate = sdf.parse(show.getDate() + " " + show.getTime());
                                    if (mDate != null) startTime = mDate.getTime();
                                } catch (Exception e) { e.printStackTrace(); }

                                // סיווג המרקר לפי הזמן: ירוק ל-LIVE, כחול ל"בקרוב", ואפור להמשך הערב.
                                if (show.isLive()) addMarkerForShow(show, "LIVE");
                                else if (startTime > currentTime) {
                                    if (startTime - currentTime <= 3600000) addMarkerForShow(show, "SOON");
                                    else addMarkerForShow(show, "LATER_TODAY");
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // יצירת מרקר פיזי על המפה. משתמשת ב-BitmapDescriptorFactory לעיצוב צבע המרקר וב-Tag לשמירת אובייקט ה-Show המלא בתוך המרקר.
    private void addMarkerForShow(Show show, String status) {
        LatLng position = new LatLng(show.getLatitude(), show.getLongitude());
        float color;
        float alpha = 1.0f;
        String snippet;

        switch (status) {
            case "LIVE": color = BitmapDescriptorFactory.HUE_GREEN; snippet = "LIVE! לחץ להצטרפות"; break;
            case "SOON": color = BitmapDescriptorFactory.HUE_AZURE; snippet = "מתחילים בקרוב: " + show.getTime(); break;
            default: color = 210f; alpha = 0.6f; snippet = "הופעה הערב ב-" + show.getTime(); break;
        }

        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title(show.getLocation())
                .alpha(alpha)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .snippet(snippet);

        Marker marker = mMap.addMarker(options);
        if (marker != null) {
            marker.setTag(show); // שמירת כל נתוני המופע בתוך המרקר לשימוש בעת הקלקה.
            if ("LIVE".equals(status)) liveMarkers.add(marker);
        }
    }

    // מנגנון אנימציה שרץ בלולאה אינסופית (Recursive Runnable) ומשנה את השקיפות (Alpha) של מרקרים ב-LIVE כדי ליצור אפקט פועם.
    private void startPulseAnimation() {
        if (pulseRunnable != null) return;
        pulseRunnable = new Runnable() {
            @Override
            public void run() {
                // שינוי הדרגתי של ה-Alpha למעלה ולמטה.
                if (pulseDirectionUp) {
                    pulseAlpha += 0.05f;
                    if (pulseAlpha >= 1.0f) pulseDirectionUp = false;
                } else {
                    pulseAlpha -= 0.05f;
                    if (pulseAlpha <= 0.4f) pulseDirectionUp = true;
                }

                for (Marker m : liveMarkers) {
                    try { if (m != null) m.setAlpha(pulseAlpha); } catch (Exception e) { }
                }
                pulseHandler.postDelayed(this, 60); // הרצה חוזרת כל 60 מילישניות לחלקות האנימציה.
            }
        };
        pulseHandler.post(pulseRunnable);
    }

    // הגדרת ה-Callback שמופעל בכל פעם שמערכת ה-GPS של המכשיר מעדכנת את המיקום הנוכחי של המשתמש.
    private void setUpLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currentUserLocation = location;
                    // התמקדות במפה (Zoom) רק בפעם הראשונה שהמיקום מתקבל כדי לא להפריע למשתמש בגלישה במפה.
                    if (isFirstLocationUpdate && mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                        isFirstLocationUpdate = false;
                    }
                }
            }
        };
    }

    // בקשת עדכוני מיקום בתדירות גבוהה (High Accuracy) מה-FusedLocationProviderClient.
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).setMinUpdateIntervalMillis(2000).build();
        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext()).setTitle("התנתקות").setMessage("אתה בטוח שברצונך להתנתק?").setPositiveButton("כן", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
        }).setNegativeButton("לא", (dialog, which) -> dialog.dismiss()).show();
    }

    // ניקוי משאבים ב-Lifecycle של סגירת ה-View למניעת Memory Leaks של אנימציות ועדכוני GPS.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pulseHandler != null && pulseRunnable != null) pulseHandler.removeCallbacks(pulseRunnable);
        if (fusedLocationProviderClient != null && locationCallback != null) fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    // אדפטור מותאם אישית שיוצר את ה"בועה" הקופצת מעל המרקר. מאפשר עיצוב חופשי של חלון המידע בעזרת XML חיצוני.
    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        public CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        // שאיבת הנתונים מאובייקט ה-Show ששמור ב-Tag של המרקר והזרקתם ל-Layout של חלון המידע.
        private void renderWindowText(Marker marker, View view) {
            Show show = (Show) marker.getTag();
            if (show != null) {
                TextView tvStageName = view.findViewById(R.id.info_stage_name);
                TextView tvGenre = view.findViewById(R.id.info_genre);
                TextView tvLocation = view.findViewById(R.id.info_location);
                TextView tvStatus = view.findViewById(R.id.info_status);

                tvStageName.setText(show.getStageName());
                tvGenre.setText(show.getGenre());
                tvLocation.setText("📍 " + show.getLocation());
                tvStatus.setText(marker.getSnippet());

                if (marker.getSnippet() != null && marker.getSnippet().contains("LIVE")) tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                else tvStatus.setTextColor(Color.DKGRAY);
            }
        }

        @Nullable @Override public View getInfoWindow(@NonNull Marker marker) { renderWindowText(marker, mWindow); return mWindow; }
        @Nullable @Override public View getInfoContents(@NonNull Marker marker) { return null; }
    }
}