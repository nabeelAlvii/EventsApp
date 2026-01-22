package com.example.eventsapp.Login_Register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventsapp.Models.EventsModel;
import com.example.eventsapp.HomeActivity;
import com.example.eventsapp.Models.UsersModel;
import com.example.eventsapp.R;
import com.example.eventsapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.btnRegister.setOnClickListener(view -> {

            // 1. Validation (Khali box check karo)
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            // Create User in Firebase
            registerUser(username, email, password);
        });

        binding.tvLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser(String username, String email, String password) {

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toasty.warning(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toasty.warning(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Button Click hone par (Loading Start)
        binding.progressBar.setVisibility(View.VISIBLE);
        // Button ko disable kar do taaki user dubara click na kare
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Creating Account...");

        // Firebase Call
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // Result aane par (Loading Stop)
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true); // Button wapas chalu
                        binding.btnRegister.setText("SIGN UP");

                        if (task.isSuccessful()) {
                            UsersModel users = new UsersModel(username, email, password);

                            String id = task.getResult().getUser().getUid();
                            database.getReference().child("Users").child(id).setValue(users); // "Users" node me save hoga

                            Toasty.success(RegisterActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toasty.error(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}