package com.example.eventsapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventsapp.Models.EventsModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class EventDetailsActivity extends AppCompatActivity {
    // UI Components declare kar rahe hain
    TextView title, location, date, time, description, creator;
    ImageView imageView;
    ImageButton deleteButton;
    EventsModel currentEvent; // Jo event hum dekh rahe hain

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        title = findViewById(R.id.title);
        location = findViewById(R.id.location);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        description = findViewById(R.id.description);
        creator = findViewById(R.id.creator);
        imageView = findViewById(R.id.imageView);
        deleteButton = findViewById(R.id.deleteButton);


        if (getIntent().hasExtra("selected_event")) {
            currentEvent = (EventsModel) getIntent().getSerializableExtra("selected_event");

            if (currentEvent != null) {
                title.setText(currentEvent.getTitle());
                location.setText(currentEvent.getLocation());
                date.setText(currentEvent.getDate());
                time.setText(currentEvent.getTime());
                description.setText(currentEvent.getDescription());
                creator.setText(currentEvent.getCreator());

                // Image Set karna using Glide
                if (currentEvent.getImageUri() != null && !currentEvent.getImageUri().isEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(currentEvent.getImageUri())
                            .placeholder(R.drawable.event) // Placeholder image
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.event);
                }
            }
        }

        deleteButton.setOnClickListener(v -> {
            // Direct delete karne se pehle user se confirm karo (Good UX)
            new AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteEventFromFirebase();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void deleteEventFromFirebase() {
        if (currentEvent != null && currentEvent.getEventId() != null) {
            // 1. Firebase ka Reference lo
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Events");

            // 2. Specific ID ko remove karo
            databaseReference.child(currentEvent.getEventId()).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(EventDetailsActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                            finish(); // Wapis Home screen par jao
                        } else {
                            Toasty.error(EventDetailsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toasty.error(EventDetailsActivity.this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
        }
    }
}