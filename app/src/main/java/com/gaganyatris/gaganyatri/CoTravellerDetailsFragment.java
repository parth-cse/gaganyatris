package com.gaganyatris.gaganyatri;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.google.firebase.Timestamp;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class CoTravellerDetailsFragment extends Fragment {

    EditText name, email, pNo, dob, count, state, city;
    Calendar calendar;
    CountryCodePicker countryCode;
    AutoCompleteTextView gender;
    LinearLayout mobileNumberForm;





    public CoTravellerDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_co_traveller_details, container, false);

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
        dob.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSaveNext = view.findViewById(R.id.btn_save_next);
        TextView headerTextView = requireActivity().findViewById(R.id.textView2);

        // Check if editing: get coTraveller_id if provided
        String existingCoTravellerId;
        if (getArguments() != null && getArguments().containsKey("coTraveller_id")) {
            existingCoTravellerId = getArguments().getString("coTraveller_id");
        } else {
            existingCoTravellerId = null;
        }

        if (existingCoTravellerId != null) {
            headerTextView.setText("Edit Co Traveller"); // Change title for edit mode

            // Fetch co-traveller data from Firestore to prefill fields
            FirebaseFirestore.getInstance().collection("coTravellers")
                    .document(existingCoTravellerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            CoTraveller fetchedCoTraveller = documentSnapshot.toObject(CoTraveller.class);
                            if (fetchedCoTraveller != null) {
                                name.setText(fetchedCoTraveller.getName());
                                email.setText(fetchedCoTraveller.getEmail());
                                gender.setText(fetchedCoTraveller.getGender());
                                count.setText(fetchedCoTraveller.getCountry());
                                state.setText(fetchedCoTraveller.getState());
                                city.setText(fetchedCoTraveller.getCity());

                                if (fetchedCoTraveller.getPhoneNo() != null) {
                                    String fullPhoneNumber = fetchedCoTraveller.getPhoneNo();
                                    countryCode.setFullNumber(fullPhoneNumber);
                                    String localPhoneNumber = countryCode.getFormattedFullNumber()
                                            .replace("+" + countryCode.getSelectedCountryCode(), "");
                                    pNo.setText(localPhoneNumber);
                                }

                                if (fetchedCoTraveller.getDateOfBirth() != null) {
                                    Calendar dobCalendar = Calendar.getInstance();
                                    dobCalendar.setTime(fetchedCoTraveller.getDateOfBirth().toDate());
                                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                                            dobCalendar.get(Calendar.DAY_OF_MONTH),
                                            dobCalendar.get(Calendar.MONTH) + 1,
                                            dobCalendar.get(Calendar.YEAR));
                                    dob.setText(formattedDate);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load co-traveller details", Toast.LENGTH_SHORT).show();
                    });
        }

        // Allowed values for gender selection
        String[] genderOptions = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        gender.setAdapter(adapter);
        gender.setThreshold(1);
        gender.setOnClickListener(v -> gender.showDropDown());
        gender.setKeyListener(null);

        btnSaveNext.setOnClickListener(v -> {
            // Fetch the current user UID
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Use existingCoTravellerId if editing, else generate a new unique ID
            String coTravellerIdToUse = (existingCoTravellerId != null) ? existingCoTravellerId : UUID.randomUUID().toString();
            boolean isEditing = (existingCoTravellerId != null);

            // Collect data from form
            String iName = name.getText().toString().trim();
            String iEmail = email.getText().toString().trim();
            String iPNo = countryCode.getFullNumberWithPlus().trim();
            String iGender = gender.getText().toString().trim();
            String iCountry = count.getText().toString().trim();
            String iState = state.getText().toString().trim();
            String iCity = city.getText().toString().trim();

            // Validate required fields...
            if (iName.isEmpty()) {
                name.setError("Name is required");
                name.requestFocus();
                return;
            }
            if (iEmail.isEmpty()) {
                email.setError("Email is required");
                email.requestFocus();
                return;
            }
            if (iPNo.isEmpty()) {
                pNo.setError("Phone number is required");
                pNo.requestFocus();
                return;
            }
            if (iCountry.isEmpty()) {
                count.setError("Country is required");
                count.requestFocus();
                return;
            }
            if (iState.isEmpty()) {
                state.setError("State is required");
                state.requestFocus();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(iEmail).matches()) {
                email.setError("Enter a valid email");
                email.requestFocus();
                return;
            }
            if (iPNo.length() < 10 || iPNo.length() > 15) {
                pNo.setError("Enter a valid phone number");
                pNo.requestFocus();
                return;
            }

            Timestamp iDob = null;
            if (!dob.getText().toString().isEmpty()) {
                String[] dateParts = dob.getText().toString().split("/");
                if (dateParts.length == 3) {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1;
                    int year = Integer.parseInt(dateParts[2]);
                    Calendar dobCalendar = Calendar.getInstance();
                    dobCalendar.set(year, month, day);
                    iDob = new Timestamp(dobCalendar.getTime());
                }
            }

            // Create CoTraveller object using the determined ID
            CoTraveller coTraveller = new CoTraveller(
                    coTravellerIdToUse,  // Use existing id if editing, new id if adding
                    currentUserUid,
                    iName, iEmail, iPNo, iDob, iGender, iCountry, iState, iCity, 0
            );

            // Pass data to the next fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("coTraveller", coTraveller);
            bundle.putBoolean("isEditing", isEditing); // Pass the flag

            CoTravellerAvatarFragment coTravellerAvatarFragment = new CoTravellerAvatarFragment();
            coTravellerAvatarFragment.setArguments(bundle);

            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, coTravellerAvatarFragment, "coTravellerAvatarFragment");
            transaction.addToBackStack("coTravellerAvatarFragment");
            transaction.commit();
        });
    }




    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date as DD/MM/YYYY
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    dob.setText(formattedDate);
                }, year, month, day);


        datePickerDialog.show();
    }
}