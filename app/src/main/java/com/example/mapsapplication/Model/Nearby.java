package com.example.mapsapplication.Model;

public class Nearby {

    private String email, name;
    private double lat, lng;

    public Nearby(String email, String name, double lat, double lng) {
        this.email = email;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
