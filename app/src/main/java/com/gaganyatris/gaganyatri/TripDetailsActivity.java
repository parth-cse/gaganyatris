package com.gaganyatris.gaganyatri;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.models.Users;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

public class TripDetailsActivity extends AppCompatActivity {
    private final int statusBarColor = R.color.primaryColor;
    private String startDate, endDate, from, to, budget, tripType, travelMode;

    private String arrivalAtTripDest, returnTimeFromTripDest, arrivalDate, returnDate;
    private boolean exploreNearby, tripTicketEnteredTo = false, tripTicketEnteredFro = false;
    private int totalTripDays, prnStatus = 5;
    private String avgTime;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private TableLayout coTravellersTable;
    private TextView tripMode, ticketStatus;
    private ImageView statusIcon;
    private ConstraintLayout ticketTo, ticketFro;
    ArrayList<String> coTravellers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        initViews();
        setupNavigationButtons();
        fetchTripData();
    }

    private void initViews() {
        coTravellersTable = findViewById(R.id.coTravellersTable);
        tripMode = findViewById(R.id.mode);
        ticketStatus = findViewById(R.id.ticketStatus);
        statusIcon = findViewById(R.id.statusIcon);
        ticketTo = findViewById(R.id.ticketTo2);
        ticketFro = findViewById(R.id.ticketFro2);
    }

    private void setupNavigationButtons() {
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.tripPlan).setOnClickListener(v -> {
            Intent intent = new Intent(this, TripPlanActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("tripStartDate", startDate);
            bundle.putString("tripEndDate", endDate);
            bundle.putString("from", from);
            bundle.putString("to", to);
            bundle.putString("budget", budget);
            bundle.putString("tripType", tripType);
            bundle.putBoolean("exploreNearby", exploreNearby);
            bundle.putInt("totalTripDays", totalTripDays);
            bundle.putString("selectedMode", travelMode);
            bundle.putString("arrivalDate", arrivalDate);
            bundle.putString("returnDate", returnDate);
            bundle.putString("arrivalTime", arrivalAtTripDest);
            bundle.putString("returnTime", returnTimeFromTripDest);
            bundle.putInt("numberOfTravellers", coTravellers.size()+1);
            bundle.putStringArrayList("coTraveller", coTravellers);
            // Add any other data you need to transfer

            intent.putExtras(bundle);
            startActivity(intent);
        });
        findViewById(R.id.checkList).setOnClickListener(v -> startActivity(new Intent(this, TripCheckListActivity.class)));
        findViewById(R.id.mapView).setOnClickListener(v -> startActivity(new Intent(this, TripMapViewActivity.class)));
    }

    private void fetchTripData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            startDate = bundle.getString("tripStartDate");
            endDate = bundle.getString("tripEndDate");
            from = bundle.getString("from");
            to = bundle.getString("to");
            budget = bundle.getString("budget");
            tripType = bundle.getString("tripType");
            exploreNearby = bundle.getBoolean("exploreNearby");
            totalTripDays = bundle.getInt("totalTripDays");
            travelMode = bundle.getString("selectedMode");
            coTravellers = bundle.getStringArrayList("selectedCoTravellers");
            if(travelMode.equals("Cab")){
                avgTime = bundle.getString("avgTime");
            }

            mAuth = FirebaseAuth.getInstance();
            fetchCurrentUserAndCoTravellers(coTravellers);

            ((TextView) findViewById(R.id.noOfDays)).setText(totalTripDays + " Days");
            ((TextView) findViewById(R.id.tripTitle)).setText("Trip to " + to);
            ((TextView) findViewById(R.id.tripStartDate)).setText(formatDate(startDate));
            ((TextView) findViewById(R.id.tripEndDate)).setText(formatDate(endDate));
        }
        setTravelMode();
        updateTicketStatus();
        checkTripTicketEntered();
    }

    private void checkTripTicketEntered() {
        if ("Train".equals(travelMode)) {
            findViewById(R.id.addTicketButton).setOnClickListener(v -> {
                showPnrInputDialog(findViewById(R.id.ticketTo2));

            });
            findViewById(R.id.addTicketButton2).setOnClickListener(v -> {
                showPnrInputDialog(findViewById(R.id.ticketFro2));

            });
        }else if ("Cab".equals(travelMode)) {
            findViewById(R.id.addTicketButton).setOnClickListener(v -> {
                showCabInputDialog(findViewById(R.id.ticketTo2));
            });
            findViewById(R.id.addTicketButton2).setOnClickListener(v -> {
                showCabInputDialog(findViewById(R.id.ticketFro2));

            });
        } else {
            findViewById(R.id.addTicketButton).setOnClickListener(v -> {
                tripTicketEnteredTo = true;
                checkTripTicketEntered();
            });
        }

        if (tripTicketEnteredTo) {
            if(tripTicketEnteredFro){
                findViewById(R.id.addButton2).setVisibility(View.GONE);
                ticketFro.setVisibility(View.VISIBLE);
            }else{
                ticketTo.setVisibility(View.VISIBLE);
                findViewById(R.id.breaker).setVisibility(View.VISIBLE);
                findViewById(R.id.ticketFro).setVisibility(View.VISIBLE);
                findViewById(R.id.addButton).setVisibility(View.GONE);
                findViewById(R.id.addButton2).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.ticketFro).setVisibility(View.GONE);
            ticketTo.setVisibility(View.GONE);
            ticketFro.setVisibility(View.GONE);
            findViewById(R.id.breaker).setVisibility(View.GONE);
            findViewById(R.id.addButton).setVisibility(View.VISIBLE);
        }
    }

    private void setTravelMode() {
        if (travelMode == null) {
            tripMode.setText("No Specified");
            return;
        }
        switch (travelMode) {
            case "Flight":
                tripMode.setText("By Flight");
                break;
            case "Train":
                tripMode.setText("By Indian Railways");
                break;
            case "Bus":
                tripMode.setText("By State Transport or Private Bus");
                break;
            case "Cab":
                ((TextView) findViewById(R.id.travelTitle)).setText("Trip Details");
                tripMode.setText("By Private Vehicle");
                break;
            default:
                tripMode.setText("No Specified");
                break;
        }
    }

    private void updateTicketStatus() {
        int textColor;
        int iconRes;
        String statusText;

        switch (prnStatus) {
            case 0:
                statusText = "Pending";
                textColor = R.color.primaryColor;
                iconRes = R.drawable.ticket_red;
                break;
            case 1:
                statusText = "Waiting";
                textColor = R.color.holo_yellow;
                iconRes = R.drawable.ticket_yellow;
                break;
            case 2:
                statusText = "Confirm";
                textColor = R.color.holo_green_dark;
                iconRes = R.drawable.ticket_confirm;
                break;
            default:
                statusText = "Not Applicable";
                textColor = R.color.holo_yellow;
                iconRes = R.drawable.ticket_yellow;
                break;
        }

        ticketStatus.setText(statusText);
        ticketStatus.setTextColor(ContextCompat.getColor(this, textColor));
        statusIcon.setImageResource(iconRes);
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

    private void showPnrInputDialog(ConstraintLayout container) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PNR Number");

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String pnrNumber = input.getText().toString().trim();
            if (pnrNumber.length() == 10) {
                fetchPnrStatus(pnrNumber, container);
            } else {
                Toast.makeText(this, "Please enter a valid 12-digit PNR number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void fetchPnrStatus(String pnrNumber, ConstraintLayout container) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader in = null;

            try {
                String urlString = "https://irctc1.p.rapidapi.com/api/v3/getPNRStatus?pnrNumber=" + pnrNumber;
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                // Set Request Method and Headers
                conn.setRequestMethod("GET");
                conn.setRequestProperty("x-rapidapi-key", "779d4a6dbfmshb73ad0bc58fc86ap143147jsn10cc719e6d29");
                conn.setRequestProperty("x-rapidapi-host", "irctc1.p.rapidapi.com");
                conn.setConnectTimeout(10000); // 10 seconds timeout
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }

                    String responseBody = response.toString();
                    Log.d("PNR_Response", responseBody); // Log the response

                    // Update UI on the main thread
                    String finalResponse = responseBody;
                    runOnUiThread(() -> updateUiWithPnrData(finalResponse, container));
                } else {
                    Log.e("PNR_Error", "Failed to fetch data. Response Code: " + responseCode);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch PNR status", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("PNR_Exception", "Error fetching PNR status", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error fetching PNR status", Toast.LENGTH_SHORT).show());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("PNR_Exception", "Error closing BufferedReader", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private String formatDuration(String duration) {
        try {
            String[] parts = duration.split(":"); // Split "7:26" into ["7", "26"]
            return parts[0] + "h" + parts[1] + "m"; // Convert to "7h26m"
        } catch (Exception e) {
            e.printStackTrace();
            return duration; // Return original if parsing fails
        }
    }

    private void updateUiWithPnrData(String jsonResponse, ConstraintLayout id) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject data = jsonObject.getJSONObject("data");

            String trainNo = data.getString("TrainNo");
            String trainName = data.getString("TrainName");
            String pnr = data.getString("Pnr");
            String travelDate = data.getString("Doj");
            String destinationReached = data.getString("DestinationDoj");
            String fromStation = data.getString("BoardingPoint");
            String toStation = data.getString("To");
            String departureTime = data.getString("DepartureTime");
            String arrivalTime = data.getString("ArrivalTime");
            String travelClass = data.getString("Class");
            String tripDuration = data.getString("Duration");
            String source = data.getString("SourceName");
            String dest = data.getString("DestinationName");

            if(id == findViewById(R.id.ticketTo2)){
                arrivalAtTripDest = arrivalTime;
                arrivalDate = destinationReached;
            }else{
                if(id == findViewById(R.id.ticketFro2)){
                    returnTimeFromTripDest = departureTime;
                    returnDate = travelDate;
                }
            }

            // Find the parent layout
            id.removeAllViews(); // Remove any previous views before adding new one

            // Inflate the train_ticket_layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View ticketView = inflater.inflate(R.layout.train_ticket_layout, id, false);

            // Get references to TextViews in train_ticket_layout
            TextView trainNoView = ticketView.findViewById(R.id.textView15);
            TextView pnrView = ticketView.findViewById(R.id.textView16);
            TextView trainNameView = ticketView.findViewById(R.id.textView17);
            TextView travelClassView = ticketView.findViewById(R.id.textView18);
            TextView travelStartDateView = ticketView.findViewById(R.id.textView19);
            TextView travelDestDateView = ticketView.findViewById(R.id.tripTitle0);
            TextView fromStationView = ticketView.findViewById(R.id.tripTitle3);
            TextView toStationView = ticketView.findViewById(R.id.tripTitle4);
            TextView departureTimeView = ticketView.findViewById(R.id.tripTitle1);
            TextView arrivalTimeView = ticketView.findViewById(R.id.tripTitle2);
            TextView durationView = ticketView.findViewById(R.id.duration);
            TextView sourceView = ticketView.findViewById(R.id.source);
            TextView destView = ticketView.findViewById(R.id.destination);

            // Set data to the views
            trainNoView.setText("Train No.: " + trainNo);
            pnrView.setText("PNR: " + pnr);
            trainNameView.setText(trainName);
            travelClassView.setText(travelClass);
            travelStartDateView.setText(formatDateTrain(travelDate));
            travelDestDateView.setText(formatDateTrain(destinationReached));
            fromStationView.setText(fromStation);
            toStationView.setText(toStation);
            departureTimeView.setText(departureTime);
            arrivalTimeView.setText(arrivalTime);
            durationView.setText(formatDuration(tripDuration));
            sourceView.setText(source);
            destView.setText(dest);


            // Add the inflated layout to ticketTo2
            id.addView(ticketView);

            // Make ticketTo2 visible
            id.setVisibility(View.VISIBLE);
            if(id == findViewById(R.id.ticketTo2)){
                tripTicketEnteredTo = true;
            }else{
                if (id == findViewById(R.id.ticketFro2)){
                    tripTicketEnteredFro = true;
                }
            }
            checkTripTicketEntered();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateTrain(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // Return original if parsing fails
        }
    }

    private String formatDateCab(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // Return original if parsing fails
        }
    }
    private void showCabInputDialog(ConstraintLayout id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Vehicle Details");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Cab Number Input
        final EditText cabNumberInput = new EditText(this);
        cabNumberInput.setHint("Enter Vehicle Number");
        cabNumberInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(cabNumberInput);

        // If ID is ticketFro2, include Date Picker
        final TextView tripDateText = new TextView(this);
        final Button tripDateButton = new Button(this);
        if (id == findViewById(R.id.ticketFro2)) {
            tripDateButton.setText("Select Trip Return Date");
            layout.addView(tripDateButton);
            layout.addView(tripDateText);

            tripDateButton.setOnClickListener(v -> pickReturnDate(tripDateText));
        }

        // Trip Start Time Button
        final Button startTimeButton = new Button(this);
        startTimeButton.setText("Select Trip Start Time");
        layout.addView(startTimeButton);

        final TextView startTimeText = new TextView(this);
        layout.addView(startTimeText);

        startTimeButton.setOnClickListener(v -> pickTime(startTimeText));

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String cabNumber = cabNumberInput.getText().toString().trim();
            String startTime = startTimeText.getText().toString().trim();
            String tripDate = (id == findViewById(R.id.ticketFro2)) ? tripDateText.getText().toString().trim() : startDate;

            if (cabNumber.isEmpty() || startTime.isEmpty() || (id == findViewById(R.id.ticketFro2) && tripDate.isEmpty())) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show();
            } else {
                saveCabDetails(cabNumber, startTime, tripDate, id);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveCabDetails(String cabNumber, String startTime, String tripDate, ConstraintLayout id) {
        if (avgTime == null || avgTime.trim().isEmpty()) {
            Toast.makeText(this, "Average travel time is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View cabView = inflater.inflate(R.layout.travel_cab_layout, id, false);

        TextView cabNumberView = cabView.findViewById(R.id.textView15);
        TextView tripDateView = cabView.findViewById(R.id.textView19);
        TextView tripEndDate = cabView.findViewById(R.id.tripTitle0);
        TextView tripStartTime = cabView.findViewById(R.id.tripTitle1);
        TextView tripEndTime = cabView.findViewById(R.id.tripTitle2);
        TextView source = cabView.findViewById(R.id.source);
        TextView dest = cabView.findViewById(R.id.destination);

        cabNumberView.setText("Vehicle No.: " + cabNumber);
        tripDateView.setText(formatDateCab(tripDate)); // Use selected trip date
        tripStartTime.setText(startTime);
        source.setText(from);
        dest.setText(to);

        if (id == findViewById(R.id.ticketFro2)){
            cabNumberView.setVisibility(View.GONE);
        }

        // Extract numbers from avgTime (e.g., "6-7 hours" or "6 hr")
        int minHours = 0, maxHours = 0;
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(avgTime.trim());

        List<Integer> hourValues = new ArrayList<>();
        while (matcher.find()) {
            hourValues.add(Integer.parseInt(matcher.group()));
        }

        if (hourValues.isEmpty()) {
            Toast.makeText(this, "Invalid travel time format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hourValues.size() == 1) {
            minHours = maxHours = hourValues.get(0);
        } else {
            minHours = hourValues.get(0);
            maxHours = hourValues.get(1);
        }

        int avgHours = (minHours + maxHours) / 2;

        // Parse startTime (e.g., "13:30")
        String[] timeParts = startTime.split(":");
        int startHour = Integer.parseInt(timeParts[0]);
        int startMinute = Integer.parseInt(timeParts[1]);

        // Correct date format for parsing tripDate
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar tripStartCal = Calendar.getInstance();

        try {
            tripStartCal.setTime(dateFormat.parse(tripDate)); // Parse selected trip date correctly
        } catch (Exception e) {
            e.printStackTrace();
        }

        tripStartCal.set(Calendar.HOUR_OF_DAY, startHour);
        tripStartCal.set(Calendar.MINUTE, startMinute);

        // Store original date before modification
        int originalDay = tripStartCal.get(Calendar.DAY_OF_MONTH);

        // Add avgHours to start time
        tripStartCal.add(Calendar.HOUR_OF_DAY, avgHours);

        // Check if date changed after adding avgHours
        boolean crossesMidnight = tripStartCal.get(Calendar.DAY_OF_MONTH) > originalDay;

        // Get final values
        String endTime = String.format(Locale.getDefault(), "%02d:%02d",
                tripStartCal.get(Calendar.HOUR_OF_DAY),
                tripStartCal.get(Calendar.MINUTE));

        String endDate = dateFormat.format(tripStartCal.getTime()); // Get updated date in "dd/MM/yyyy"

        // Set values in UI
        tripEndTime.setText(endTime);
        tripEndDate.setText(formatDateCab(endDate));

        if(id == findViewById(R.id.ticketTo2)){
            arrivalAtTripDest = endTime;
            arrivalDate = endDate;
        }else{
            if(id == findViewById(R.id.ticketFro2)){
                returnTimeFromTripDest = startTime;
                returnDate = tripDate;
            }
        }

        id.removeAllViews();
        id.addView(cabView);

        if (id == findViewById(R.id.ticketTo2)){
            tripTicketEnteredTo = true;
            checkTripTicketEntered();
        }else if(id == findViewById(R.id.ticketFro2)){
            tripTicketEnteredFro=true;
            checkTripTicketEntered();
        }
    }

    private void pickTime(TextView timeTextView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    timeTextView.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void pickReturnDate(TextView dateTextView) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        try {
            startCal.setTime(dateFormat.parse(startDate)); // Set start date
            endCal.setTime(dateFormat.parse(endDate)); // Set end date
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            String selectedDate = dateFormat.format(selectedCal.getTime());
            dateTextView.setText(selectedDate);
        }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));

        // Restrict available dates
        datePickerDialog.getDatePicker().setMinDate(startCal.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(endCal.getTimeInMillis());

        datePickerDialog.show();
    }


}
