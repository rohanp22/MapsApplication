package com.example.mapsapplication.ui.dashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mapsapplication.MapsActivity;
import com.example.mapsapplication.Model.Nearby;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.Others.URLs;
import com.example.mapsapplication.Others.VolleySingleton;
import com.example.mapsapplication.ProfileFragment;
import com.example.mapsapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationSource.OnLocationChangedListener {

    private MapsViewModel dashboardViewModel;
    private Handler handler;
    View root;
    ArrayList<Marker> markers;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(MapsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.root = root;
        //final TextView textView = root.findViewById(R.id.text_dashboard);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        markers = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(map != null) {
                    map.clear();
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserFirebase user = snapshot.getValue(UserFirebase.class);
                    if(user.getLng() != null && user.getLng() != null && !user.getId().equals(SharedPrefManager.getInstance(getContext()).getUser().getId()+"")) {
                        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(user.getLat()), Double.parseDouble(user.getLng()))));
                        marker.setTag(user.getId());
                        markers.add(marker);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(map != null) {
                    map.clear();
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserFirebase user = snapshot.getValue(UserFirebase.class);
                    if(user.getLng() != null && user.getLng() != null && !user.getId().equals(SharedPrefManager.getInstance(getContext()).getUser().getId()+"")) {
                        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(user.getLat()), Double.parseDouble(user.getLng()))));
                        marker.setTag(user.getId());
                        markers.add(marker);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap map;
    private ArrayList<Nearby> nearbies;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2) {

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371;

        Log.d("Distance", (c * r) + "");

        return (c * r);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    int i = 0;

    private void addMarkers(){
        //map.clear();
        nearbies = new ArrayList<>();
        Log.d("Updated", "Locations updated");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://projectzero5.tk/findNearbyUsers.php?id="+SharedPrefManager.getInstance(getContext()).getUser().getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            nearbies.clear();
                            JSONObject obj = new JSONObject(response);
                            Log.d("nearby", response+"");
                            JSONArray heroArray = obj.getJSONArray("nearby");
                            for (int i = 0; i < heroArray.length(); i++) {
                                JSONObject heroObject = heroArray.getJSONObject(i);
                                Nearby c = new Nearby(
                                        heroObject.getString("email"),
                                        heroObject.getString("name"),
                                        Double.parseDouble(heroObject.getString("lat")),
                                        Double.parseDouble(heroObject.getString("lng")));
                                nearbies.add(c);

                                //ListView l = (ListView) findViewById(R.id.transactionList);
                            }
                            for(Nearby nearby: nearbies){
                                Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(nearby.getLat(), nearby.getLng()))
                                        .title("Marker"));
                                markers.add(marker);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        //addMarkers();
        this.map = map;
        getLocationPermission();
        AlertDialog alertDialog;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            alertDialog = new AlertDialog.Builder(getContext())
                    .setMessage("GPS not enabled")
                    .setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(getContext(), "Turn on location", Toast.LENGTH_LONG).show();
                }
            });
        }
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onStop() {
        Log.e("", "OnStop Result...");
        super.onStop();
        //onRestart();
    }

    @Override
    public void onPause() {
        Log.e("", "OnPause Result...");
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("", "OnActivity Result...");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {
            switch (requestCode) {
                case 1:
                    updateLocationUI();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        nearbies = new ArrayList<>();
        Log.d("Resume", "OnResume");
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (gps_enabled && network_enabled) {
            updateLocationUI();
            getDeviceLocation();
        }
    }

    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                final GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d("Marker id", marker.getTag().toString());
                        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ProfileFragment()).addToBackStack(null).commit();
                        return false;
                    }
                };
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.setOnMarkerClickListener(markerClickListener);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Log.d("Latitude", lastKnownLocation.getLatitude() + "");
                                Log.d("longitude", lastKnownLocation.getLongitude() + "");
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                Log.d("Myid", SharedPrefManager.getInstance(getContext()).getUser().getId()+"");
                                DatabaseReference myRef = database.getReference("Users").child(SharedPrefManager.getInstance(getContext()).getUser().getId()+"");
                                myRef.child("lat").setValue(lastKnownLocation.getLatitude()+"");
                                myRef.child("lng").setValue(lastKnownLocation.getLongitude()+"");
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        Log.d("UpdateLocation", "Update location");

        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(final Location location) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.d("Myid", SharedPrefManager.getInstance(getContext()).getUser().getId()+"");
        DatabaseReference myRef = database.getReference("Users").child(SharedPrefManager.getInstance(getContext()).getUser().getId()+"");
        myRef.child("lat").setValue(location.getLatitude()+"");
        myRef.child("lng").setValue(location.getLongitude()+"");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_LOCATION_UPDATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("lat", location.getLatitude() + "");
                params.put("lng", location.getLongitude() + "");
                params.put("id", SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                return params;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}