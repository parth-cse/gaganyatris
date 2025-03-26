package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class OnBoardingActivity extends AppCompatActivity {

    final int statusBarColor = R.color.primaryColor;
    CountryCodePicker countryCode;
    EditText phoneNo;
    ImageButton googleAuthBtn;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Log.w("Google Sign In", "Google sign in failed, code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                }
            });

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

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("185052209754-dcl1i4b2f9pf84fsf602n0iikdbq3qug.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        googleAuthBtn.setOnClickListener(view -> signIn());

        nextBtn.setOnClickListener(view -> {
            if (!countryCode.isValidFullNumber()) {
                phoneNo.setError("Please Enter a Valid Phone Number");
                return;
            }
            Intent iHome = new Intent(OnBoardingActivity.this, OTPVerificationActivity.class);
            iHome.putExtra("phone", countryCode.getFullNumberWithPlus());
            startActivity(iHome);
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfUserExists(user);
                    } else {
                        Toast.makeText(OnBoardingActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserExists(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // User exists, go to MainActivity
                    updateUI(user);
                } else {
                    // User does not exist, go to UserDetailsActivity
                    Intent userDetailsIntent = new Intent(OnBoardingActivity.this, UserDetailActivity.class);
                    userDetailsIntent.putExtra("uid", user.getUid());
                    startActivity(userDetailsIntent);
                    finish();
                }
            } else {
                Toast.makeText(OnBoardingActivity.this, "Failed to check user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();
            String personId = user.getUid();

            Intent mainIntent = new Intent(OnBoardingActivity.this, MainActivity.class);
            mainIntent.putExtra("name", personName);
            mainIntent.putExtra("email", personEmail);
            mainIntent.putExtra("id", personId);
            startActivity(mainIntent);
            finish();
        } else {
            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show();
        }
    }
}