package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hbb20.CountryCodePicker;

public class OnBoardingActivity extends AppCompatActivity {

    final int statusBarColor = R.color.primaryColor;
    CountryCodePicker countryCode;
    EditText phoneNo;
    ImageButton googleAuthBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        ImageButton nextBtn = findViewById(R.id.next_btn);
        countryCode = findViewById(R.id.country_code);
        phoneNo = findViewById(R.id.phoneNo);
        countryCode.registerCarrierNumberEditText(phoneNo);
        googleAuthBtn = findViewById(R.id.google_signin);
        nextBtn.setOnClickListener(view -> {
            if(!countryCode.isValidFullNumber()){
                phoneNo.setError("Please Enter a Valid Phone Number");
                return;
            }
            Intent iHome = new Intent(OnBoardingActivity.this, OTPVerificationActivity.class);
            iHome.putExtra("phone", countryCode.getFullNumberWithPlus());
            startActivity(iHome);
        });
    }
}