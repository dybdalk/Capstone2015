package trio.drivewithfriends;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.
        OnConnectionFailedListener {

    // All these public final static values are keys that are
    // used when passing messages between activities. The idea
    // is that these messages are read from input by the user
    // and passed to the next activity.

    // EXTRA_MESSAGE is used to pass the username to RouteActivity
    public final static String EXTRA_MESSAGE = "trio.drivewithfriends.MESSAGE";

    // MY_LAT and MY_LNG pass current location to MapActivity
    public final static String MY_LAT = "trio.drivewithfriends.MY_LAT";
    public final static String MY_LNG = "trio.drivewithfriends.MY_LNG";

    // Pass start and end location to MapActivity. The user inputs these locations
    // as addresses and we use Geocoding to get GPS coordinates. Addresses can be
    // literal address or just names, i,e, "Tacoma, Wa".
    public final static String START_LOCATION = "trio.drivewithfriends.START_LOCATION";
    public final static String END_LOCATION = "trio.drivewithfriends.END_LOCATION";

    // Current Location
    private Location mLastLocation;
    // Api client for getting location information.
    private GoogleApiClient mGoogleApiClient;
    // Current Location in LatLng
    private LatLng my_location;
    // textView of current location
    private TextView mTextLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextLocation = (TextView) findViewById(R.id.location);
        buildGoogleApiClient();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // create google api client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
/*
<<<<<<< HEAD
    // when google api client connects
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        my_location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        updateUI();
=======
*/
    // when Google api client connects
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        my_location = new LatLng(0.0,0.0);
        //updateUI();
    }

    // What we do with buttons:
    //
    // Start RouteActivity and send username as message
    public void buttonToRouteTouch(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    // Start MapActivity and send route and location
    // information as messages.
    public void buttonToMapTouch(View view) {
        Intent intent = new Intent(this, MapActivity.class);

        EditText editHome = (EditText) findViewById(R.id.edit_start_location);
        EditText editWork = (EditText) findViewById(R.id.edit_end_location);
        String homeAddress = editHome.getText().toString();
        String workAddress = editWork.getText().toString();

        String myLng = Double.toString(my_location.longitude);
        String myLat = Double.toString(my_location.latitude);
        intent.putExtra(MY_LAT, myLat);
        intent.putExtra(MY_LNG, myLng);
        intent.putExtra(START_LOCATION, homeAddress);
        intent.putExtra(END_LOCATION, workAddress);

        startActivity(intent);
    }

    // when connection to google fails.
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (mLastLocation != null) {
            mTextLocation.setText("Time: " + DateFormat.getTimeInstance().format(new Date()) + " | Lat: " + String.valueOf(mLastLocation.getLatitude()) + " | " + "Lng: " + String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
        }
    }

    @Override
    // when user leaves activity?
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


}
