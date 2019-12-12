package com.example.mapact_example.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String username;
    private String messageToshare;
    private Message message;
    private int iconImage;

    public ClusterMarker() {

    }

    public ClusterMarker(LatLng position, String username, String messageToshare, Message message, int iconImage) {
        this.position = position;
        this.username = username;
        this.messageToshare = messageToshare;
        this.message = message;
        this.iconImage = iconImage;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageToshare() {
        return messageToshare;
    }

    public void setMessageToshare(String messageToshare) {
        this.messageToshare = messageToshare;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getIconImage() {
        return iconImage;
    }

    public void setIconImage(int iconImage) {
        this.iconImage = iconImage;
    }

    //ToString method is only for debugging
    @Override
    public String toString() {
        return "ClusterMarker{" +
                "position=" + position +
                ", username='" + username + '\'' +
                ", messageToshare='" + messageToshare + '\'' +
                ", message=" + message +
                ", iconImage=" + iconImage +
                '}';
    }
}
