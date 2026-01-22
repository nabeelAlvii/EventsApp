package com.example.eventsapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventsapp.Models.EventsModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class AddEventActivity extends AppCompatActivity {
    private Uri selectedImageUri;
    ArrayList<EventsModel> eventList;
    EditText editTextTitle, editTextLocation, editTextDate, editTextTime, editTextDescription, editTextCreator;
    TextView galleryImg;
    ImageView imageViewEvents;
    private Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        getWindow().setStatusBarColor(Color.GRAY);

        // ---------------- View Binding ----------------
        galleryImg = findViewById(R.id.galleryImg);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCreator = findViewById(R.id.editTextCreator);
        imageViewEvents = findViewById(R.id.imageViewEvents);

        myCalendar = Calendar.getInstance();

        // Retrieve the event list from the Intent
        eventList = (ArrayList<EventsModel>) getIntent().getSerializableExtra("eventsList");
        if (eventList == null) {
            eventList = new ArrayList<>();
        }

        // ---------------- Open Events List ----------------
        imageViewEvents.setOnClickListener(v -> finish());

        // ---------------- Image Picker ----------------
        galleryImg.setOnClickListener(view -> ImagePicker.with(AddEventActivity.this)
                .crop()    // Crop image with 16:9 aspect ratio
                .compress(1024)   // Final image size will be less than 1 MB
                .maxResultSize(1080, 1080)    // Final image resolution will be less than 1080 x 1080
                .start());

        // ---------------- Add Event ----------------
        Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(view -> addEvent());

        // ---------------- Date & Time ----------------
        editTextDate.setOnClickListener(view -> showDatePickerDialog());
        editTextTime.setOnClickListener(view -> showTimePickerDialog());
    }

    // ================= ADD EVENT LOGIC =================
    private void addEvent() {
        String title = editTextTitle.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String creator = editTextCreator.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) ||
                TextUtils.isEmpty(date) || TextUtils.isEmpty(time) ||
                TextUtils.isEmpty(description) || TextUtils.isEmpty(creator)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- LOADING DIALOG SHOW ---
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Event...");
        progressDialog.setCancelable(false); // User beech mein cancel na kare
        progressDialog.show();

        // 1. Get Current Users ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 2. Reference ab User ke folder mein point karega: "Events / UserID"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Events")
                .child(userId);
        // 3. Ab ID generate karo
        String eventId = databaseReference.push().getKey();

        // Check karo: User ne Image select ki hai ya nahi?
        if (selectedImageUri != null) {
            // CASE A: Image hai -> Upload karo -> Phir Save karo

            // Storage mein folder path: "event_images/unique_id"
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("event_images/" + eventId);

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image Upload Success! Ab URL maango
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // Ab Database mein save karo (Image URL ke saath)
                            saveDataToDatabase(eventId, title, location, date, time, description, creator, downloadUrl, progressDialog);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toasty.error(AddEventActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            // CASE B: Image nahi hai -> Bina Image ke save karo
            saveDataToDatabase(eventId, title, location, date, time, description, creator, null, progressDialog);
        }
    }

    // --- HELPER METHOD: SAVE TO DATABASE ---
    private void saveDataToDatabase(String eventId, String title, String location, String date, String time, String description, String creator, String imageUrl, ProgressDialog progressDialog) {
        EventsModel event = new EventsModel(title, location, date, time, description, creator, imageUrl);
        event.setEventId(eventId);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase
                .getInstance()
                .getReference("Events")
                .child(userId)
                .child(eventId)
                .setValue(event)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss(); // Loading band karo
                    if (task.isSuccessful()) {
                        Toasty.success(AddEventActivity.this, "Event Published!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        finish();
                    } else {
                        Toasty.error(AddEventActivity.this, "Database Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Image picker result handling
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            galleryImg.setText(getImageName(selectedImageUri)); // Set image name in the TextView
        }
    }

    // ================= DATE PICKER =================
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    myCalendar.set(year, month, dayOfMonth);
                    updateDateLabel();
                },
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    // ================= TIME PICKER =================
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    myCalendar.set(Calendar.MINUTE, minute);
                    updateTimeLabel();
                },
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateTimeLabel() {
        String myFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editTextTime.setText(sdf.format(myCalendar.getTime()));
    }

    // ================= UTILS =================
    private String getImageName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void clearFields() {
        editTextTitle.setText("");
        editTextLocation.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        editTextDescription.setText("");
        editTextCreator.setText("");
        galleryImg.setText("Select Image");
        selectedImageUri = null;
    }
}


