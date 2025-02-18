package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;

public class OTPVerificationActivity extends AppCompatActivity {
    final int statusBarColor = R.color.primaryColor;
    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    TextView resend;
    Long timeOutSeconds = 60L;
    String phoneNo;

    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        resend = findViewById(R.id.or);
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        setupOTPInputs();

        phoneNo = Objects.requireNonNull(getIntent().getExtras()).getString("phone");
        sendOtp(phoneNo, false);

        ImageButton nextBtn = findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(view -> {
            String enteredOTP = otp1.getText().toString() +
                    otp2.getText().toString() +
                    otp3.getText().toString() +
                    otp4.getText().toString() +
                    otp5.getText().toString() +
                    otp6.getText().toString();

            if (enteredOTP.length() != 6) {
                Toast.makeText(OTPVerificationActivity.this, "Please enter all OTP digits", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.setMessage("Verifying OTP...");
            loadingDialog.show();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
            signIn(credential, loadingDialog);
        });

        resend.setOnClickListener(view -> sendOtp(phoneNo, true));
    }

    private void setupOTPInputs() {
        EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};

        for (int i = 0; i < otpFields.length; i++) {
            int index = i;

            otpFields[index].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty() && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });

            otpFields[index].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (otpFields[index].getText().toString().isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    void sendOtp(String phone, boolean isResent) {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Sending OTP...");
        loadingDialog.show();

        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone).setTimeout(timeOutSeconds, TimeUnit.SECONDS)
                        .setActivity(this).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                loadingDialog.dismiss();
                                signIn(phoneAuthCredential, loadingDialog);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                loadingDialog.dismiss();
                                Toast.makeText(OTPVerificationActivity.this, "Login Process Failed", Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                loadingDialog.dismiss();
                                startResendTimer();
                                verificationCode = s;
                                resendingToken = forceResendingToken;
                                Toast.makeText(OTPVerificationActivity.this, "OTP has been sent to" + phone, Toast.LENGTH_SHORT).show();
                            }

                        });
        if (isResent) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential, LoadingDialog loadingDialog) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingDialog.dismiss();

                if (task.isSuccessful()) {
                    Intent iHome = new Intent(OTPVerificationActivity.this, MainActivity.class);
                    iHome.putExtra("phone", phoneNo);
                    iHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(iHome);
                } else {
                    Toast.makeText(OTPVerificationActivity.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    void startResendTimer() {
        resend.setTextColor(Color.parseColor("#66000000"));
        resend.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    timeOutSeconds--;
                    resend.setText("Resend OTP in " + timeOutSeconds);
                    if (timeOutSeconds <= 0) {
                        timeOutSeconds = 60L;
                        timer.cancel();
                        resend.setText("Resend OTP");
                        resend.setTextColor(Color.parseColor("#4285F4"));
                        resend.setEnabled(true);
                    }
                });
            }
        }, 0, 1000);
    }
}