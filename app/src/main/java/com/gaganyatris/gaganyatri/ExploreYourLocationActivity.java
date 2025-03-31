package com.gaganyatris.gaganyatri;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ExploreYourLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText fromEditText;
    private AutoCompleteTextView timeAutoCompleteTextView;
    private AutoCompleteTextView tripTypeAutoCompleteTextView;
    private MapView mapView;
    private GoogleMap googleMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LoadingDialog loadingDialog;
    private final int statusBarColor = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_your_location);

        setupWindowInsets();
        setupUI();
        setupMap(savedInstanceState);
        setupLocation();
        setupDropdowns();
        setupSaveButton();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
    }

    private void setupUI() {
        ImageButton backBTN = findViewById(R.id.backBtn);
        backBTN.setOnClickListener(v -> finish());
        fromEditText = findViewById(R.id.from);
        timeAutoCompleteTextView = findViewById(R.id.time);
        tripTypeAutoCompleteTextView = findViewById(R.id.type_of_trip);
        fromEditText.setEnabled(false);
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.setVisibility(View.VISIBLE);
    }

    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        getCurrentLocation();
    }

    private void setupDropdowns() {
        populateTimeDropdown();
        populateTripTypeDropdown();
    }

    private void setupSaveButton() {
        findViewById(R.id.btn_save_next).setOnClickListener(v -> sendDataToNextActivity());
    }

    private void sendDataToNextActivity() {
        String from = fromEditText.getText().toString();
        String time = timeAutoCompleteTextView.getText().toString();
        String tripType = tripTypeAutoCompleteTextView.getText().toString();

        Intent intent = new Intent(this, ExploreLocationResultActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("time", time);
        intent.putExtra("tripType", tripType);
        startActivity(intent);
        finish();
    }

    private void populateTimeDropdown() {
        String[] timeOptions = {"1 hr", "1 hr 30 min", "2 hr", "2 hr 30 min", "3 hr", "3 hr 30 min", "4 hr", "4 hr 30 min", "5 hr", "5 hr 30 min", "6 hr", "6 hr 30 min", "7 hr", "7 hr 30 min", "8 hr", "8 hr 30 min", "9 hr", "9 hr 30 min"};
        setupAutoCompleteTextView(timeAutoCompleteTextView, timeOptions);
    }

    private void populateTripTypeDropdown() {
        String[] tripTypeOptions = {"Religious", "Historic", "Educational", "Friends Trip", "Family Trip", "Solo Explorer"};
        setupAutoCompleteTextView(tripTypeAutoCompleteTextView, tripTypeOptions);
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setKeyListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            getAddressFromLatLng(latLng);
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult.addOnSuccessListener(this, location -> {
                handleLocationResult(location);
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void handleLocationResult(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            getAddressFromLatLng(latLng);
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i)).append("\n");
                }
                fromEditText.setText(addressText.toString());
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        }
    }
}