package com.example.eventsapp;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class events_page extends AppCompatActivity {

    ImageView imageView;
    TextView textViewTitle;
    TextView textViewLocation;
    TextView textViewDate;
    TextView textViewTime;
    TextView textViewDescription;
    TextView textViewCreator;
    FloatingActionButton addMoreEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_page);

//         Retrieve the event details from the intent
        imageView = findViewById(R.id.imageView);
        textViewTitle = findViewById(R.id.title);
        textViewLocation = findViewById(R.id.location);
        textViewDate = findViewById(R.id.date);
        textViewTime = findViewById(R.id.time);
        textViewDescription = findViewById(R.id.description);
        textViewCreator = findViewById(R.id.creator);

//         Retrieve the event details from the intent
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String location = intent.getStringExtra("location");
            String date = intent.getStringExtra("date");
            String time = intent.getStringExtra("time");
            String description = intent.getStringExtra("description");
            String creator = intent.getStringExtra("creator");

//             Update the UI with the retrieved event details
            textViewTitle.setText(title);
            textViewLocation.setText(location);
            textViewDate.setText(date);
            textViewTime.setText(time);
            textViewDescription.setText(description);
            textViewCreator.setText(creator);

//            ArrayList<SlideModel> imageList = new ArrayList<>();

            if (intent.hasExtra("imageUri")) {
//                 Retrieve the image URI and load it into the ImageView
                Uri imageUri = Uri.parse(intent.getStringExtra("imageUri"));
                imageView.setImageURI(imageUri);
                /*SlideModel slideModel = new SlideModel(imageUri.toString(), ScaleTypes.FIT);
                imageList.add(slideModel);
                imageList.add(slideModel);
                imageList.add(slideModel);*/
            } else {
//                 Log an error or display a message indicating that the intent is null
                Log.e("events_page", "Intent is null");
                Toast.makeText(this, "Intent is null", Toast.LENGTH_SHORT).show();
            }

             // Create image list
 /*imageList.add(new SlideModel("String Url" or R.drawable))
 imageList.add(new SlideModel("String Url" or R.drawable, "title")) You can add title

            imageList.add(new SlideModel(, ScaleTypes.FIT));
            imageList.add(new SlideModel(R., ScaleTypes.FIT));
            imageList.add(new SlideModel(, ScaleTypes.FIT));

            ImageSlider imageSlider = findViewById(R.id.imageView);
            imageSlider.setImageList(imageList);*/

        }
    }
}
