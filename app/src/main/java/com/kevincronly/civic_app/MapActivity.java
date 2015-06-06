package com.kevincronly.civic_app;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener
{
    private static final String TAG = "MapActivity";
    private boolean tripStarted;
    private GoogleMap mMap;
    private LatLng latLng;
    private Location mStartLocation;
    private Location mEndLocation;
    private LocationManager locationManager;
    private String provider;
    private Criteria criteria;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private LatLng start;
    private LatLng end;
    private double distance;
    private static int claimID = 1;
    private static int employeeID = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("trackr");

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

        buildGoogleApiClient();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        Log.i(TAG, "OnCreate");
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap map)
    {
        mMap = map;

        mMap.setMyLocationEnabled(true);
        Log.i(TAG, "map ready");
    }

    private void startTrip()
    {
        mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title("Start"));
        mStartLocation = mCurrentLocation;
        /*
        Snackbar
                .make(view, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .show();

        Log.i(TAG, "Snacked");*/

        Log.i(TAG, "trip started");
    }

    private void endTrip()
    {
        mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title("Finish"));
        Log.i(TAG, "trip ended");
        mEndLocation = mCurrentLocation;

        start = new LatLng(mStartLocation.getLatitude(), mStartLocation.getLongitude());
        end = new LatLng(mEndLocation.getLatitude(), mEndLocation.getLongitude());

        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        routing.execute(start, end);


    }

    @Override
    public void onConnected(Bundle bundle)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.i(TAG, "location updates requested");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        Log.i(TAG, "api connected");
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "api suspended");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = location;
        Log.i(TAG, "location changed");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.i(TAG, "connection failed");
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG, "Google api client built");
        mGoogleApiClient.connect();
    }

/*
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location)
        {

        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void onProviderEnabled(String provider)
        {

        }

        public void onProviderDisabled(String provider) {}
    };

    // Creating a criteria object to retrieve provider
        criteria = new Criteria();

        // Getting the name of the best provider
        provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        mStartLocation = locationManager.getLastKnownLocation(provider);

        // Getting latitude of the current location
        double latitude = mStartLocation.getLatitude();

        // Getting longitude of the current location
        double longitude = mStartLocation.getLongitude();

        // Creating a LatLng object for the current location
        latLng = new LatLng(latitude, longitude);

    */

    @Override
    public void onRoutingFailure()
    {
        // The Routing request failed
        Log.i(TAG, "routing failed");
    }

    @Override
    public void onRoutingStart()
    {
        // The Routing Request starts
        Log.i(TAG, "routing started");
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(10);
        polyOptions.addAll(mPolyOptions.getPoints());
        mMap.addPolyline(polyOptions);

        distance = route.getLength();
        Toast.makeText(getApplicationContext(), "You traveled " + route.getDistanceText(), Toast.LENGTH_LONG).show();

        /*
        // Start marker

        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        mMap.addMarker(options);

        // End marker

        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pushpin));
        mMap.addMarker(options);*/

        new RequestTask().execute("http://398755d3.ngrok.com/");
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://398755d3.ngrok.com/tracking/new");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("employeeID", "" + employeeID));
                nameValuePairs.add(new BasicNameValuePair("claim_id", "" + claimID++));
                nameValuePairs.add(new BasicNameValuePair("Distance", "" + distance));
                nameValuePairs.add(new BasicNameValuePair("Start", mStartLocation.getLatitude() + ", " + mStartLocation.getLongitude()));
                nameValuePairs.add(new BasicNameValuePair("Stop", mEndLocation.getLatitude() + ", " + mEndLocation.getLongitude()));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httpPost);

            } catch (ClientProtocolException e)
            {
                Log.i(TAG, "Client protocol exception");
            } catch (IOException e)
            {
                Log.i(TAG, "IO exception");
            }
            return "";
        }
    }

}
