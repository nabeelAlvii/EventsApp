package com.example.eventsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventsapp.Login_Register.LoginActivity;
import com.example.eventsapp.Models.EventsModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

// EventsListActivity list of summarized events
public class HomeActivity extends AppCompatActivity {
    private ArrayList<EventsModel> eventList;
    private RecyclerView recyclerView;
    private EventsAdapter eventAdapter;
    private View emptyStateView;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Make sure layout name sahi ho

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        // Default Action Bar chupane ke liye
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerView);
        emptyStateView = findViewById(R.id.emptyStateView);
        progressBar = findViewById(R.id.progressBar);
        TextView tvDate = findViewById(R.id.tvDate);

        // logout logic
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            // 1. Popup Menu banao
            PopupMenu popup = new PopupMenu(HomeActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_home, popup.getMenu());

            // 2. Click handle karo
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {

                    // Firebase se Sign Out karo
                    FirebaseAuth.getInstance().signOut();

                    // 2. Google Client se sign out (Ye important hai "Account Selection" wapas lane ke liye)
                    gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Jab Google sign out complete ho jaye, tabhi screen change karo
                            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            Toasty.success(HomeActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }
                return false;
            });

            // 3. Menu dikhao
            popup.show();
        });

        // --- DATE SETTING LOGIC ---
        tvDate = findViewById(R.id.tvDate); // XML mein maine ID di thi
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
        tvDate.setText(dateFormat.format(currentDate));

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventsAdapter(this, eventList, event -> showDeleteDialog(event));
        recyclerView.setAdapter(eventAdapter);

        // --- 3. Firebase Setup ---
        // "Events" folder se connect kar rahe hain
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Events")
                    .child(userId); // Sirf apne folder se data uthao

            // Data fetch karna start karo
            fetchEventsFromFirebase();
        } else {
            // Agar user login nahi hai, toh Login Screen par bhejo
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish(); // HomeActivity band kar do
        }

        // --- FAB Click (Add Event) ---
        FloatingActionButton fab = findViewById(R.id.buttonAddMoreEvents);
        fab.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AddEventActivity.class)));
    }

    private void fetchEventsFromFirebase() {
        // Loading dikhao
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Purani list saaf karo (warna duplicates aayenge)
                eventList.clear();

                // Loop through all children (saare events check karo)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // JSON data ko Java Object mein convert karo
                    EventsModel event = dataSnapshot.getValue(EventsModel.class);
                    eventList.add(event);
                }

                // List ko ulta karo taaki naya event sabse upar aaye
                Collections.reverse(eventList);

                // Adapter ko bolo data badal gaya
                eventAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                // Empty State Logic
                if (eventList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toasty.error(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Delete Confirmation Dialog dikhane ke liye
    private void showDeleteDialog(EventsModel event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete '" + event.getTitle() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Agar Yes bola, toh delete karo
                    deleteEventFromFirebase(event);
                })
                .setNegativeButton("No", null) // No bola toh kuch mat karo
                .show();
    }

    // Actual Firebase Delete Logic
    private void deleteEventFromFirebase(EventsModel event) {
        if (event.getEventId() != null) {
            databaseReference.child(event.getEventId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toasty.success(HomeActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                         /*Humein list refresh karne ki zaroorat nahi hai.
                         Kyunki humne 'addValueEventListener' lagaya hai, Firebase khud list update kar dega!*/
                    })
                    .addOnFailureListener(e -> {
                        Toasty.error(HomeActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
