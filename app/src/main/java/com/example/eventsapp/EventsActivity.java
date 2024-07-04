package com.example.eventsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventsActivity extends AppCompatActivity implements EventAdapter.OnEventDeleteListener{
    private ArrayList<Event> eventList;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Retrieve the event list from the Intent
        eventList = (ArrayList<Event>) getIntent().getSerializableExtra("eventsList");

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(EventsActivity.this, eventList, this);
        recyclerView.setAdapter(eventAdapter);

        // Button to navigate back to MainActivity
        Button addMoreEventsButton = findViewById(R.id.buttonAddMoreEvents);
        addMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(EventsActivity.this, MainActivity.class);
                intent.putExtra("eventsList", eventList);
                startActivity(intent);
            }
        });
    }

    @Override
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
                        Toast.makeText(EventsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }

    @Override
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
                        Toast.makeText(EventsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }
}
