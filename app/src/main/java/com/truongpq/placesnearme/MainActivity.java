package com.truongpq.placesnearme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.truongpq.placesnearme.adapters.PlaceTypesAdapter;
import com.truongpq.placesnearme.adapters.PlacesAdapter;
import com.truongpq.placesnearme.models.ItemClickSupport;
import com.truongpq.placesnearme.models.Place;
import com.truongpq.placesnearme.models.PlaceType;
import com.truongpq.placesnearme.models.PlacesRespose;
import com.truongpq.placesnearme.networks.PlacesApiHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener, View.OnClickListener, Callback<PlacesRespose>, ItemClickSupport.OnItemClickListener {

    private final String LOG_TAG = "MainActivity";

    private boolean doubleBackToExitPressedOnce = false;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng mLastLatLng;

    private ImageButton btnMyLocation;
    private EditText edtSearch;
    private BottomSheetBehavior bottomSheetType;
    private BottomSheetBehavior bottomSheetPlace;

    private RecyclerView rvTypes;
    private PlaceTypesAdapter placeTypesAdapter;
    private List<PlaceType> placeTypes;
    private RecyclerView rvPlaces;
    private PlacesAdapter placesAdapter;

    private PlacesApiHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        helper = new PlacesApiHelper(this);

        init();
        initPlaceTypes();
        initPlaces();
    }

    private void init() {
        btnMyLocation = (ImageButton) findViewById(R.id.btn_my_location);
        btnMyLocation.setOnClickListener(this);

        View btsType = findViewById(R.id.bottom_sheet_type);
        bottomSheetType = BottomSheetBehavior.from(btsType);

        View btsPlace = findViewById(R.id.bottom_sheet_place);
        bottomSheetPlace = BottomSheetBehavior.from(btsPlace);

        edtSearch = (EditText) findViewById(R.id.edit_search);
        edtSearch.setOnClickListener(this);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s = s.toString().toLowerCase();
                final List<PlaceType> filteredList = new ArrayList<>();
                for (PlaceType t : placeTypes) {
                    String name = t.getName().toLowerCase();
                    if (name.contains(s)) {
                        filteredList.add(t);
                    }
                }
                placeTypesAdapter = new PlaceTypesAdapter(filteredList);
                rvTypes.setAdapter(placeTypesAdapter);
                placeTypesAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initPlaceTypes() {
        rvTypes = (RecyclerView) findViewById(R.id.rv_types);
        rvTypes.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvTypes.setLayoutManager(layoutManager);

        placeTypes = PlaceType.createPlaceTypes();
        placeTypesAdapter = new PlaceTypesAdapter(placeTypes);
        rvTypes.setAdapter(placeTypesAdapter);

        ItemClickSupport.addTo(rvTypes).setOnItemClickListener(this);
    }

    private void initPlaces() {
        rvPlaces = (RecyclerView) findViewById(R.id.rv_places);
        rvPlaces.setHasFixedSize(true);
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
    }

    private void moveToLocation(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_my_location:
                moveToLocation(mLastLatLng);
                break;
            case R.id.edit_search:
                bottomSheetType.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
//        if (bottomSheetType.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//            bottomSheetType.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            return;
//        }
//
//        if (bottomSheetPlace.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetPlace.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//            mMap.clear();
//            bottomSheetPlace.setPeekHeight(0);
//            bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            return;
//        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10); // Update location every second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        moveToLocation(mLastLatLng);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    // CallBack Retrofit
    @Override
    public void onResponse(Call<PlacesRespose> call, Response<PlacesRespose> response) {
        final List<Place> places = response.body().getResults();
        placesAdapter = new PlacesAdapter(places);
        rvPlaces.setAdapter(placesAdapter);
        ItemClickSupport.addTo(rvPlaces).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                mMap.clear();
                Place place = places.get(position);
                com.truongpq.placesnearme.models.Location location = place.getGeometry().getLocation();
                LatLng latLng = new LatLng(location.getLat(), location.getLng());
                mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()).snippet(place.getVicinity()));
                moveToLocation(latLng);
                bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });

        bottomSheetPlace.setPeekHeight(100);
        bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mMap.clear();
        for (Place place : places) {
            com.truongpq.placesnearme.models.Location location = place.getGeometry().getLocation();
            LatLng latLng = new LatLng(location.getLat(), location.getLng());
            mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()).snippet(place.getVicinity()));
        }
    }

    // CallBack Retrofit
    @Override
    public void onFailure(Call<PlacesRespose> call, Throwable t) {
        Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    // Click Item RecycleView
    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        helper.requestPlaces(placeTypes.get(position).getId(), mLastLatLng, 5000, this);
        bottomSheetType.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
