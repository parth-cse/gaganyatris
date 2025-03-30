package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

    private static final int STATUS_BAR_COLOR = R.color.primaryColor;
    private static final long TIMEOUT_SECONDS = 60L;

    private EditText[] otpFields;
    private TextView resend;
    private String phoneNo;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);
        setupWindowInsets();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));

        initializeViews();
        setupOTPInputs();

        phoneNo = Objects.requireNonNull(getIntent().getExtras()).getString("phone");
        sendOtp(phoneNo, false);

        findViewById(R.id.next_btn).setOnClickListener(view -> verifyOTP());
        resend.setOnClickListener(view -> sendOtp(phoneNo, true));
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        resend = findViewById(R.id.or);
        otpFields = new EditText[]{
                findViewById(R.id.otp1), findViewById(R.id.otp2),
                findViewById(R.id.otp3), findViewById(R.id.otp4),
                findViewById(R.id.otp5), findViewById(R.id.otp6)
        };
        loadingDialog = new LoadingDialog(this);
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty() && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[index].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN
                        && otpFields[index].getText().toString().isEmpty() && index > 0) {
                    otpFields[index - 1].requestFocus();
                }
                return false;
            });
        }
    }

    private void verifyOTP() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText otpField : otpFields) {
            otpBuilder.append(otpField.getText().toString());
        }
        String enteredOTP = otpBuilder.toString();

        if (enteredOTP.length() != 6) {
            Toast.makeText(this, "Please enter all OTP digits", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.setMessage("Verifying OTP...");
        loadingDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
        signIn(credential);
    }

    private void sendOtp(String phone, boolean isResent) {
        loadingDialog.setMessage("Sending OTP...");
        loadingDialog.show();

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        loadingDialog.dismiss();
                        signIn(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        loadingDialog.dismiss();
                        Toast.makeText(OTPVerificationActivity.this, "Login Process Failed", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        loadingDialog.dismiss();
                        startResendTimer();
                        verificationCode = verificationId;
                        resendingToken = token;
                        Toast.makeText(OTPVerificationActivity.this, "OTP has been sent to " + phone, Toast.LENGTH_SHORT).show();
                    }
                });

        if (isResent) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void signIn(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingDialog.dismiss();
                if (task.isSuccessful()) {
                    navigateToMainActivity();
                } else {
                    Toast.makeText(OTPVerificationActivity.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("phone", phoneNo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startResendTimer() {
        resend.setTextColor(Color.parseColor("#66000000"));
        resend.setEnabled(false);
        new Timer().schedule(new TimerTask() {
            long remainingSeconds = TIMEOUT_SECONDS;

            @Override
            public void run() {
                runOnUiThread(() -> {
                    resend.setText("Resend OTP in " + remainingSeconds);
                    if (remainingSeconds <= 0) {
                        cancel();
                        runOnUiThread(() -> {
                            resend.setText("Resend OTP");
                            resend.setTextColor(Color.parseColor("#4285F4"));
                            resend.setEnabled(true);
                        });
                    }
                    remainingSeconds--;
                });
            }
        }, 0, 1000);
    }
}