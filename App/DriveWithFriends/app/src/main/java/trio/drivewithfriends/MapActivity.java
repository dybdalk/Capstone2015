package trio.drivewithfriends;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;


public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener {

    private MapFragment mapFragment;
    private GoogleMap myMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mLatLng;
    private String workAddress;
    private String homeAddress;
    private LatLng home;
    private LatLng work;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // get location
        Intent intent = getIntent();
        String lat = intent.getStringExtra(MainActivity.MY_LAT);
        String lng = intent.getStringExtra(MainActivity.MY_LNG);

        workAddress = intent.getStringExtra(MainActivity.END_LOCATION);
        homeAddress = intent.getStringExtra(MainActivity.START_LOCATION);

        mLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        if (0 == workAddress.compareTo("")) {
            work = new LatLng(mLatLng.latitude + .001, mLatLng.longitude + .001);
        }

        if (0 == homeAddress.compareTo("")) {
            home = mLatLng;
        }

        // create map
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        myMap=map;
//        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 8));
        placeMarkers();
    }

    public void placeMarkers(){
        try {
            Geocoder geocoder = new Geocoder(this);
            System.out.println("Home: " + homeAddress );
            System.out.println("Work: " + workAddress);
            List<Address> homeAddresses = geocoder.getFromLocationName(homeAddress, 1);
            List<Address> workAddresses = geocoder.getFromLocationName(workAddress, 1);
            if (!homeAddresses.isEmpty()) {
                Address homeAd = homeAddresses.get(0);
                home = new LatLng(homeAd.getLatitude(), homeAd.getLongitude());
            }
            if (!workAddresses.isEmpty()) {
                Address workAd = workAddresses.get(0);
                work = new LatLng(workAd.getLatitude(), workAd.getLongitude());
            }
            myMap.addMarker(new MarkerOptions()
                    .position(home)
                    .draggable(true)
                    .title("Home"));

            myMap.addMarker(new MarkerOptions()
                    .position(work)
                    .draggable(true)
                    .title("Work"));

            // Set the camera to the greatest possible zoom level that includes
            // home and work
            // myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 4));

            myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds.Builder myBoundsBuilder = new LatLngBounds.Builder();
                    myBoundsBuilder = myBoundsBuilder.include(home); myBoundsBuilder = myBoundsBuilder.include(work);
                    LatLngBounds myBounds = myBoundsBuilder.build();
                    myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myBounds,0));
                    myMap.animateCamera(CameraUpdateFactory.zoomOut(), 1000, null);
                }
            });
        }
        catch(Exception e) {
            System.out.println("caought!");
            System.out.println(e.getMessage());
  //          LatLng ll = new LatLng(0,0);
  //          myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 4));

        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng location = marker.getPosition();
        if (0 == marker.getTitle().compareTo("Work")) {
            work = location;
        }
        else if (0 == marker.getTitle().compareTo("Home")) {
            home = location;
        }
    }
}