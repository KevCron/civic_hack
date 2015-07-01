package com.kevincronly.civic_app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private Location mStartLocation;
    private Location mEndLocation;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private LatLng start;
    private LatLng end;
    private double distance;
    private static int claimID = 1;
    private static int employeeID = 7;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mAdapter;
    private boolean tripping;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> coordinates = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
    private ArrayList<AbstractMap.SimpleEntry<String, String>> info = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
    private int count;
    private double metersTraveled;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        userName = getIntent().getStringExtra("userName");
        Log.i(TAG, "user name is " + userName);

        Log.i(TAG, "OnCreate");
    }

    // Methods associated with creating the activity
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // setting up nav drawer
    private void setupDrawer()
    {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void addDrawerItems() {
        final String[] osArray = {"Profile", "Trips", "Data", "Help", "Sign Out"};
        mAdapter = new DrawerAdapter(this, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = osArray[position];
                if (temp.equals("Sign Out")) {
                    finish();
                }
            }
        });
    }

    // starting and stopping the trip
    public void tripClick()
    {
        final Button btn = (Button) findViewById(R.id.trip_button);
        if (mCurrentLocation != null) {
            if (!tripping) {
                tripping = true;
                btn.setText("End Trip");
                startTrip();
            } else {
                tripping = false;
                btn.setText("Start Trip");
                endTrip();
            }
        }
    }

    private void startTrip()
    {
        mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title("Start"));
        mStartLocation = mCurrentLocation;
        count = 0;
        coordinates.add(new AbstractMap.SimpleEntry<String, String>("coordinate", "" + mStartLocation.getLatitude() + ", " + mStartLocation.getLongitude()));
        Log.i(TAG, "trip started");
    }

    private void endTrip()
    {
        mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title("Finish"));
        Log.i(TAG, "trip ended");
        mEndLocation = mCurrentLocation;
        coordinates.add(new AbstractMap.SimpleEntry<String, String>("coordinate", "" + mEndLocation.getLatitude() + ", " + mEndLocation.getLongitude()));
        count = 0;

        distance = distance * 0.000621371;
        Log.i(TAG, "miles " + distance);

        Intent i = new Intent(this, ClaimActivity.class);
        startActivityForResult(i, 0);

        //new PostClaimTask().execute("http://23e25190.ngrok.com/claim/new");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            Log.i(TAG, "claim title is " + data.getStringExtra("claimTitle"));
            Log.i(TAG, "nature of business is " + data.getStringExtra("natureOfBusiness"));
            Log.i(TAG, "license plate number is " + data.getStringExtra("licensePlateNumber"));
            Log.i(TAG, "start mileage is " + data.getStringExtra("startMileage"));
            Log.i(TAG, "end mileage is " + data.getStringExtra("endMileage"));
            Log.i(TAG, "notes are " + data.getStringExtra("notes"));

            coordinates.add(new AbstractMap.SimpleEntry<String, String>("claim_title", data.getStringExtra("claimTitle")));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("nature_of_business", data.getStringExtra("natureOfBusiness")));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("license_plate_number", data.getStringExtra("licensePlateNumber")));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("start_mileage", data.getStringExtra("startMileage")));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("stop_mileage", data.getStringExtra("endMileage")));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("miles_traveled", "" + distance));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("user_name", userName));
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("IPS_location", data.getStringExtra("notes")));

            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Claim submitted", Snackbar.LENGTH_LONG).show();

            //new PostClaimTask().execute("http://23e25190.ngrok.com/claim/new");
        }
    }

    // Map methods and associated logic for location
    @Override
    public void onMapReady(GoogleMap map)
    {
        mMap = map;

        mMap.setMyLocationEnabled(true);

        Log.i(TAG, "map ready1");
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.i(TAG, "location updates requested");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        Button tripBtn = (Button) findViewById(R.id.trip_button);
        tripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripClick();
            }
        });
        Log.i(TAG, "api connected1");
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "api suspended");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (tripping)
        {
            distance = distance + location.distanceTo(mCurrentLocation);
            Log.i(TAG, "distance traveled " + distance);
            coordinates.add(new AbstractMap.SimpleEntry<String, String>("coordinate", "" + location.getLatitude() + ", " + location.getLongitude()));
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                            .add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                            .add(new LatLng(location.getLatitude(), location.getLongitude()))
            );
            Log.i(TAG, "new location " + location.getLatitude() + ", " + location.getLongitude());
        }

        mCurrentLocation = location;

        if (location.distanceTo(mCurrentLocation) > 100)
        {
            // do update stuff
            mCurrentLocation = location;
        }
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

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String callURL(String myurl) throws IOException
    {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        String response = "";

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.i(TAG, "connection opened");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(coordinates));
            writer.flush();
            writer.close();
            os.close();

            // Starts the query
            conn.connect();
            Log.i(TAG, "connected");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response = "FAILED";
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Connection Error", Snackbar.LENGTH_LONG).show();
            }

            // Convert the InputStream into a string
            return response;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    private String getQuery(ArrayList<AbstractMap.SimpleEntry<String, String>> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey().toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class PostClaimTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return callURL(params[0]);
            }
            catch (IOException e)
            {
                return "URL may be invalid";
            }
        }

        @Override
        protected void onPostExecute(String responseString)
        {
            Log.i(TAG, "respone is " + responseString);
            onSuccess();
            return;
        }
    }

    private void onSuccess()
    {
        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Claim submitted", Snackbar.LENGTH_LONG).show();
    }
}
