package com.example.mapact_example;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;

public class UserLocation {
    private LatLng place;
    private Time time;
    private String userid;

    public UserLocation(LatLng place, Time time, String userid) {

        this.place = place;
        this.time = time;
        this.userid = userid;
    }

    public UserLocation(){

    }

    public LatLng getPlace() {
        return place;
    }

    public void setPlace(LatLng place) {
        this.place = place;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "place=" + place +
                ", time=" + time +
                ", userid='" + userid + '\'' +
                '}';
    }
}
