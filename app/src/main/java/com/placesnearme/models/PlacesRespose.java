package com.placesnearme.models;

import java.util.List;

/**
 * Created by TruongPQ on 4/9/16.
 */
public class PlacesRespose {
    private List<Place> results;

    public PlacesRespose() {}

    public PlacesRespose(List<Place> results) {
        this.results = results;
    }

    public List<Place> getResults() {
        return results;
    }

    public void setResults(List<Place> results) {
        this.results = results;
    }
}
