package trio.passengr.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

import trio.passengr.R;
import trio.passengr.route.Route;
import trio.passengr.route.Routing;
import trio.passengr.route.RoutingListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/* This is the main map activity that the user sees to approve their route.
 *
 * Route requests and parsing is done in the trio.passengr.route package,
 * the code for which was taken from
 *
 * https://github.com/jd-alexander/Google-Directions-Android
 */

public class MapActivity extends FragmentActivity implements
        RoutingListener,
        GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener
{
    // id's for API keys
    private static String ANDROID_API_KEY;
    private static String BROWSER_API_KEY;

    // START and END pass current location to MapActivity
    public final static String START_LAT = "trio.passengr.START_LAT";
    public final static String START_LNG = "trio.passengr.START_LNG";
    public final static String END_LAT = "trio.passengr.END_LAT";
    public final static String END_LNG = "trio.passengr.END_LNG";


    protected GoogleMap map;
    private String endAddress; // end address as string
    private String startAddress; // start address as string
    protected LatLng start;     // start gps coordinates
    protected LatLng end;       // end gps coordinates

    private ArrayList<LatLng> waypoints = new ArrayList<LatLng>();
    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("******************activity created at ExampleActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        // get map object from fragment view.
        MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        fm.getMapAsync(this);

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
        map.clear();
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        map.addPolyline(polyoptions);
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(route.getStartLocation());
        options.draggable(true);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        options.title("Start");
        map.addMarker(options);
        // End marker
        options = new MarkerOptions();
        options.position(route.getEndLocation());
        options.draggable(true);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        options.title("End");
        map.addMarker(options);

        // zoom to include both markers
        LatLngBounds.Builder myBoundsBuilder = new LatLngBounds.Builder();
        myBoundsBuilder = myBoundsBuilder.include(start);
        myBoundsBuilder = myBoundsBuilder.include(end);
        LatLngBounds myBounds = myBoundsBuilder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(myBounds, 200));
 //       map.moveCamera(CameraUpdateFactory.zoomOut());
    }

    @Override
    public void onPause(){
    super.onPause();
    }

    @Override
    public void onResume(){
    super.onResume();
    }

    @Override
    public void onStart(){
    super.onStart();
    }
    @Override
    public void onStop(){
    super.onStop();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        System.out.println("end drag");
        // updates route information when user drags
        // markers.
        LatLng location = marker.getPosition();
        if (0 == marker.getTitle().compareTo("Start")) {
            start = location;
        } else if (0 == marker.getTitle().compareTo("End")) {
            end = location;
        }

        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        System.out.println("******************execute routing task in ExampleActivity.java");
        routing.execute(start, end);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // when map resource is ready, set listeners for when map is loaded
        // (i.e. visible), and for when markers are dragged.
        map = googleMap;
        map.setOnMarkerDragListener(this);
        map.setOnMapLoadedCallback(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapLoaded() {
        // create and start a routing thread, which takes
        // two gps coordinates as input and outputs a polyline as
        // well as a route object.
        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        System.out.println("******************execute routing task in ExampleActivity.java");
        routing.execute(start, end);
    }

    @Override
    public void onMapClick(LatLng point) {

/*      waypoints.clear();
        waypoints.add(point);
        MarkerOptions options = new MarkerOptions();
        options.position(point);
        options.draggable(true);
        options.title("Waypoint");
        map.addMarker(options);
        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        System.out.println("******************execute routing task in ExampleActivity.java");
        routing.execute(start, waypoints.get(0), end);*/
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    // Start MapActivity and send route and location
    // information as messages.
    public void buttonConfirmRouteTouch(View view) {
        Intent intent = new Intent(this, AddTimeInfoActivity.class);

        String startLat = Double.toString(start.latitude);
        String startLng = Double.toString(start.longitude);
        String endLat = Double.toString(end.latitude);
        String endLng = Double.toString(end.longitude);

        intent.putExtra(START_LAT, startLat);
        intent.putExtra(START_LNG, startLng);
        intent.putExtra(END_LAT, endLat);
        intent.putExtra(END_LNG, endLng);

        startActivity(intent);
    }

}
