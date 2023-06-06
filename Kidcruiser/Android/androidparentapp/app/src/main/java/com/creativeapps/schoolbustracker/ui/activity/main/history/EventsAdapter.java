package com.creativeapps.schoolbustracker.ui.activity.main.history;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.network.models.EventLog;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.LogsViewHolder> {
    //EventLog list
    private List<EventLog> mLogList;

    //region Constructor
    public EventsAdapter(List<EventLog> logList) {
        this.mLogList = new ArrayList<>();
        this.mLogList.addAll(logList);

    }
    //endregion

    @Override
    public LogsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate rows of the RecycleView

        View view = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        view = inflater.from(parent.getContext())
                .inflate(R.layout.driver_log_row_item, parent, false);
        return new LogsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
        EventLog eventLog = mLogList.get(position);

        Integer eventType = eventLog.getEvent_type();
        String event_title = "";
        switch (eventType)
        {
            case 1:
                event_title = "arrived at";
                break;

            case 2:
                event_title = "left";
                break;
            case 3:
                event_title = MainActivity.getContext().getString(R.string.checked_in_string);
                break;
            case 4:
                event_title = MainActivity.getContext().getString(R.string.checked_out_string);
                break;
        }

        if(eventType ==3 || eventType==4)
        {
            if(eventLog.getChild() != null)
                event_title = eventLog.getChild().getchildName() + " " + event_title;
            else
                event_title = "Your child " + event_title;
        }
        else
        {
            Integer place = eventLog.getEvent_place();
            if(eventType == 1)
            {
                if(place == 1)
                {
                    event_title = MainActivity.getContext().getString(R.string.bus_arrived_at_school);
                }
                else if(place == 2)
                {
                    event_title = MainActivity.getContext().getString(R.string.bus_arrived_at_home);
                }
            }
            else if(eventType == 2)
            {
                if(place == 1)
                {
                    event_title = MainActivity.getContext().getString(R.string.bus_left_school);
                }
                else if(place == 2)
                {
                    event_title = MainActivity.getContext().getString(R.string.bus_left_home);
                }
            }
        }
        holder.event_title.setText(event_title);
        holder.event_time.setText(eventLog.getEvent_time());
    }

    @Override
    public int getItemCount() {
        //return the size of the list to be displayed
        if(mLogList != null){
            return mLogList.size();
        }
        return 0;
    }

    public void updateData(List<EventLog> eventLogs) {
        this.mLogList.clear();
        this.mLogList.addAll(eventLogs);
        //signal data change
        notifyDataSetChanged();
    }



    /*Define a class that instantiate gui elements of rows of the displayed list.
    Object from LogsViewHolder class is used in onBindViewHolder function*/
    public class LogsViewHolder extends RecyclerView.ViewHolder {
        //event_title and event_time are used here
        public TextView event_title, event_time;

        public LogsViewHolder(View view) {
            super(view);

            event_title = view.findViewById(R.id.event_title);
            event_time = view.findViewById(R.id.event_time);

        }
    }
}