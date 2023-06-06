package com.creativeapps.schoolbustracker.data.network.models;

import java.util.ArrayList;

public class Payload
{
    public double distance;
    public double time;
    public double lat;
    public double lng;
    public double speed;
    public String polyline;
    public ArrayList<Double> polylineValues;
    public Payload(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Payload(double lat, double lng, double speed) {
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
    }

    public Payload(double lat, double lng, double speed, String polyline) {
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
        this.polyline = polyline;
    }
}
