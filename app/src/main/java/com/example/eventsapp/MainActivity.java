package com.example.eventsapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class MainActivity extends AppCompatActivity {
    private Uri selectedImageUri;

    private Calendar myCalendar;
    EditText editTextTitle, editTextLocation, editTextDate, editTextTime, editTextDescription, editTextCreator;
    TextView img;
    Button buttonSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.GRAY);

//         Initialize views from activity_main.xml
        img = findViewById(R.id.galleryImg);
//        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCreator = findViewById(R.id.editTextCreator);

//         Set onClickListener for adding the image
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
//                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .crop(16f, 9f)	//Crop image with 16:9 aspect ratio
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });


        Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                 Retrieve event details from EditText fields
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
                    // Create an Intent to start the events_page activity
                    Intent intent = new Intent(MainActivity.this, events_page.class);
                    // Pass the event details as extras
                    intent.putExtra("title", title);
                    intent.putExtra("location", location);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);
                    intent.putExtra("description", description);
                    intent.putExtra("creator", creator);
                    if (selectedImageUri != null) {
                        intent.putExtra("imageUri", selectedImageUri.toString());
                    }
                    // Start the events_page activity
                    startActivity(intent);
                }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // Set the selected image URI
                selectedImageUri = uri;
                // Set the image link text to display the selected image URI
                img.setText(selectedImageUri.toString());
            }
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        // Clear all input fields when MainActivity is started
        editTextTitle.setText("");
        editTextLocation.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        editTextDescription.setText("");
        editTextCreator.setText("");
    }
}




