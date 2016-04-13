package com.truongpq.placesnearme.networks;

import com.truongpq.placesnearme.models.PlacesRespose;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by TruongPQ on 4/9/16.
 */
public interface PlacesApiInterface {
    @GET("/maps/api/place/nearbysearch/json")
    Call<PlacesRespose> requestPlaces(@Query("location") String location,
                                      @Query("radius") String radius,
                                      @Query("type") String types,
                                      @Query("key") String key);
}
