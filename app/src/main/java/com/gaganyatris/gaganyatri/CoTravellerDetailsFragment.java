package com.gaganyatris.gaganyatri;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class CoTravellerDetailsFragment extends Fragment {

    private EditText name, email, pNo, dob, count, state, city;
    private Calendar calendar;
    private CountryCodePicker countryCode;
    private AutoCompleteTextView gender;
    private LinearLayout mobileNumberForm;
    private TextView headerTextView;
    private Button btnSaveNext;

    public CoTravellerDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_co_traveller_details, container, false);
        initializeViews(view);
        setupDatePicker();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupGenderDropdown();
        setupSaveNextButton();
        populateFieldsIfEditing();
    }

    private void initializeViews(View view) {
        name = view.findViewById(R.id.first_name);
        email = view.findViewById(R.id.email);
        pNo = view.findViewById(R.id.phone_number);
        dob = view.findViewById(R.id.et_dob);
        countryCode = view.findViewById(R.id.countryCode);
        countryCode.registerCarrierNumberEditText(pNo);
        gender = view.findViewById(R.id.autoCompleteTextView);
        count = view.findViewById(R.id.country);
        state = view.findViewById(R.id.state);
        city = view.findViewById(R.id.city);
        calendar = Calendar.getInstance();
        mobileNumberForm = view.findViewById(R.id.mobile_number_form);
        headerTextView = requireActivity().findViewById(R.id.textView2);
        btnSaveNext = view.findViewById(R.id.btn_save_next);
    }

    private void setupDatePicker() {
        dob.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupGenderDropdown() {
        String[] genderOptions = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions);
        gender.setAdapter(adapter);
        gender.setThreshold(1);
        gender.setOnClickListener(v -> gender.showDropDown());
        gender.setKeyListener(null);
    }

    private void setupSaveNextButton() {
        btnSaveNext.setOnClickListener(v -> saveAndNavigate());
    }

    private void populateFieldsIfEditing() {
        String existingCoTravellerId = getArguments() != null && getArguments().containsKey("coTraveller_id")
                ? getArguments().getString("coTraveller_id") : null;

        if (existingCoTravellerId != null) {
            headerTextView.setText("Edit Co Traveller");
            fetchCoTravellerData(existingCoTravellerId);
        }
    }

    private void fetchCoTravellerData(String coTravellerId) {
        FirebaseFirestore.getInstance().collection("coTravellers").document(coTravellerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CoTraveller fetchedCoTraveller = documentSnapshot.toObject(CoTraveller.class);
                        if (fetchedCoTraveller != null) {
                            populateFields(fetchedCoTraveller);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load co-traveller details", Toast.LENGTH_SHORT).show());
    }

    private void populateFields(CoTraveller coTraveller) {
        name.setText(coTraveller.getName());
        email.setText(coTraveller.getEmail());
        gender.setText(coTraveller.getGender());
        count.setText(coTraveller.getCountry());
        state.setText(coTraveller.getState());
        city.setText(coTraveller.getCity());

        if (coTraveller.getPhoneNo() != null) {
            countryCode.setFullNumber(coTraveller.getPhoneNo());
            String localPhoneNumber = countryCode.getFormattedFullNumber().replace("+" + countryCode.getSelectedCountryCode(), "");
            pNo.setText(localPhoneNumber);
        }

        if (coTraveller.getDateOfBirth() != null) {
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(coTraveller.getDateOfBirth().toDate());
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    dobCalendar.get(Calendar.DAY_OF_MONTH), dobCalendar.get(Calendar.MONTH) + 1, dobCalendar.get(Calendar.YEAR));
            dob.setText(formattedDate);
        }
    }

    private void saveAndNavigate() {
        if (validateFields()) {
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String existingCoTravellerId = getArguments() != null && getArguments().containsKey("coTraveller_id")
                    ? getArguments().getString("coTraveller_id") : null;
            String coTravellerIdToUse = existingCoTravellerId != null ? existingCoTravellerId : UUID.randomUUID().toString();
            boolean isEditing = existingCoTravellerId != null;

            CoTraveller coTraveller = createCoTravellerObject(coTravellerIdToUse, currentUserUid);
            navigateToAvatarFragment(coTraveller, isEditing);
        }
    }

    private boolean validateFields() {
        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Name is required");
            name.requestFocus();
            return false;
        }
        if (email.getText().toString().trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            email.setError("Valid email is required");
            email.requestFocus();
            return false;
        }
        if (pNo.getText().toString().trim().isEmpty() || pNo.getText().toString().trim().length() < 10 || pNo.getText().toString().trim().length() > 15) {
            pNo.setError("Valid phone number is required");
            pNo.requestFocus();
            return false;
        }
        if (count.getText().toString().trim().isEmpty()) {
            count.setError("Country is required");
            count.requestFocus();
            return false;
        }
        if (state.getText().toString().trim().isEmpty()) {
            state.setError("State is required");
            state.requestFocus();
            return false;
        }
        return true;
    }

    private CoTraveller createCoTravellerObject(String coTravellerId, String currentUserUid) {
        Timestamp dobTimestamp = null;
        if (!dob.getText().toString().isEmpty()) {
            String[] dateParts = dob.getText().toString().split("/");
            if (dateParts.length == 3) {
                Calendar dobCalendar = Calendar.getInstance();
                dobCalendar.set(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[0]));
                dobTimestamp = new Timestamp(dobCalendar.getTime());
            }
        }
        return new CoTraveller(coTravellerId, currentUserUid, name.getText().toString().trim(),
                email.getText().toString().trim(), countryCode.getFullNumberWithPlus().trim(), dobTimestamp,
                gender.getText().toString().trim(), count.getText().toString().trim(),
                state.getText().toString().trim(), city.getText().toString().trim(), 0);
    }

    private void navigateToAvatarFragment(CoTraveller coTraveller, boolean isEditing) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("coTraveller", coTraveller);
        bundle.putBoolean("isEditing", isEditing);

        CoTravellerAvatarFragment coTravellerAvatarFragment = new CoTravellerAvatarFragment();
        coTravellerAvatarFragment.setArguments(bundle);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, coTravellerAvatarFragment, "coTravellerAvatarFragment");
        transaction.addToBackStack("coTravellerAvatarFragment");
        transaction.commit();
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    dob.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
}