package com.example.eventsapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventsapp.Models.EventsModel;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private final Context context;
    private final ArrayList<EventsModel> eventList;

    // 1. Naya Interface declare kiya (Bridge)
    private final OnEventLongClickListener longClickListener;

    // Interface Definition
    public interface OnEventLongClickListener {
        void onLongClick(EventsModel event);
    }

    // 2. Constructor update kiya (Listener pass karne ke liye)
    public EventsAdapter(Context context, ArrayList<EventsModel> eventList, OnEventLongClickListener longClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.longClickListener = longClickListener; // Variable set kiya
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventsModel event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
        holder.date.setText(event.getDate());
        holder.time.setText(event.getTime());

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(event.getImageUri())
                    .placeholder(R.drawable.loading_spinner)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.loading_spinner);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            // Sabse Important: Data pass karna (Taaki detail screen ko pata chale kaunsa event khula hai)
            intent.putExtra("selected_event", event); // Ensure Event implements Serializable
            context.startActivity(intent);
        });

        // 3. --- LONG PRESS LOGIC HERE ---
        holder.itemView.setOnLongClickListener(v -> {
            // Activity ko signal bhejo ki "Is event par long press hua hai"
            longClickListener.onLongClick(event);
            return true; // true matlab event handle ho gaya
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, time, description, creator;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvEventTitle);
            location = itemView.findViewById(R.id.tvEventLocation);
            date = itemView.findViewById(R.id.tvEventDate);
            time = itemView.findViewById(R.id.tvEventTime);
            image = itemView.findViewById(R.id.ivEventImage);
        }
    }
}
