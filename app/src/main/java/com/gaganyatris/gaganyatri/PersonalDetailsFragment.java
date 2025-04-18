package com.gaganyatris.gaganyatri;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PersonalDetailsFragment extends Fragment {

    EditText name, email, pNo, dob, count, state, city;
    String iName, iEmail, iPNo, iDob, iGender, iCountry, iState, iCity;
    Calendar calendar;
    CountryCodePicker countryCode;
    AutoCompleteTextView gender;
    LinearLayout mobileNumberForm;

    public PersonalDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_details, container, false);

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
        dob.setOnClickListener(v -> showDatePickerDialog());
        mobileNumberForm = view.findViewById(R.id.mobile_number_form);

        // Check if user is logged in with phone authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            String phoneNumber = user.getPhoneNumber();
            String localPhoneNumber = countryCode.getFullNumber();
            pNo.setText(localPhoneNumber);
            pNo.setEnabled(false);
            countryCode.setFullNumber(phoneNumber);
            countryCode.setEnabled(false);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSaveNext = view.findViewById(R.id.btn_save_next);

        // Allowed values for gender selection
        String[] genderOptions = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        gender.setAdapter(adapter);
        gender.setThreshold(1);
        gender.setOnClickListener(v -> gender.showDropDown());
        gender.setKeyListener(null);

        // Show ProgressDialog while fetching data
        LoadingDialog loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();

        // Fetch user details from Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        loadingDialog.dismiss(); // Dismiss progress dialog

                        if (documentSnapshot.exists()) {
                            // Retrieve data from Firestore
                            iName = documentSnapshot.getString("name");
                            iEmail = documentSnapshot.getString("email");
                            iPNo = documentSnapshot.getString("phone"); // Retrieve phone from Firestore

                            Object dobObject = documentSnapshot.get("dob");
                            if (dobObject instanceof Timestamp) {
                                Date dobDate = ((Timestamp) dobObject).toDate();
                                iDob = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dobDate);
                            } else if (dobObject instanceof String) {
                                iDob = (String) dobObject;
                            } else {
                                iDob = "";
                            }

                            iGender = documentSnapshot.getString("gender");
                            iCountry = documentSnapshot.getString("country");
                            iState = documentSnapshot.getString("state");
                            iCity = documentSnapshot.getString("city");

                            // Populate EditText fields
                            name.setText(iName);
                            email.setText(iEmail);
                            dob.setText(iDob);
                            gender.setText(iGender);
                            count.setText(iCountry);
                            state.setText(iState);
                            city.setText(iCity);

                            // Check if user is signed in with Google
                            boolean isGoogleSignIn = (user.getEmail() != null && !user.getEmail().isEmpty());

                            if (isGoogleSignIn) {
                                // If authenticated via Google, retrieve phone number from Firestore
                                if (iPNo != null && !iPNo.isEmpty()) {
                                    pNo.setText(iPNo);
                                    countryCode.setFullNumber(iPNo);
                                }
                                // Make phone number editable only when signed in via Google
                                pNo.setEnabled(true);
                                countryCode.setEnabled(true);
                            } else {
                                // For other sign-in methods, disable phone number
                                pNo.setEnabled(false);
                                countryCode.setEnabled(false);
                            }

                            if (isGoogleSignIn) {
                                email.setEnabled(false); // Disable email editing for Google sign-in
                            }
                        } else {
                            // User does not exist, check authentication method
                            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                // Google Sign-In, pre-fill email
                                email.setText(user.getEmail());
                                email.setEnabled(false); // Disable email editing

                                // Fetch and display phone number from Firestore
                                if (iPNo != null && !iPNo.isEmpty()) {
                                    pNo.setText(iPNo);
                                    countryCode.setFullNumber(iPNo);
                                }
                                pNo.setEnabled(true);
                                countryCode.setEnabled(true);
                            } else {
                                // User not found and not Google Sign-In, log out
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(requireContext(), OnBoardingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                                Toast.makeText(requireContext(), "User data not found, please sign up again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismiss(); // Dismiss progress dialog
                        Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            loadingDialog.dismiss(); // Dismiss if no user is logged in
        }

        // Handle button click to navigate to the next fragment
        btnSaveNext.setOnClickListener(v -> {
            if (validateFields()) {
                Bundle bundle = new Bundle();
                bundle.putString("name", iName);
                bundle.putString("email", iEmail);
                bundle.putString("phone", iPNo);
                bundle.putString("dob", iDob);
                bundle.putString("gender", iGender);
                bundle.putString("country", iCountry);
                bundle.putString("state", iState);
                bundle.putString("city", iCity);

                FragmentManager fm = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                SelectAvatarFragment selectAvatarFragment = new SelectAvatarFragment();
                selectAvatarFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, selectAvatarFragment, "selectAvatarFragment");
                transaction.addToBackStack("selectAvatarFragment");
                transaction.commit();
            }
        });
    }

    private boolean validateFields() {
        iName = name.getText().toString().trim();
        iEmail = email.getText().toString().trim();
        iPNo = countryCode.getFullNumberWithPlus();
        iDob = dob.getText().toString().trim();
        iGender = gender.getText().toString().trim();
        iCountry = count.getText().toString().trim();
        iState = state.getText().toString().trim();
        iCity = city.getText().toString().trim();

        if (TextUtils.isEmpty(iName)) {
            name.setError("This field is required");
            return false;
        }
        if (TextUtils.isEmpty(iEmail)) {
            email.setError("This field is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(iEmail).matches()) {
            email.setError("Invalid email address");
            return false;
        }
        if (TextUtils.isEmpty(iPNo) || !countryCode.isValidFullNumber()) {
            pNo.setError("Invalid phone number");
            return false;
        }
        if (TextUtils.isEmpty(iDob)) {
            dob.setError("This field is required");
            return false;
        }
        if (TextUtils.isEmpty(iGender)) {
            gender.setError("This field is required");
            return false;
        }
        if (TextUtils.isEmpty(iCountry)) {
            count.setError("This field is required");
            return false;
        }
        if (TextUtils.isEmpty(iState)) {
            state.setError("This field is required");
            return false;
        }
        if (TextUtils.isEmpty(iCity)) {
            city.setError("This field is required");
            return false;
        }
        return true;
    }

    private void showDatePickerDialog() {
        // Use a fresh instance of Calendar to get the current date
        Calendar currentCalendar = Calendar.getInstance();
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date as DD/MM/YYYY
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    dob.setText(formattedDate);
                }, year, month, day);

        // Set max date (current date - 16 years)
        Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.add(Calendar.YEAR, -16);
        datePickerDialog.getDatePicker().setMaxDate(maxDateCalendar.getTimeInMillis());

        datePickerDialog.show();
    }
}