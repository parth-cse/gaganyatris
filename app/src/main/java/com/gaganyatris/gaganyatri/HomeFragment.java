package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.models.Trip;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView heyTextView;
    private ListenerRegistration firestoreListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    LoadingDialog loadingDialog;

    private List<Trip> allTrips = new ArrayList<>();
    private TextView textView;
    private LinearLayout upcomingTrip;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTripHistory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout findTickets = view.findViewById(R.id.find_tickets);
        LinearLayout travelGroups = view.findViewById(R.id.travel_groups);
        LinearLayout getGuide = view.findViewById(R.id.get_guide);
        LinearLayout planYourTrip = view.findViewById(R.id.planYourTrip);
        heyTextView = view.findViewById(R.id.hey);
        view.findViewById(R.id.cameraLens).setOnClickListener(v -> startActivity(new Intent(requireContext(), ImageSearchActivity.class)));
        view.findViewById(R.id.gagoo).setOnClickListener(v -> startActivity(new Intent(requireContext(), AiChatBotActivity.class)));
        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        textView = view.findViewById(R.id.textView3);
        upcomingTrip = view.findViewById(R.id.upcomingTrip);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }

        findTickets.setOnClickListener(v -> startActivity(new Intent(requireContext(), ModeOfTransportActivity.class)));
        travelGroups.setOnClickListener(v -> startActivity(new Intent(requireContext(), TravelGroupActivity.class)));
        getGuide.setOnClickListener(v -> startActivity(new Intent(requireContext(), GetGuideActivity.class)));
        planYourTrip.setOnClickListener(v -> startActivity(new Intent(requireContext(), PlanYourTripActivity.class)));
        view.findViewById(R.id.exploreLocation).setOnClickListener(v -> startActivity(new Intent(requireContext(), ExploreYourLocationActivity.class)));
    }

    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        firestoreListener = userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        String firstName = name.split("\\s+")[0];
                        heyTextView.setText("Hey 👋 " + firstName);
                    } else {
                        heyTextView.setText("Hey 👋 Traveler");
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void fetchTripHistory() {
        String userId = mAuth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp todayTimestamp = new Timestamp(calendar.getTime());

        db.collection("trips")
                .whereGreaterThanOrEqualTo("tripStartDate", todayTimestamp)
                .whereEqualTo("userUid", userId)
                .orderBy("tripStartDate", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            allTrips.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Trip trip = document.toObject(Trip.class);
                                trip.setTripId(document.getId());
                                allTrips.add(trip);
                            }

                            if (allTrips.isEmpty()) {
                                textView.setVisibility(View.GONE);
                                upcomingTrip.setVisibility(View.GONE);
                            } else {
                                textView.setVisibility(View.VISIBLE);
                                upcomingTrip.setVisibility(View.VISIBLE);
                                displayTripHistory(allTrips);
                            }
                        } else {
                            if (allTrips.isEmpty()) {
                                textView.setVisibility(View.VISIBLE);
                                View cardView = LayoutInflater.from(getContext()).inflate(R.layout.cardview, upcomingTrip, false);
                                cardView.setOnClickListener(v -> startActivity(new Intent(requireContext(), PlanYourTripActivity.class)));
                                upcomingTrip.addView(cardView);
                            }
                            Log.e("HomeFragment", "Error fetching trip history: ", task.getException());
                            Toast.makeText(requireContext(), "Failed to load trip history.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

    private void displayTripHistory(List<Trip> trips) {
        upcomingTrip.removeAllViews();

        Collections.sort(trips, (trip1, trip2) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date1 = dateFormat.parse((String) trip1.getTripDetails().get("endDate"));
                Date date2 = dateFormat.parse((String) trip2.getTripDetails().get("endDate"));
                if (date1 == null || date2 == null) return 0; //Handle null dates.
                return date2.compareTo(date1);
            } catch (ParseException e) {
                Log.e("HomeFragment", "Error parsing date: " + e.getMessage());
                return 0; // Return 0 to avoid breaking the sort.
            }
        });

        for (Trip trip : trips) {
            View tripCardView = LayoutInflater.from(getContext()).inflate(R.layout.history_card, upcomingTrip, false);

            TextView title = tripCardView.findViewById(R.id.title);
            TextView date = tripCardView.findViewById(R.id.date);
            TextView soloOrGroup = tripCardView.findViewById(R.id.soloOrGroup);
            ImageView imageView = tripCardView.findViewById(R.id.imageView14);

            Map<String, Object> tripDetails = trip.getTripDetails();
            String startDate = (String) tripDetails.get("startDate");
            String endDate = (String) tripDetails.get("endDate");
            String fromCity = (String) tripDetails.get("to");

            title.setText(formatDate(startDate) + " to " + formatDate(endDate));
            date.setText("Trip to " + fromCity);
            imageView.setImageResource(getCityImageResource((String) tripDetails.get("to")));

            tripCardView.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), SavedTripActivity.class);
                i.putExtra("tripDocumentId", trip.getTripId());
                startActivity(i);
            });

            if (trip.getCoTravellers() != null && trip.getCoTravellers().size() > 1) {
                soloOrGroup.setText("Group Trip");
            } else {
                soloOrGroup.setText("Solo");
            }

            upcomingTrip.addView(tripCardView);
        }
    }

    private String formatDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.e("HomeFragment", "Error parsing date: " + e.getMessage());
                return "Invalid Date";
            }
        }
        return "Date Unavailable";
    }

    private int getCityImageResource(String city) {
        if (city == null) return R.drawable.default_image;
        String formattedCityName = city.toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(formattedCityName, "drawable", requireContext().getPackageName());
        return resId != 0 ? resId : R.drawable.default_image;
    }
}