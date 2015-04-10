package trio.drivewithfriends.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import trio.drivewithfriends.R;
import trio.drivewithfriends.route.Route;
import trio.drivewithfriends.route.Routing;
import trio.drivewithfriends.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/* This is the main map activity that the user sees to approve their route.
 *
 * Route requests and parsing is done in the trio.drivewithfriends.route package,
 * the code for which was taken from
 *
 * https://github.com/jd-alexander/Google-Directions-Android
 */

public class ApproveRouteActivity extends FragmentActivity implements RoutingListener
{
    // id's for API keys
    private static String ANDROID_API_KEY;
    private static String BROWSER_API_KEY;

    protected GoogleMap map;
    private String endAddress; // end address as string
    private String startAddress; // start address as string
    protected LatLng start;     // start gps coordinates
    protected LatLng end;       // end gps coordinates
    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("******************activity created at ExampleActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        // get route addresses
        Intent intent = getIntent();
        endAddress = intent.getStringExtra(MainActivity.END_LOCATION);
        startAddress = intent.getStringExtra(MainActivity.START_LOCATION);
        // handle missing input from user.
        if (0 == endAddress.compareTo("")) {
            endAddress = "Seattle, WA";
            //work = new LatLng(mLatLng.latitude + .001, mLatLng.longitude + .001);
        }
        if (0 == startAddress.compareTo("")) {
            startAddress = "Tacoma, WA";
            //home = mLatLng;
        }

        // convert textual address to GPS coordinates.
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> homeAddresses = geocoder.getFromLocationName(startAddress, 1);
            List<Address> workAddresses = geocoder.getFromLocationName(endAddress, 1);
            if (!homeAddresses.isEmpty()) {
                Address homeAd = homeAddresses.get(0);
                start = new LatLng(homeAd.getLatitude(), homeAd.getLongitude());
            }
            if (!workAddresses.isEmpty()) {
                Address workAd = workAddresses.get(0);
                end = new LatLng(workAd.getLatitude(), workAd.getLongitude());
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }

        // get map view
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = fm.getMap();

        // when map resource is ready, zoom camera appropriately
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // zoom to include both markers
                LatLngBounds.Builder myBoundsBuilder = new LatLngBounds.Builder();
                myBoundsBuilder = myBoundsBuilder.include(start);
                myBoundsBuilder = myBoundsBuilder.include(end);
                LatLngBounds myBounds = myBoundsBuilder.build();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(myBounds, 0));
                map.animateCamera(CameraUpdateFactory.zoomOut(), 1000, null);
            }
        });

        // create and start a routing thread, which takes
        // two gps coordinates as input and outputs a polyline as
        // well as a route object.
        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        System.out.println("******************execute routing task in ExampleActivity.java");
        routing.execute(start, end);
    }
    @Override
    public void onRoutingFailure() {
        System.out.println("******************routing failure in ExampleActivity.java");
        // The Routing request failed
    }
    @Override
    public void onRoutingStart() {
        System.out.println("******************routing start in ExampleActivity.java");
// The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        System.out.println("******************routing success in ExampleActivity.java");
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        map.addPolyline(polyoptions);
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        map.addMarker(options);
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        map.addMarker(options);
    }

    @Override
    public void onPause(){

    }

    @Override
    public void onResume(){

    }

    @Override
    public void onStart(){

    }
    @Override
    public void onStop(){

    }
}
