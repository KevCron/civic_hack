package com.kevincronly.civic_app;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static final String TAG = "MapActivity";
    private boolean tripStarted;
    private GoogleMap mMap;
    private LatLng latLng;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = (Button) findViewById(R.id.trip_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tripStarted) {
                    tripStarted = true;
                    btn.setText("End Trip");
                    startTrip();
                } else {
                    tripStarted = false;
                    btn.setText("Start Trip");
                    endTrip();
                }
            }
        });

        MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        Log.i(TAG, "OnCreate");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap map)
    {
        mMap = map;

        mMap.setMyLocationEnabled(true);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        mCurrentLocation = locationManager.getLastKnownLocation(provider);

        if(mCurrentLocation != null)
        {
            // Getting latitude of the current location
            double latitude = mCurrentLocation.getLatitude();

            // Getting longitude of the current location
            double longitude = mCurrentLocation.getLongitude();

            // Creating a LatLng object for the current location
            latLng = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }
    }

    private void startTrip()
    {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));

        /*
        Snackbar
                .make(view, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .show();

        Log.i(TAG, "Snacked");*/

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i(TAG, "trip started");
    }

    private void endTrip()
    {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Finish"));
        Log.i(TAG, "trip started");
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mCurrentLocation = mLastLocation;
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double oldLat = mCurrentLocation.getLatitude();
        double oldLon = mCurrentLocation.getLongitude();

        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(oldLat, oldLon))
                .add(new LatLng(lat, lon));

        mMap.addPolyline(rectOptions);
        Log.i(TAG, "drawing path");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    /*
    // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
     */
}
