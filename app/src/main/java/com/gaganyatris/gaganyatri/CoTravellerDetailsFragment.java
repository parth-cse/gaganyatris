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

            // Generate a unique ID for coTraveller_id
            String uniqueCoTravellerId = UUID.randomUUID().toString();

            // Collect data from form
            String iName = name.getText().toString().trim();
            String iEmail = email.getText().toString().trim();
            String iPNo = countryCode.getFullNumberWithPlus().trim();
            String iGender = gender.getText().toString().trim();
            String iCountry = count.getText().toString().trim();
            String iState = state.getText().toString().trim();
            String iCity = city.getText().toString().trim();

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

            // Create CoTraveller object
            CoTraveller coTraveller = new CoTraveller(
                    uniqueCoTravellerId,  // Generated unique ID
                    currentUserUid,       // Current authenticated user's UID
                    iName, iEmail, iPNo, iDob, iGender, iCountry, iState, iCity, 0
            );

            // Pass data to the next fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("coTraveller", coTraveller);

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