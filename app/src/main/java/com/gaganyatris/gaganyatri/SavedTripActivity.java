package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gaganyatris.gaganyatri.models.CabDetails;
import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.models.TrainDetails;
import com.gaganyatris.gaganyatri.models.Trip;
import com.gaganyatris.gaganyatri.models.Users;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SavedTripActivity extends AppCompatActivity {

    private final int statusBarColor = R.color.primaryColor;

    String from, to, tripStartDate, tripEndDate;
    long noTripDays;
    String tripId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    LoadingDialog loadingDialog;
    private TableLayout coTravellersTable;
    ArrayList<String> coTravellers;
    String cabNumber;
    CabDetails cabT, cabF;
    TrainDetails trainT, trainF;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_trip);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        tripId = getIntent().getStringExtra("tripDocumentId");
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        mAuth = FirebaseAuth.getInstance();
        fetchData();

        findViewById(R.id.tripPlan).setOnClickListener(v ->{
            Intent i = new Intent(SavedTripActivity.this, TripPlanActivity.class);
            i.putExtra("tripID", tripId);
            startActivity(i);
        });

    }

    void fetchData(){
        db.collection("trips").document(tripId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Trip trip = document.toObject(Trip.class);
                    if (trip != null){
                        tripStartDate = (String) trip.getTripDetails().get("startDate");
                        tripEndDate = (String) trip.getTripDetails().get("endDate");
                        from = (String) trip.getTripDetails().get("from");
                        to = (String) trip.getTripDetails().get("to");
                        coTravellers = trip.getCoTravellers();
                        noTripDays = (long) trip.getTripDetails().get("totalTripDays");
                        if (trip.getTripDetails().get("travelMode").equals("Cab")) {
                            cabT = trip.getCabDetailsTo();
                            cabF = trip.getCabDetailsFro();
                        }

                        if (trip.getTripDetails().get("travelMode").equals("Train")) {
                            trainT = trip.getTrainDetailsTo();
                            trainF = trip.getTrainDetailsFro();
                        }
                    }
                }
            }
            updateUI();
        });

    }

    private void updateUI() {
        TextView tripStartDateView = findViewById(R.id.tripStartDate);
        TextView tripEndDateView = findViewById(R.id.tripEndDate);
        coTravellersTable = findViewById(R.id.coTravellersTable);
        TextView noOfDaysView = findViewById(R.id.noOfDays);
        ConstraintLayout cabToLayout = findViewById(R.id.ticketTo2);
        ConstraintLayout cabFromLayout = findViewById(R.id.ticketFro2);

        tripStartDateView.setText(formatDate(tripStartDate));
        tripEndDateView.setText(formatDate(tripEndDate));
        fetchCurrentUserAndCoTravellers(coTravellers);
        noOfDaysView.setText(noTripDays + " Day(s)");

        if (cabT != null) {
            cabDetailsUiUpdate(cabToLayout, cabT, tripStartDate, from, to);
        }
        if (cabF != null) {
            cabDetailsUiUpdate(cabFromLayout, cabF, tripEndDate, to, from);
        }
        if (trainT != null) {
            trainDetailsUiUpdate(cabToLayout, trainT, tripStartDate, from, to);
        }
        if (trainF != null) {
            trainDetailsUiUpdate(cabFromLayout, trainF, tripEndDate, to, from);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void fetchCurrentUserAndCoTravellers(ArrayList<String> coTravellerIds) {
        coTravellersTable.removeAllViews();
        TableRow headerRow = new TableRow(this);
        String[] headers = {"Name", "Age", "Gender"};
        for (String header : headers) {
            TextView headerTextView;
            if(header.equals("Name")){
                headerTextView = createTextView(header, true, true);
            }else{
                headerTextView = createTextView(header, true, false);
            }
            headerRow.addView(headerTextView);
        }


        coTravellersTable.addView(headerRow);
        View spacer = new View(this);
        spacer.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.row_bottom_margin)
        ));
        coTravellersTable.addView(spacer);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);
                            if (user != null) {
                                addCurrentUserToTable(user);
                                fetchCoTravellersData(coTravellerIds);
                            }
                        } else {
                            Log.d("CurrentUser", "No such document");
                            fetchCoTravellersData(coTravellerIds);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("CurrentUser", "Error getting document", e);
                        fetchCoTravellersData(coTravellerIds);
                    });
        } else {
            fetchCoTravellersData(coTravellerIds);
        }
    }

    private void addCurrentUserToTable(Users user) {
        TableRow row = new TableRow(this);
        TextView nameTextView = createTextView(user.getName(), false, true);
        mAuth = FirebaseAuth.getInstance();
        TextView ageTextView = createTextView(String.valueOf(calculateAge(user.getDateOfBirth())), false, false);
        TextView genderTextView = createTextView(user.getGender(), false, false);
        row.addView(nameTextView);
        row.addView(ageTextView);
        row.addView(genderTextView);
        coTravellersTable.addView(row);
        View spacer = new View(this);
        spacer.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.row_bottom_margin)
        ));
        coTravellersTable.addView(spacer);

    }


    private void fetchCoTravellersData(ArrayList<String> coTravellerIds) {
        // Clear existing rows

        // Add header row


        for (String coTravellerId : coTravellerIds) {
            db.collection("coTravellers").document(coTravellerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            CoTraveller coTraveller = documentSnapshot.toObject(CoTraveller.class);
                            if (coTraveller != null) {
                                TableRow row = new TableRow(this);

                                TextView nameTextView = createTextView(coTraveller.getName(), false, true);
                                TextView ageTextView = createTextView(String.valueOf(calculateAge(coTraveller.getDateOfBirth())), false, false);
                                TextView genderTextView = createTextView(coTraveller.getGender(), false, false);

                                row.addView(nameTextView);
                                row.addView(ageTextView);
                                row.addView(genderTextView);
                                coTravellersTable.addView(row);
                                View spacer = new View(this);
                                spacer.setLayoutParams(new TableRow.LayoutParams(
                                        TableRow.LayoutParams.MATCH_PARENT,
                                        (int) getResources().getDimension(R.dimen.row_bottom_margin)
                                ));
                                coTravellersTable.addView(spacer);
                            }
                        } else {
                            Log.d("CoTravellerData", "No such document");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("CoTravellerData", "Error getting document", e);
                    });
        }
        loadingDialog.dismiss();
    }

    private TextView createTextView(String text, boolean isHeader, boolean isName) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Equal weight
        if(isHeader){
            textView.setTextAppearance(R.style.train_fligh);
        }else{
            textView.setTextAppearance(R.style.travel_tick);
        }

        if (isName){
            textView.setGravity(android.view.Gravity.START);

        }
        else{
            textView.setGravity(android.view.Gravity.CENTER);
        }
        return textView;
    }
    private int calculateAge(Timestamp dobTimestamp) {
        if (dobTimestamp == null) {
            return -1;
        }

        Date dobDate = dobTimestamp.toDate();
        Calendar dob = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dob.setTime(dobDate);
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private void cabDetailsUiUpdate(ConstraintLayout id, CabDetails cab, String tripDate, String from, String to){
        LayoutInflater inflater = LayoutInflater.from(this);
        View cabView = inflater.inflate(R.layout.travel_cab_layout, id, false);

        TextView cabNumberView = cabView.findViewById(R.id.textView15);
        TextView tripDateView = cabView.findViewById(R.id.textView19);
        TextView tripStartTime = cabView.findViewById(R.id.tripTitle1);
        TextView tripEndTime = cabView.findViewById(R.id.tripTitle2);
        TextView source = cabView.findViewById(R.id.source);
        TextView dest = cabView.findViewById(R.id.destination);
        TextView dur = cabView.findViewById(R.id.duration);

        cabNumberView.setText("Vehicle No.: " + cab.getCabNumber());
        tripDateView.setText(formatDateCab(tripDate));
        tripStartTime.setText(cab.getStartTime());
        tripEndTime.setText(cab.getEndTime());
        source.setText(from);
        dest.setText(to);
        dur.setText(cab.getDuration());

        if (id == findViewById(R.id.ticketFro2)){
            cabNumberView.setVisibility(View.GONE);
        }

        id.addView(cabView);
    }

    private String formatDateCab(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void trainDetailsUiUpdate(ConstraintLayout id, TrainDetails train, String tripDate, String fromStation, String toStation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View trainView = inflater.inflate(R.layout.train_ticket_layout, id, false);

        TextView trainNumberView = trainView.findViewById(R.id.textView15);
        TextView pnrNumberView = trainView.findViewById(R.id.textView16);
        TextView trainNameView = trainView.findViewById(R.id.textView17);
        TextView travelClassView = trainView.findViewById(R.id.textView18);
        TextView tripDateView = trainView.findViewById(R.id.textView19);
        TextView tripEndDateView = trainView.findViewById(R.id.tripTitle0);
        TextView tripStartTime = trainView.findViewById(R.id.tripTitle1);
        TextView tripEndTime = trainView.findViewById(R.id.tripTitle2);
        TextView fromStationView = trainView.findViewById(R.id.tripTitle3);
        TextView toStationView = trainView.findViewById(R.id.tripTitle4);
        TextView sourceView = trainView.findViewById(R.id.source);
        TextView destinationView = trainView.findViewById(R.id.destination);
        TextView durationView = trainView.findViewById(R.id.duration);

        trainNumberView.setText("Train No.: " + train.getTrainNumber());
        pnrNumberView.setText("PRN: " + train.getPnrNumber());
        trainNameView.setText(train.getTrainName());
        travelClassView.setText(train.getTravelClass());
        tripDateView.setText(formatDateCab(tripDate));
        tripEndDateView.setText(formatDateCab(train.getTravelDestDate()));
        tripStartTime.setText(train.getDepartureTime());
        tripEndTime.setText(train.getArrivalTime());
        fromStationView.setText(train.getFromStation());
        toStationView.setText(train.getToStation());
        sourceView.setText(fromStation);
        destinationView.setText(toStation);
        durationView.setText(train.getDuration());

        id.addView(trainView);
    }
}