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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InitialFormFragment extends Fragment {

    EditText tripStartDate, tripEndDate;
    Button saveNext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_initial_form, container, false);
        tripStartDate = view.findViewById(R.id.tripStartDate);
        tripEndDate = view.findViewById(R.id.tripEndDate);
        tripStartDate.setOnClickListener(v -> showDatePickerDialog(tripStartDate));
        tripEndDate.setOnClickListener(v -> showDatePickerDialog(tripEndDate));
        saveNext = view.findViewById(R.id.btn_save_next);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        saveNext.setOnClickListener(v -> {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            SelectCoTravellerFragment selectCoTravellerFragment = new SelectCoTravellerFragment();
            transaction.replace(R.id.fragmentContainer, selectCoTravellerFragment, "selectCoTravellerFragment");
            transaction.addToBackStack("selectCoTravellerFragment");
            transaction.commit();
        });
    }

    private void showDatePickerDialog(EditText a) {
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
                // Handle parsing error (e.g., show a toast)
                Toast.makeText(requireContext(), "Invalid Start Date", Toast.LENGTH_SHORT).show();
            }
        }


        datePickerDialog.show();
    }
}