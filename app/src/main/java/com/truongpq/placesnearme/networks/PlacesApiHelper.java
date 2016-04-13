package com.truongpq.placesnearme.networks;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.truongpq.placesnearme.R;
import com.truongpq.placesnearme.models.PlacesRespose;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by TruongPQ on 4/9/16.
 */
public class PlacesApiHelper {
    private Context context;

    public PlacesApiHelper(Context context) {
        this.context = context;
    }

    public void requestPlaces(String type, LatLng latLng, int radius, Callback<PlacesRespose> callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.places_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlacesApiInterface placesApiInterface = retrofit.create(PlacesApiInterface.class);

        Call<PlacesRespose> call = placesApiInterface.requestPlaces(
                String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude),
                String.valueOf(radius),
                type,
                context.getString(R.string.google_maps_key));
        call.enqueue(callback);
    }
}
