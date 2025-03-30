package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

public class OnBoardingActivity extends AppCompatActivity {

    private static final String TAG = "OnBoardingActivity";
    private static final String GOOGLE_CLIENT_ID = "185052209754-gji05ih4jgu5u579qm1ar2uc4mjo4sf5.apps.googleusercontent.com";
    private static final int STATUS_BAR_COLOR = R.color.primaryColor;

    private CountryCodePicker countryCode;
    private EditText phoneNo;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);
        setupInsets();
        setupUI();
        configureGoogleSignIn();
        setupListeners();
    }

    private void setupInsets() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
    }

    private void setupUI() {
        ImageButton nextBtn = findViewById(R.id.next_btn);
        countryCode = findViewById(R.id.country_code);
        phoneNo = findViewById(R.id.phoneNo);
        countryCode.registerCarrierNumberEditText(phoneNo);
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupListeners() {
        findViewById(R.id.google_signin).setOnClickListener(view -> signIn());
        findViewById(R.id.next_btn).setOnClickListener(view -> handleNextButtonClick());
    }

    private void signIn() {
        signInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed, code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
            Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkIfUserExists(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(OnBoardingActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserExists(FirebaseUser user) {
        firestore.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            updateUI(user);
                        } else {
                            startUserDetailsActivity(user.getUid());
                        }
                    } else {
                        Toast.makeText(OnBoardingActivity.this, "Failed to check user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
            intent.putExtra("name", user.getDisplayName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("id", user.getUid());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show();
        }
    }

    private void startUserDetailsActivity(String uid) {
        Intent intent = new Intent(OnBoardingActivity.this, UserDetailActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
        finish();
    }

    private void handleNextButtonClick() {
        if (!countryCode.isValidFullNumber()) {
            phoneNo.setError("Please Enter a Valid Phone Number");
            return;
        }
        Intent intent = new Intent(OnBoardingActivity.this, OTPVerificationActivity.class);
        intent.putExtra("phone", countryCode.getFullNumberWithPlus());
        startActivity(intent);
    }
}