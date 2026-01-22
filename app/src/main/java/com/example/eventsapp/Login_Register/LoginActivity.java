package com.example.eventsapp.Login_Register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventsapp.HomeActivity;
import com.example.eventsapp.Models.UsersModel;
import com.example.eventsapp.R;
import com.example.eventsapp.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 20; // Koi bhi number le sakte ho sakta hai (Request Code)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Build the sign-in request
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- 2. GOOGLE BUTTON CLICK ---
        binding.btnGoogleLogin.setOnClickListener(view -> signIn());

        // SignIn BUTTON CLICK ---
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            signInWithEmailAndPassword(email, password);
        });

        binding.tvRegister.setOnClickListener(view -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    public void signInWithEmailAndPassword(String email, String password) {

        if (email.isEmpty()) {
            binding.etEmail.setError("Enter Email");
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Enter Password");
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Please Wait...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true); // Button wapas chalu
                        binding.btnLogin.setText("SIGN IN");

                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toasty.error(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // --- 3. START SIGN IN INTENT ---
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // --- 4. HANDLE RESULT (Jab user Email select karlega) ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google se account mil gaya, ab Firebase se connect karo
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toasty.error(this, "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleLogin.setEnabled(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String uid = firebaseUser.getUid();

                        database.getReference()
                                .child("Users")
                                .child(uid)
                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {

                                        // ✅ USER PEHLE SE EXIST NAHI KARTA
                                        if (!snapshot.exists()) {

                                            UsersModel users = new UsersModel();
                                            users.setUserId(uid);
                                            users.setUsername(firebaseUser.getDisplayName());
                                            users.setEmail(firebaseUser.getEmail());
                                            users.setProfilePic(
                                                    firebaseUser.getPhotoUrl() != null
                                                            ? firebaseUser.getPhotoUrl().toString()
                                                            : ""
                                            );

                                            database.getReference()
                                                    .child("Users")
                                                    .child(uid)
                                                    .setValue(users);
                                        }

                                        binding.progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.btnGoogleLogin.setEnabled(true);
                                        Toasty.error(LoginActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnGoogleLogin.setEnabled(true);
                        Toasty.error(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}