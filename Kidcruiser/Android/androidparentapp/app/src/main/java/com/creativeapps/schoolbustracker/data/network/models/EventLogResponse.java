package com.creativeapps.schoolbustracker.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class EventLogResponse {

    @Expose
    @SerializedName("eventLog")
    private List<EventLog> eventLog;


    public List<EventLog> getEventLog() {

        return eventLog;
    }

}
