package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.models.Trip;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private LinearLayout noHistoryLayout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    LoadingDialog loadingDialog;
    private EditText etSearch;
    private List<Trip> allTrips = new ArrayList<>(); // Store all fetched trips

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        historyContainer = view.findViewById(R.id.historyContainer);
        noHistoryLayout = view.findViewById(R.id.no_history);
        etSearch = view.findViewById(R.id.etSearch);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fetchTripHistory();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void fetchTripHistory() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("trips")
                .whereEqualTo("userUid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        allTrips.clear(); // Clear existing list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Trip trip = document.toObject(Trip.class);
                            trip.setTripId(document.getId());
                            allTrips.add(trip);
                        }

                        if (allTrips.isEmpty()) {
                            historyContainer.setVisibility(View.GONE);
                            noHistoryLayout.setVisibility(View.VISIBLE);
                        } else {
                            historyContainer.setVisibility(View.VISIBLE);
                            noHistoryLayout.setVisibility(View.GONE);
                            displayTripHistory(allTrips); // Display all trips initially
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch trip history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTripHistory(List<Trip> trips) {
        historyContainer.removeAllViews(); // Clear existing views

        Collections.sort(trips, new Comparator<Trip>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            @Override
            public int compare(Trip trip1, Trip trip2) {
                try {
                    Date date1 = dateFormat.parse((String) trip1.getTripDetails().get("endDate"));
                    Date date2 = dateFormat.parse((String) trip2.getTripDetails().get("endDate"));
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        for (Trip trip : trips) {
            View tripCardView = LayoutInflater.from(getContext()).inflate(R.layout.history_card, historyContainer, false);

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

            historyContainer.addView(tripCardView);
        }
    }

    private void filterTrips(String searchText) {
        List<Trip> filteredTrips = new ArrayList<>();
        if (searchText.isEmpty()) {
            displayTripHistory(allTrips); // Display all trips if search is empty
            return;
        }

        searchText = searchText.toLowerCase();
        for (Trip trip : allTrips) {
            Map<String, Object> tripDetails = trip.getTripDetails();
            String startDate = (String) tripDetails.get("startDate");
            String endDate = (String) tripDetails.get("endDate");
            String fromCity = (String) tripDetails.get("to");

            if (fromCity.toLowerCase().contains(searchText) ||
                    startDate.toLowerCase().contains(searchText) ||
                    endDate.toLowerCase().contains(searchText)) {
                filteredTrips.add(trip);
            }
        }
        displayTripHistory(filteredTrips);
    }

    private int getCityImageResource(String city) {
        if (city == null) return R.drawable.default_image;
        String formattedCityName = city.toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(formattedCityName, "drawable", requireContext().getPackageName());
        return resId != 0 ? resId : R.drawable.default_image;
    }

    private String formatDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                // Handle parsing error (e.g., log the error, return a default string)
                System.err.println("Error parsing date: " + e.getMessage());
                return "Invalid Date"; // Or another appropriate error message
            }
        }
        return "Date Unavailable";
    }
}
