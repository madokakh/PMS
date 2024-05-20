package com.example.scheduleproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<Object> items;
    private Context context;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.items = organizeItems(events);
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            String date = (String) items.get(position);
            ((HeaderViewHolder) holder).textDate.setText(date);
        } else {
            Event event = (Event) items.get(position);
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getDefault());

            String time = timeFormat.format(start.getValue());
            String description = event.getDescription();
            String date = getDate(event);
            String title = event.getSummary();
            String location = event.getLocation();

            ((EventViewHolder) holder).textTime.setText(time);
           // setAM_PMText(time, ((EventViewHolder) holder).textTime);
            ((EventViewHolder) holder).textTime.setText(time);
            ((EventViewHolder) holder).textDescription.setText(event.getSummary());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("description", description);
                intent.putExtra("title", title);
                intent.putExtra("location", location);
                intent.putExtra("eventId", event.getId());
                context.startActivity(intent);
            });
        }


    }
    private void setAM_PMText(String time,TextView tvAM_PM) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(time);
            int hours = date.getHours();
            if (hours < 12) {
                tvAM_PM.setText(R.string.morning);
            } else if(hours < 17) {
                tvAM_PM.setText(R.string.afternoon);
            }else if(hours < 7) {
                tvAM_PM.setText(R.string.evening);
            }else{
                tvAM_PM.setText(R.string.night);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private String getDate(Event event) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
            start = event.getStart().getDate();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(start.getValue());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private List<Object> organizeItems(List<Event> events) {
        List<Object> organizedItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());

        String currentHeader = "";
        for (Event event : events) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            String date = dateFormat.format(start.getValue());
            if (!date.equals(currentHeader)) {
                currentHeader = date;
                organizedItems.add(currentHeader);
            }
            organizedItems.add(event);
        }
        return organizedItems;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView textTime;
        public TextView textDescription;
        public TextView textAMPM;

        public EventViewHolder(View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.text_time);
            textDescription = itemView.findViewById(R.id.text_description);
           // textAMPM = itemView.findViewById(R.id.tvAmPm);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView textDate;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
        }
    }
}
