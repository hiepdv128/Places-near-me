package com.truongpq.placesnearme;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.directions.route.Segment;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.truongpq.placesnearme.adapters.PlaceTypesAdapter;
import com.truongpq.placesnearme.adapters.PlacesAdapter;
import com.truongpq.placesnearme.adapters.SegmentsAdapter;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener, View.OnClickListener, ItemClickSupport.OnItemClickListener {

    private final String LOG_TAG = "MainActivity";

    private boolean doubleBackToExitPressedOnce = false;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng mLastLatLng;

    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabTypes;
    private ImageButton fabDirection;
    private EditText edtSearch;
    private BottomSheetBehavior bottomSheetType;
    private BottomSheetBehavior bottomSheetPlace;

    private BottomSheetBehavior bottomSheetDirections;
    private TextView tvDuretionDistance;

    private RecyclerView rvTypes;
    private PlaceTypesAdapter placeTypesAdapter;
    private List<PlaceType> placeTypes;
    private List<PlaceType> searchTypes;

    private RecyclerView rvPlaces;
    private PlacesAdapter placesAdapter;
    private List<Place> places;

    private RecyclerView rvDirections;
    private SegmentsAdapter segmentsAdapter;
    private List<Segment> segments;

    private int currentMakerId;

    private SharedPreferences sharedPreferences;


    private PlacesApiHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
        initDirections();
    }

    private void init() {
        fabMyLocation = (FloatingActionButton) findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(this);

        fabTypes = (FloatingActionButton) findViewById(R.id.fab_types);
        fabTypes.setOnClickListener(this);

        fabDirection = (ImageButton) findViewById(R.id.fab_direction);

        View btsType = findViewById(R.id.bottom_sheet_type);
        bottomSheetType = BottomSheetBehavior.from(btsType);

        View btsPlace = findViewById(R.id.bottom_sheet_place);
        bottomSheetPlace = BottomSheetBehavior.from(btsPlace);

        final View btsDirections = findViewById(R.id.bottom_sheet_directions);
        bottomSheetDirections = BottomSheetBehavior.from(btsDirections);

        edtSearch = (EditText) findViewById(R.id.edit_search);
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
                searchTypes.clear();
                searchTypes.addAll(filteredList);
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
        searchTypes = PlaceType.createPlaceTypes();
        placeTypesAdapter = new PlaceTypesAdapter(placeTypes);
        rvTypes.setAdapter(placeTypesAdapter);
        ItemClickSupport.addTo(rvTypes).setOnItemClickListener(this);
    }

    private void initPlaces() {
        rvPlaces = (RecyclerView) findViewById(R.id.rv_places);
        rvPlaces.setHasFixedSize(true);
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        places = new ArrayList<>();
        placesAdapter = new PlacesAdapter(places);
        rvPlaces.setAdapter(placesAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvPlaces.addItemDecoration(itemDecoration);
    }

    private void initDirections() {
        tvDuretionDistance = (TextView) findViewById(R.id.duretion_distance);
        rvDirections = (RecyclerView) findViewById(R.id.rvDirections);
        rvDirections.setHasFixedSize(true);
        rvDirections.setLayoutManager(new LinearLayoutManager(this));
        segments = new ArrayList<>();
        segmentsAdapter = new SegmentsAdapter(segments);
        rvDirections.setAdapter(segmentsAdapter);
    }

    private void moveToLocation(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    private void direction(LatLng end) {
        bottomSheetPlace.setPeekHeight(0);
        bottomSheetPlace.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetDirections.setPeekHeight(100);
        bottomSheetDirections.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDirections.setState(BottomSheetBehavior.STATE_COLLAPSED);

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .language("VI")
                .withListener(new RoutingListener() {
                    @Override
                    public void onRoutingFailure(RouteException e) {

                    }

                    @Override
                    public void onRoutingStart() {

                    }

                    @Override
                    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
                        List<Polyline> polylines = new ArrayList<>();
                        for (Route r : arrayList) {
                            tvDuretionDistance.setText(r.getDistanceText() + " (" + r.getDurationText() + ")");
                            segments.clear();
                            segments.addAll(r.getSegments());
                            segmentsAdapter.notifyDataSetChanged();

                            PolylineOptions polyOptions = new PolylineOptions();
                            polyOptions.color(Color.BLUE);
                            polyOptions.addAll(r.getPoints());
                            Polyline polyline = mMap.addPolyline(polyOptions);
                            polylines.add(polyline);
                        }
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(mLastLatLng).bearing(45).zoom(18).build();
                        mMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }

                    @Override
                    public void onRoutingCancelled() {

                    }
                })
                .waypoints(mLastLatLng, end)
                .build();
        routing.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_my_location:
                moveToLocation(mLastLatLng);
                break;
            case R.id.fab_types:
                bottomSheetType.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;

        }
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (fabDirection.getVisibility() == View.VISIBLE) {
            fabDirection.setVisibility(View.GONE);
            return;
        }

        if (bottomSheetType.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetType.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (bottomSheetPlace.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (bottomSheetPlace.getPeekHeight() == 100) {
            setTitle(R.string.app_name);
            mMap.clear();
            bottomSheetPlace.setPeekHeight(0);
            bottomSheetPlace.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (bottomSheetDirections.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDirections.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (bottomSheetDirections.getPeekHeight() == 100) {
            setTitle(R.string.app_name);
            mMap.clear();
            bottomSheetDirections.setPeekHeight(0);
            bottomSheetDirections.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDirections.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                fabDirection.setVisibility(View.VISIBLE);
                fabDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.fromResource(currentMakerId)));
                        direction(marker.getPosition());
                        fabDirection.setVisibility(View.GONE);
                    }
                });
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                fabDirection.setVisibility(View.GONE);
            }
        });
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

    // Click Item RecycleView
    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        String radius = sharedPreferences.getString("list_radius", "5000");
        currentMakerId = getResources().getIdentifier("marker_" + searchTypes.get(position).getId(), "drawable", getApplicationContext().getPackageName());
        helper.requestPlaces(searchTypes.get(position).getId(), mLastLatLng, Integer.parseInt(radius), new Callback<PlacesRespose>() {
            @Override
            public void onResponse(Call<PlacesRespose> call, Response<PlacesRespose> response) {
                final List<Place> responsePlaces = response.body().getResults();
                places.clear();
                places.addAll(responsePlaces);
                placesAdapter.notifyDataSetChanged();
                ItemClickSupport.addTo(rvPlaces).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        mMap.clear();
                        Place place = responsePlaces.get(position);
                        com.truongpq.placesnearme.models.Location location = place.getGeometry().getLocation();
                        LatLng latLng = new LatLng(location.getLat(), location.getLng());
                        direction(latLng);
                        mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()).snippet(place.getVicinity()).icon(BitmapDescriptorFactory.fromResource(currentMakerId)));
                        moveToLocation(latLng);
                    }
                });

                bottomSheetPlace.setPeekHeight(100);
                bottomSheetPlace.setState(BottomSheetBehavior.STATE_COLLAPSED);

                mMap.clear();
                for (Place place : responsePlaces) {
                    com.truongpq.placesnearme.models.Location location = place.getGeometry().getLocation();
                    LatLng latLng = new LatLng(location.getLat(), location.getLng());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()).snippet(place.getVicinity()).icon(BitmapDescriptorFactory.fromResource(currentMakerId)));
                }
            }

            @Override
            public void onFailure(Call<PlacesRespose> call, Throwable t) {

            }
        });
        setTitle(searchTypes.get(position).getName());
        bottomSheetType.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
