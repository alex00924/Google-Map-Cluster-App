package com.rol.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import net.sharewire.googlemapsclustering.ClusterItem;

class MyClusterItem implements ClusterItem {

    public LatLng location;

    public String task_id;
    public String organiser_id;
    public String organiser_name;
    public String organiser_image;
    public String title;
    public String task_image;
    public String str_location;
    public String created_date;
    public String start_date;
    public String start_time;
    public String no_of_helper;
    public String distance;
    public String is_fav;
    public String total_comments;
    public String description;
    public String total_helper;
    public String total_confirmed_helper;
    public String task_start_date_time;
    public double latitude;
    public double longitude;
    public String is_friend;

    public String is_added;
    public String is_applied;
    public String is_recommanded;
    public String task_status;
    public String is_accepted;

    MyClusterItem(@NonNull LatLng location) {
        this.location = location;
    }

    @Override
    public double getLatitude() {
        return location.latitude;
    }

    @Override
    public double getLongitude() {
        return location.longitude;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    public void setLatLong()
    {
        this.location = new LatLng(latitude, longitude);
    }

    public int      nIndx = 0;
}
