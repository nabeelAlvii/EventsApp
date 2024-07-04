package com.example.eventsapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventDeleteListener {
    private Uri selectedImageUri;
    private ArrayList<Event> eventList;
    EditText editTextTitle, editTextLocation, editTextDate, editTextTime, editTextDescription, editTextCreator;
    TextView galleryImg;
    RecyclerView recyclerView;
    EventAdapter eventAdapter;
    private Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.GRAY);

        // Initialize views from activity_main.xml
        galleryImg = findViewById(R.id.galleryImg);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCreator = findViewById(R.id.editTextCreator);
        recyclerView = findViewById(R.id.recyclerView);

        // Retrieve the event list from the Intent
        eventList = (ArrayList<Event>) getIntent().getSerializableExtra("eventsList");
        if (eventList == null) {
            eventList = new ArrayList<>();
        }

        // Initialize event adapter
        eventAdapter = new EventAdapter(MainActivity.this, eventList, MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(eventAdapter);

        // Set onClickListener for adding the image
        galleryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .crop(16f, 9f)    // Crop image with 16:9 aspect ratio
                        .compress(1024)   // Final image size will be less than 1 MB
                        .maxResultSize(1080, 1080)    // Final image resolution will be less than 1080 x 1080
                        .start();
            }
        });

        Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve event details from EditText fields
                String title = editTextTitle.getText().toString();
                String location = editTextLocation.getText().toString();
                String date = editTextDate.getText().toString();
                String time = editTextTime.getText().toString();
                String description = editTextDescription.getText().toString();
                String creator = editTextCreator.getText().toString();

                // Check if any field is empty
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) ||
                        TextUtils.isEmpty(date) || TextUtils.isEmpty(time) ||
                        TextUtils.isEmpty(description) || TextUtils.isEmpty(creator)) {
                    Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Add the event to the list
                    Event event = new Event(title, location, date, time, description, creator, selectedImageUri != null ? selectedImageUri.toString() : null);
                    eventList.add(event);

                    // Navigate to EventsActivity and pass the event list
                    Intent intent = new Intent(MainActivity.this, EventsActivity.class);
                    intent.putExtra("eventsList", eventList);
                    startActivity(intent);

                    // Clear the input fields
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
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        myCalendar = Calendar.getInstance();

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }



    // Image picker result handling
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            String imageName = getImageName(selectedImageUri); // Get image name from URI
            galleryImg.setText(imageName); // Set image name in the TextView
        }
    }

    // #
    public void onEventLongClicked(Event event) {
        // Show delete confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the event from the list and notify the adapter
                        eventList.remove(event);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }

    public void onEventDeleteClicked(Event event) {
        // Show delete confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the event from the list and notify the adapter
                        eventList.remove(event);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }


    private void showTimePickerDialog() {
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    myCalendar.set(Calendar.MINUTE, selectedMinute);
                    updateTimeLabel();
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void updateTimeLabel() {
        String myFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editTextTime.setText(sdf.format(myCalendar.getTime()));
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private final DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, month);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
    };

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        editTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    private String getImageName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
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


