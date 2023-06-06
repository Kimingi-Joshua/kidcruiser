package com.creativeapps.schoolbustracker.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;



public class EventLog implements Parcelable {
    @Expose
    @SerializedName("case_id")
    private Integer event_type;


    @Expose
    @SerializedName("event_place")
    private Integer event_place;

    @Expose
    @SerializedName("updated_at")
    private String event_time;


    @Expose
    @SerializedName("child")
    private Child child;


    protected EventLog(Parcel in) {
        event_type = in.readInt();
        event_place = in.readInt();
        event_time = in.readString();
        child = in.readParcelable(Child.class.getClassLoader());
    }

    public static final Creator<EventLog> CREATOR = new Creator<EventLog>() {
        @Override
        public EventLog createFromParcel(Parcel in) {

            return new EventLog(in);
        }

        @Override
        public EventLog[] newArray(int size) {
            return new EventLog[size];
        }
    };

    public EventLog(Integer event_type, Integer event_place, String event_time, Child child) {
        this.event_type = event_type;
        this.event_place = event_place;
        this.event_time = event_time;
        this.child = child;

    }

    public Integer getEvent_type() {
        return event_type;
    }

    public void setEvent_type(Integer event_type) {
        this.event_type = event_type;
    }

    public Integer getEvent_place() {
        return event_place;
    }

    public void setEvent_place(Integer event_place) {
        this.event_place = event_place;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(event_type);
        parcel.writeInt(event_place);
        parcel.writeString(event_time);
    }

    public Child getChild() {
        return child;
    }
}