package com.truongpq.placesnearme.models;

import com.truongpq.placesnearme.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TruongPQ on 4/25/16.
 */
public class PlaceType {
    private String id;
    private String name;
    private int icon;

    public PlaceType(String id, String name, int icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public static List<PlaceType> createPlaceTypes() {
        List<PlaceType> placeTypes = new ArrayList<>();
        placeTypes.add(new PlaceType("atm", "ATM", R.drawable.ic_atm));
        placeTypes.add(new PlaceType("bank", "Ngân hàng", R.drawable.ic_bank));
        placeTypes.add(new PlaceType("bus", "Xe buýt", R.drawable.ic_bus));
        placeTypes.add(new PlaceType("coffee", "Cafe", R.drawable.ic_coffee));
        placeTypes.add(new PlaceType("hospital", "Bệnh viện", R.drawable.ic_hospital));
        placeTypes.add(new PlaceType("restaurant", "Ăn uống", R.drawable.ic_restaurant));
        return placeTypes;
    }
}
