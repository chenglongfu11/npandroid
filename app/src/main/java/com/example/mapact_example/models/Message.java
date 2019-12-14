package com.example.mapact_example.models;

import com.google.android.gms.maps.model.LatLng;


import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

    private static long serialVersionUID = 1L;
    private String username;
    private String msg;
    private LatLng location;


    private Date date;

    public Message(String username, String msg, LatLng location, Date date) {
        this.username = username;
        this.msg = msg;
        this.location = location;
        this.date = date;
    }

    public Message() {

    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }



    //in case to print stg to log.i

    @Override
    public String toString() {
        return "Message{" +
                "username='" + username + '\'' +
                ", msg='" + msg + '\'' +
                ", location=" + location +
                ", date=" + date +
                '}';
    }
}
