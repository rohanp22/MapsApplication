package com.example.mapsapplication.ui.maps;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mapsapplication.MapsActivity;
import com.example.mapsapplication.Model.Nearby;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.ProfileFragment;
import com.example.mapsapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationSource.OnLocationChangedListener {

    private MapsViewModel dashboardViewModel;
    View root;
    ArrayList<Marker> markers;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap map;
    private ArrayList<Nearby> nearbies;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LayoutInflater layoutInflater;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.layoutInflater = inflater;
        dashboardViewModel =
                ViewModelProviders.of(this).get(MapsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.root = root;
        //final TextView textView = root.findViewById(R.id.text_dashboard);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

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

    GoogleApiClient googleApiClient;

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(root.getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                Log.d("Onactivity", "Onactivity");
                                startIntentSenderForResult(status.getResolution().getIntentSender(), 199, null, 0, 0, 0, null);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
        addMarkers();
    }

    Marker markerCustom;
    String customName;
    MarkerOptions customMarkerOptions;

    @Override
    public void onMapReady(final GoogleMap map) {
        //addMarkers();

        this.map = map;

        this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder alert = new AlertDialog.Builder(root.getContext());
                final EditText edittext = new EditText(root.getContext());
                alert.setView(edittext);
                alert.setTitle("Add marker?")
                        .setMessage("Do you want to mark this location: ")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Editable YouEditTextValue = edittext.getText();
                                customName = YouEditTextValue.toString();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("mymarker").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                                myRef.child("lat").setValue(latLng.latitude + "");
                                myRef.child("lng").setValue(latLng.longitude + "");
                                myRef.child("name").setValue(customName);
                                if(markerCustom != null) {
                                    markerCustom.remove();
                                }
                                 customMarkerOptions = new MarkerOptions()
                                        .position(new LatLng(latLng.latitude, latLng.longitude))
                                        .draggable(false)
                                        .flat(false)
                                        .icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(customName)));
                                markerCustom = map.addMarker(customMarkerOptions);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });

        getLocationPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap createStoreMarker(String name) {
        assert getParentFragment() != null;
        View markerLayout = layoutInflater.inflate(R.layout.custom_marker_layout, null);

        ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView markerRating = (TextView) markerLayout.findViewById(R.id.marker_text);
        markerImage.setImageResource(R.drawable.ic_map_marker);
        markerRating.setText(name);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    public void addMarkers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        markers = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (map != null) {
                    map.clear();
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference ref = db.getReference("mymarker").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String uid = SharedPrefManager.getInstance(root.getContext()).getUser().toString();
                            if(snapshot.exists()) {
                                Log.d("Snapshot", snapshot.toString());
                                String lat = snapshot.child("lat").getValue(String.class);
                                Log.d("Lat", Double.parseDouble(lat) + "");
                                String lng = snapshot.child("lng").getValue(String.class);
                                customMarkerOptions = new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)))
                                        .draggable(false)
                                        .flat(false)
                                        .icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(snapshot.child("name").getValue(String.class))));
                                markerCustom = map.addMarker(customMarkerOptions);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserFirebase user = snapshot.getValue(UserFirebase.class);
                        if (user.getLng() != null && user.getLng() != null && !user.getId().equals(SharedPrefManager.getInstance(getContext()).getUser().getId() + "")) {
                            MarkerOptions options = new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(user.getLat()), Double.parseDouble(user.getLng())))
                                    .draggable(false)
                                    .flat(false)
                                    .icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(user.getUsername())));
                            Marker tempMarker = map.addMarker(options);
                            tempMarker.setTag(user.getId());
                            markers.add(tempMarker);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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

    boolean f = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("", "OnActivity Result...");
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("OnResult", requestCode + "");
        if (resultCode == 1) {
            switch (requestCode) {
                case 1:
                    getDeviceLocation();
                    updateLocationUI();
                    break;
            }
        }

        switch (requestCode) {
            case 199:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Log.d("Given permission", "yes");
                        locationPermissionGranted = true;

                        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                if(!f) {
                                    f = true;
                                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
                                    map.moveCamera(center);
                                    map.animateCamera(zoom);
                                }

                            }
                        });

                        markerClickListener = new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                if(marker.getTag() != null) {
                                    Log.d("Marker id", marker.getTag().toString());
                                    Bundle bundle = new Bundle();
                                    bundle.putString("id", marker.getTag().toString());
                                    ProfileFragment profileFragment = new ProfileFragment();
                                    profileFragment.setArguments(bundle);
                                    getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, profileFragment).addToBackStack(null).commit();
                                    map.setOnMarkerClickListener(markerClickListener);
                                    return false;
                                } else {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(root.getContext());
                                    final EditText edittext = new EditText(root.getContext());
                                    alert.setView(edittext);
                                    alert.setTitle("Edit marker?")
                                            .setMessage("Enter the name of location: ")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Editable YouEditTextValue = edittext.getText();
                                                    customName = YouEditTextValue.toString();
                                                    customMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(customName)));
                                                    markerCustom.remove();
                                                    markerCustom = map.addMarker(customMarkerOptions);

                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                    DatabaseReference myRef = database.getReference("mymarker").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                                                    myRef.child("name").setValue(customName);
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .show();
                                }
                                return false;
                            }
                        };
                        map.setOnMarkerClickListener(markerClickListener);
                        if(lastKnownLocation != null) {
                            CameraUpdate center =
                                    CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(),
                                            -lastKnownLocation.getLongitude()));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

                            map.moveCamera(center);
                            map.animateCamera(zoom);
                        }
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(root.getContext(), "Location not enabled", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
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
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        Log.d("GPS, NETWORK", gps_enabled + " " + network_enabled);

        if (gps_enabled && network_enabled) {
            getDeviceLocation();
            updateLocationUI();
        }
    }

    GoogleMap.OnMarkerClickListener markerClickListener;

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Log.d("getDeviceLocation", "Executing");
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                markerClickListener = new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(marker.getTag() != null) {
                            Log.d("Marker id", marker.getTag().toString());
                            Bundle bundle = new Bundle();
                            bundle.putString("id", marker.getTag().toString());
                            ProfileFragment profileFragment = new ProfileFragment();
                            profileFragment.setArguments(bundle);
                            getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, profileFragment).addToBackStack(null).commit();
                            map.setOnMarkerClickListener(markerClickListener);
                            return false;
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(root.getContext());
                            final EditText edittext = new EditText(root.getContext());
                            alert.setView(edittext);
                            alert.setTitle("Edit marker?")
                                    .setMessage("Enter the name of location: ")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        public void onClick(DialogInterface dialog, int which) {
                                            Editable YouEditTextValue = edittext.getText();
                                            customName = YouEditTextValue.toString();
                                            customMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(customName)));
                                            markerCustom.remove();
                                            markerCustom = map.addMarker(customMarkerOptions);

                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference myRef = database.getReference("mymarker").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                                            myRef.child("name").setValue(customName);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .show();
                        }
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
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 11));
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))      // Sets the center of the map to Mountain View
                                        .zoom(17)                   // Sets the zoom
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("Users").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
                                myRef.child("lat").setValue(lastKnownLocation.getLatitude() + "");
                                myRef.child("lng").setValue(lastKnownLocation.getLongitude() + "");
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
                locationResult.addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Cancelled", "listener");
                    }
                });
                addMarkers();
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
            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                enableLoc();
            } else {
                getDeviceLocation();
                updateLocationUI();
            }
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
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ex) {
                    }

                    try {
                        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception ex) {
                    }

                    if (!gps_enabled && !network_enabled) {
                        enableLoc();
                    }
                }
            }
            break;

        }
    }

    private void updateLocationUI() {
        if (map != null) {
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
    }

    @Override
    public void onLocationChanged(final Location location) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.d("Myid", SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
        DatabaseReference myRef = database.getReference("Users").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
        myRef.child("lat").setValue(location.getLatitude() + "");
        myRef.child("lng").setValue(location.getLongitude() + "");
    }
}