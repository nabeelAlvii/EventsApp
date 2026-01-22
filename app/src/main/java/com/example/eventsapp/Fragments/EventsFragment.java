package com.example.eventsapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventsapp.EventsAdapter;
import com.example.eventsapp.Models.EventsModel;
import com.example.eventsapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddEvent;
    private ArrayList<EventsModel> eventList;
    private EventsAdapter adapter;

    public EventsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        /*recyclerView = view.findViewById(R.id.recyclerView);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);

        eventList = EventRepository.getEvents(); // 👈 abhi empty rahega

        adapter = new EventAdapter(getContext(), eventList, (EventAdapter.OnEventDeleteListener) getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAddEvent.setOnClickListener(v -> {
            ((MainActivity)getActivity()).openAddEventFragment();
        });*/

        return view;
    }
}
