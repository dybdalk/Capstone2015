package trio.drivewithfriends.route;

/**
 * Async Task to access the Google Direction API and return the routing data
 * which is then parsed and converting to a route overlay using some classes created by Hesham Saeed.
 * @author Joel Dean
 * @author Furkan Tektas
 * Requires an instance of the map activity and the application's current context for the progress dialog.
 *
 *
 * This is the abstract class for the thread Routing, which runs off the UI thread in order
 * to request and get route information from google https service. A nearly identical thread will be needed
 * in order to access either the database directly, or through the REST api.
 */

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public abstract class AbstractRouting<T> extends AsyncTask<T, Void, trio.drivewithfriends.route.Route> {
    protected ArrayList<trio.drivewithfriends.route.RoutingListener> _aListeners;
    protected TravelMode _mTravelMode;

    protected static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    public enum TravelMode {
        BIKING("biking"),
        DRIVING("driving"),
        WALKING("walking"),
        TRANSIT("transit");

        protected String _sValue;

        private TravelMode(String sValue) {
            this._sValue = sValue;
        }

        protected String getValue() {
            return _sValue;
        }
    }


    // constructor wih travel mode.
    public AbstractRouting(TravelMode mTravelMode) {
        this._aListeners = new ArrayList<RoutingListener>();
        this._mTravelMode = mTravelMode;
    }

    // Register a listener to the thread. Listeners are notified upon completion
    // or failure of tasks by the thread.
    public void registerListener(RoutingListener mListener) {
        _aListeners.add(mListener);
        System.out.println("****************** Listener added in AbstractRouting.java");
    }

    // Notify all listeners that the route-request process is starting
    protected void dispatchOnStart() {
        for (RoutingListener mListener : _aListeners) {
            System.out.println("****************** Signal onRoutingStart from AbstractRouting.java");
            mListener.onRoutingStart();
        }
    }

    // Notify all listeners that the route request process failed.
    protected void dispatchOnFailure() {
        for (RoutingListener mListener : _aListeners) {
            System.out.println("****************** Signal onRoutingFailure from AbstractRouting.jar");
            mListener.onRoutingFailure();
        }
    }


    protected void dispatchOnSuccess(PolylineOptions mOptions, Route route) {
        for (RoutingListener mListener : _aListeners) {
            System.out.println("****************** Signal onRoutingFailure from AbstractRouting.jar");
            mListener.onRoutingSuccess(mOptions, route);
        }
    }

    /**
     * Performs the call to the google maps API to acquire routing data and
     * deserializes it to a format the map can display.
     *
     * @param aPoints
     * @return
     */
    @Override
    protected Route doInBackground(T... aPoints) {
        System.out.println("******************doInBackground routing task");

        for (T mPoint : aPoints) {
            if (mPoint == null) {
                System.out.println("******************aPoints found as null in AbstractRouting.java");
                return null;
                }
        }

        System.out.println("******************GoogleParser.parse() in AbstractRouting.java");
        return new GoogleParser(constructURL(aPoints)).parse();
    }

    protected abstract String constructURL(T... points);

    @Override
    protected void onPreExecute() {
        dispatchOnStart();
    }

    @Override
    protected void onPostExecute(Route result) {      
        if (result == null) {
            dispatchOnFailure();
            System.out.println("****************** Route result null in AbstractRouting.java");
        } else {
            PolylineOptions mOptions = new PolylineOptions();

            for (LatLng point : result.getPoints()) {
                if (point==null){
                    System.out.print("*null*");
                }
                mOptions.add(point);
            }

            dispatchOnSuccess(mOptions, result);
        }
    }//end onPostExecute method
}
