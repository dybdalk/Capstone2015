package trio.drivewithfriends.activity;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import trio.drivewithfriends.R;

// Route Activity gets phone's location every 10 seconds or so
// and prints GPS coordinates to screen.
//
// ROUTE ACTIVITY IS NOT IN USE AND WILL BE DELETED ON NEXT PUSH
public class RouteActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.
        OnConnectionFailedListener, LocationListener {

    // TextViews for displaying location
    private TextView[] mTextLocation = new TextView[3];

    // Google api client for using their location service
    private GoogleApiClient mGoogleApiClient;

    // Location Info
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    // cycles through which text fields to update
    private int locationCounter=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get message from intent (ie from parent activity)
        setContentView(R.layout.activity_route);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // update text view object with username
        TextView textView = (TextView) findViewById(R.id.username);
        textView.setText(message);

        // track textViews from layout
        mTextLocation[0] = (TextView) findViewById(R.id.locationOne);
        mTextLocation[1] = (TextView) findViewById(R.id.locationTwo);
        mTextLocation[2] = (TextView) findViewById(R.id.locationThree);

        // Location Stuff
        buildGoogleApiClient();
        createLocationRequest();
    }

    //Build the google api client, needed for access to location information
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // listeners for when connection is made to
                // (google server?) via the GoogleApiClient
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    protected void createLocationRequest() {
        // Creates automated requests for location
        // to the google api client.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        // on activity start
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        // on activity resume
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
        startLocationUpdates();
        }
    }

    @Override
    // when user leaves activity
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    // when activity is terminated.
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);
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
    // runs when activity first connects to google client
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        updateUI();

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    // starts the automated requests for location
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // ends the automated requests for location
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, RouteActivity.this);
    }

    // updates UI and cycles through textViews
    private void updateUI() {
        if (mLastLocation != null) {
            mTextLocation[locationCounter].setText("Time: " + DateFormat.getTimeInstance().format(new Date()) + " | Lat: " + String.valueOf(mLastLocation.getLatitude()) + " | " + "Lng: " + String.valueOf(mLastLocation.getLongitude()));
            locationCounter = (locationCounter+1) % 3;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI();
    }
}