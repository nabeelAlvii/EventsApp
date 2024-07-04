package com.example.eventsapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private Context context;
    private ArrayList<Event> eventList;
    private OnEventDeleteListener onEventDeleteListener;          // #

    public EventAdapter(Context context, ArrayList<Event> eventList, OnEventDeleteListener onEventDeleteListener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventDeleteListener = onEventDeleteListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
        holder.date.setText(event.getDate());
        holder.time.setText(event.getTime());
        holder.description.setText(event.getDescription());
        holder.creator.setText(event.getCreator());

        if (event.getImageUri() != null) {
            holder.image.setImageURI(Uri.parse(event.getImageUri()));
        } else {
            holder.image.setImageResource(R.drawable.events);
        }

        // #
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onEventDeleteListener.onEventLongClicked(event);
                return true;
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEventDeleteListener.onEventDeleteClicked(event);
            }
        });
        // #
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, time, description, creator;
        ImageView image;
        ImageButton deleteButton;     // #

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            description = itemView.findViewById(R.id.description);
            creator = itemView.findViewById(R.id.creator);
            image = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);   //#
        }
    }

    public interface OnEventDeleteListener {
        void onEventLongClicked(Event event);
        void onEventDeleteClicked(Event event);
    }
}
