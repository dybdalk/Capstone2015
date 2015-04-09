package com.example.dshurbert.myapplication.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.dshurbert.myapplication.R;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;

public class MainActivity extends ActionBarActivity implements SKPrepareMapTextureListener{


    // This thread initializes the SKMaps library, should be moved to a splash activity.
    //final SKPrepareMapTextureThread prepThread = new SKPrepareMapTextureThread(this, "src/main/assets/maps", "SKMaps.zip", this);
    //private boolean lockOnMap = false; // Need to wait for Skobbler library to initialize before we can use it.
    public final static String API_KEY = "ccb3616178d7a2d8f1605074f80f684bcac549ef313b92611ca703c76f252462";

    // All these public final static values are keys that are
    // used when passing messages between activities. The idea
    // is that these messages are read from input by the user
    // and passed to the next activity.

    // EXTRA_MESSAGE is used to pass the username to RouteActivity
    public final static String START_LOCATION_STRING = "com.example.dshurbert.myapplication.START_LOCATION_STRING";
    public final static String END_LOCATION_STRING = "com.example.dshurbert.myapplication.END_LOCATION_STRING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String applicationPath = chooseStoragePath(this);

        // determine path where map resources should be copied on the device
        if (applicationPath != null) {
            mapResourcesDirPath = applicationPath + "/" + "SKMaps/";
        } else {
            // show a dialog and then finish
        }

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

    public void toMapButtonTouch(View v){
            // intents are used to start a new activity, in this
            // case we will start the map activity.
            Intent intent = new Intent(this, MapActivity.class);
            // Read input from user, who enters a start and end address.
            EditText editStartLocation = (EditText) findViewById(R.id.edit_start_location);
            EditText editEndLocation = (EditText) findViewById(R.id.edit_end_location);
            String startLocationString = editStartLocation.getText().toString();
            String endLocationString = editEndLocation.getText().toString();
            // Send strings to map activity.
            intent.putExtra(START_LOCATION_STRING, startLocationString);
            intent.putExtra(END_LOCATION_STRING, endLocationString);
            // Start the map activity.
            startActivity(intent);
    }

    @Override
    public void onMapTexturesPrepared(boolean b) {

    }
}
