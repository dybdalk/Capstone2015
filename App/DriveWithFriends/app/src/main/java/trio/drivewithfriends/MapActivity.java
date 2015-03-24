package trio.drivewithfriends;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

//
//
// TODO
// - Get marker placement information from json in our ConfigureRoute thread
// - move network access off UI thread, this is standard practice
//      http://developer.android.com/reference/android/os/AsyncTask.html
// - draw route on map and have user manipulate route,
//     http://stackoverflow.com/questions/15924834/decoding-polyline-with-new-google-maps-api
//     http://ddewaele.github.io/GoogleMapsV2WithActionBarSherlock/part5

public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener {
    private static String ANDROID_API_KEY;
    private static String BROWSER_API_KEY;

    private MapFragment mapFragment;
    private GoogleMap myMap;
    private LatLng mLatLng; // current location

//    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
//    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private String workAddress; // end address as string
    private String homeAddress; // start address as string
    private LatLng home; // GPS of home
    private LatLng work; // GPS of work
    private Marker startMarker;
    private Marker endMarker;

    private List<LatLng> routePoly; // GPS coordinates to draw.
    private String routeJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // set api keys
        Resources res = getResources();
        BROWSER_API_KEY = res.getString(R.string.browser_api_key);
        ANDROID_API_KEY = res.getString(R.string.android_api_key);

        // give proper permissions
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // get location
        Intent intent = getIntent();
        String lat = intent.getStringExtra(MainActivity.MY_LAT);
        String lng = intent.getStringExtra(MainActivity.MY_LNG);
        mLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        // get route addresses
        workAddress = intent.getStringExtra(MainActivity.END_LOCATION);
        homeAddress = intent.getStringExtra(MainActivity.START_LOCATION);

        // handle missing input from user.
        if (0 == workAddress.compareTo("")) {
            work = new LatLng(mLatLng.latitude + .001, mLatLng.longitude + .001);
        }
        if (0 == homeAddress.compareTo("")) {
            home = mLatLng;
        }

        // convert textual address to gps coordinates.
        // here we convert string addresses to GPS coordinates
        try {
            Geocoder geocoder = new Geocoder(this);
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
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        // place markers for home and work

        // create map
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    // called when map is ready for use
    public void onMapReady(GoogleMap map) {
        myMap = map;
        myMap.setOnMarkerDragListener(this);
        placeMarkers();
        new ConfigureRoute().execute(home, work);
    }

    // place markers for work and home, as well
    // as zoom camera appropriately
    public void placeMarkers() {
            // place markers for home and work
            startMarker = myMap.addMarker(new MarkerOptions()
                    .position(home)
                    .draggable(true)
                    .title("Home"));
            endMarker = myMap.addMarker(new MarkerOptions()
                    .position(work)
                    .draggable(true)
                    .title("Work"));

            // Here, i've dealt with a weird race-condition between the
            // map itself and what I think is the representation of the map
            // on our screen. To solve this, we just wait for the map to be
            // completely loaded onto phone.
            myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    // zoom to include both markers
                    LatLngBounds.Builder myBoundsBuilder = new LatLngBounds.Builder();
                    myBoundsBuilder = myBoundsBuilder.include(home);
                    myBoundsBuilder = myBoundsBuilder.include(work);
                    LatLngBounds myBounds = myBoundsBuilder.build();
                    myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myBounds, 0));
                    myMap.animateCamera(CameraUpdateFactory.zoomOut(), 1000, null);
                }
            });
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
        // updates route gps information when user drags
        // markers.
        LatLng location = marker.getPosition();
        if (0 == marker.getTitle().compareTo("Work")) {
            work = location;
        } else if (0 == marker.getTitle().compareTo("Home")) {
            home = location;
        }

        new ConfigureRoute().execute(home, work);
    }

    // We need to use a separate thread for the tasks of getting a route object from
    // google, as well as using geocoding to convert text to gps.
    //      http://developer.android.com/reference/android/os/AsyncTask.html
    private class ConfigureRoute extends AsyncTask<LatLng, Void, String> {
        protected String doInBackground(LatLng... endpoints) {


            String url = "https://maps.googleapis.com/maps/api/directions/json?";
            url = url + "origin=" + home.latitude + "," + home.longitude;
            url = url + "&destination=" + work.latitude + "," + work.longitude;
            url = url + "&key=" + BROWSER_API_KEY;
            // Get JSON route object as String
            routeJSON = getJSON(url, 100000);
            System.out.println(routeJSON);
//            routePoly = decodePoly(routeJSON);
            return routeJSON;
        }
        // Runs on UI thread after completion of doInBackground.
        // Result is the return value of doInBackground.
        protected void onPostExecute(String result) {
//            home = routePoly.get(0);
//            work = routePoly.get(routePoly.size()-1);
//            startMarker.setPosition(home);
//            endMarker.setPosition(work);
        }

        // Standard parsing of JSON url request into a String
        // http://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
        private String getJSON(String url, int timeout) {
            try {
                URL u = new URL(url);
                HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(timeout);
                c.setReadTimeout(timeout);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (MalformedURLException ex) {
                System.out.println("MalformedURLException in HTML request");
            } catch (IOException ex) {
                System.out.println("IOException in HTML request");
            }
            return null;
        }

        // Get polyline object from JSON object.
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
}
