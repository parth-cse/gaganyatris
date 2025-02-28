package com.gaganyatris.gaganyatri;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InitialFormFragment extends Fragment {

    private EditText tripStartDate, tripEndDate, fromEditText, toEditText, budget;
    private Button saveNext;
    private AutoCompleteTextView tripType;
    private Switch explore;
    private String apiKey = BuildConfig.API_KEY;
    private boolean exploreNearBy;


    // Activity Result Launchers for Place Picker
    private final ActivityResultLauncher<Intent> fromLocationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    fromEditText.setText(place.getDisplayName());
                }
            });

    private final ActivityResultLauncher<Intent> toLocationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    toEditText.setText(place.getDisplayName());
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initial_form, container, false);

        tripStartDate = view.findViewById(R.id.tripStartDate);
        tripEndDate = view.findViewById(R.id.tripEndDate);
        saveNext = view.findViewById(R.id.btn_save_next);
        tripType = view.findViewById(R.id.type_of_trip);
        explore = view.findViewById(R.id.explore);
        fromEditText = view.findViewById(R.id.from);
        toEditText = view.findViewById(R.id.to);
        budget = view.findViewById(R.id.budget);

        fromEditText.setFocusable(false);
        fromEditText.setClickable(true);
        fromEditText.setInputType(0);

        toEditText.setFocusable(false);
        toEditText.setClickable(true);
        toEditText.setInputType(0);

        tripStartDate.setOnClickListener(v -> showDatePickerDialog(tripStartDate));
        tripEndDate.setOnClickListener(v -> showDatePickerDialog(tripEndDate));

        explore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            exploreNearBy = isChecked;
            if (isChecked) {
                explore.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.holo_green_dark)));
                explore.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.holo_green_light)));
            } else {
                explore.setThumbTintList(null);
                explore.setTrackTintList(null);
            }
        });

        // Initialize Places API if not initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey);
        }

        // Set click listeners for location pickers
        fromEditText.setOnClickListener(v -> openPlacePicker(fromLocationPickerLauncher));
        toEditText.setOnClickListener(v -> openPlacePicker(toLocationPickerLauncher));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        saveNext.setOnClickListener(v -> {
            if (validateFields()) {
                // Collect user input
                String startDate = tripStartDate.getText().toString();
                String endDate = tripEndDate.getText().toString();
                String from = fromEditText.getText().toString();
                String to = toEditText.getText().toString();
                String budgetAmount = budget.getText().toString();
                String tripTypeValue = tripType.getText().toString();

                // Bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString("tripStartDate", startDate);
                bundle.putString("tripEndDate", endDate);
                bundle.putString("from", from);
                bundle.putString("to", to);
                bundle.putString("budget", budgetAmount);
                bundle.putString("tripType", tripTypeValue);
                bundle.putBoolean("exploreNearby", exploreNearBy);

                // Navigate to the next fragment
                SelectCoTravellerFragment selectCoTravellerFragment = new SelectCoTravellerFragment();
                selectCoTravellerFragment.setArguments(bundle);

                FragmentManager fm = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragmentContainer, selectCoTravellerFragment, "selectCoTravellerFragment");
                transaction.addToBackStack("selectCoTravellerFragment");
                transaction.commit();
            }
        });


        String[] tripTypeOptions = {"Religious", "Historic", "Educational", "Friends Trip", "Family Trip", "Solo Explorer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, tripTypeOptions);
        tripType.setAdapter(adapter);
        tripType.setThreshold(1);
        tripType.setOnClickListener(v -> tripType.showDropDown());
        tripType.setKeyListener(null);
    }

    private void showDatePickerDialog(EditText a) {
        Calendar currentCalendar = Calendar.getInstance();
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    a.setText(formattedDate);
                }, year, month, day);

        if (a == tripEndDate && tripStartDate.getText().length() > 0) {
            try {
                String startDateStr = tripStartDate.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate = sdf.parse(startDateStr);
                Calendar minDateCalendar = Calendar.getInstance();
                minDateCalendar.setTime(startDate);
                datePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Invalid Start Date", Toast.LENGTH_SHORT).show();
            }
        }

        datePickerDialog.show();
    }

    private void openPlacePicker(ActivityResultLauncher<Intent> launcher) {
        List<Place.Field> placeFields = List.of(Place.Field.DISPLAY_NAME);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields).setCountries(Collections.singletonList("IN"))
                .build(requireContext());

        launcher.launch(intent);
    }

    private boolean validateFields() {
        if (tripStartDate.getText().toString().trim().isEmpty()) {
            tripStartDate.setError("Please select a start date");
            tripStartDate.requestFocus();
            return false;
        }
        if (tripEndDate.getText().toString().trim().isEmpty()) {
            tripEndDate.setError("Please select an end date");
            tripEndDate.requestFocus();
            return false;
        }
        if (fromEditText.getText().toString().trim().isEmpty()) {
            fromEditText.setError("Please select a starting location");
            fromEditText.requestFocus();
            return false;
        }
        if (toEditText.getText().toString().trim().isEmpty()) {
            toEditText.setError("Please select a destination");
            toEditText.requestFocus();
            return false;
        }
        if (budget.getText().toString().trim().isEmpty()) {
            budget.setError("Please enter your budget");
            budget.requestFocus();
            return false;
        }
        if (tripType.getText().toString().trim().isEmpty()) {
            tripType.setError("Please select a trip type");
            tripType.requestFocus();
            return false;
        }
        return true;
    }

}
