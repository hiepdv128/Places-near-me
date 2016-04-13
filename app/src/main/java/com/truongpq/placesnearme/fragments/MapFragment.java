package com.truongpq.placesnearme.fragments;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.truongpq.placesnearme.R;
import com.truongpq.placesnearme.adapters.PlacesAdapter;
import com.truongpq.placesnearme.models.Place;
import com.truongpq.placesnearme.models.PlaceTypes;
import com.truongpq.placesnearme.models.PlacesRespose;
import com.truongpq.placesnearme.networks.PlacesApiHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private PlacesApiHelper helper;


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mLastLatLng;

    FloatingActionMenu fabMenuSearch;
    private FloatingActionButton fabATM;
    private FloatingActionButton fabBank;

    private SlidingUpPanelLayout slidingLayout;

    private RecyclerView rvPlaces;
    private PlacesAdapter adapter;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment mapFragment = new MapFragment();
        return mapFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null, false);

        helper = new PlacesApiHelper(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        slidingLayout = (SlidingUpPanelLayout) getActivity().findViewById(R.id.sliding_layout);

        rvPlaces = (RecyclerView) getActivity().findViewById(R.id.rv_places);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvPlaces.setLayoutManager(layoutManager);

        fabMenuSearch = (FloatingActionMenu) getActivity().findViewById(R.id.fab_menu_search);

        createCustomAnimation();

        fabATM = (FloatingActionButton) getActivity().findViewById(R.id.fab_atm);
        fabBank = (FloatingActionButton) getActivity().findViewById(R.id.fab_bank);

        fabATM.setOnClickListener(clickListener);
        fabBank.setOnClickListener(clickListener);
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void createCustomAnimation() {
        final FloatingActionMenu menuSearch = (FloatingActionMenu) getActivity().findViewById(R.id.fab_menu_search);

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menuSearch.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menuSearch.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menuSearch.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menuSearch.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                menuSearch.getMenuIconView().setImageResource(menuSearch.isOpened()
                        ? R.drawable.ic_close : R.drawable.ic_search);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        menuSearch.setIconToggleAnimatorSet(set);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        //Move to current location
        mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mLastLatLng).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        drawCircle(mLastLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void drawCircle(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(2000)
                .fillColor(Color.argb(20, 50, 0, 255))
                .strokeColor(Color.argb(20, 50, 0, 255))
                .strokeWidth(2));
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_atm:
                    fabMenuSearch.close(true);
                    createCustomAnimation();
                    helper.requestPlaces(PlaceTypes.atm, mLastLatLng, 5000, placesCallback);
                    break;
                case R.id.fab_bank:
                    fabMenuSearch.close(true);
                    createCustomAnimation();
                    helper.requestPlaces(PlaceTypes.bank, mLastLatLng, 5000, placesCallback);
                    break;
            }
        }
    };

    private Callback<PlacesRespose> placesCallback = new Callback<PlacesRespose>() {

        @Override
        public void onResponse(Call<PlacesRespose> call, Response<PlacesRespose> response) {
            List<Place> places = response.body().getResults();

            adapter = new PlacesAdapter(places);
            rvPlaces.setAdapter(adapter);

            for (Place r : places) {
                com.truongpq.placesnearme.models.Location location = r.getGeometry().getLocation();
                LatLng latLng = new LatLng(location.getLat(), location.getLng());
                String name = r.getName();
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            }

        }

        @Override
        public void onFailure(Call<PlacesRespose> call, Throwable t) {
            t.printStackTrace();
        }
    };
}



