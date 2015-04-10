package trio.drivewithfriends.route;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;

import trio.drivewithfriends.R;

/**
 * Async Task to access the Google Direction API and return the routing data.
 * Created by Furkan Tektas on 10/14/14.
 */
public class Routing extends AbstractRouting<LatLng> {

    public Routing(TravelMode mTravelMode) {
        super(mTravelMode);
        System.out.println("****************** travel mode set in Routing.java");
    }

    protected String constructURL(LatLng... points) {
        System.out.println("******************construct URL in Routing.java");
        if (points == null){
            System.out.println("******************null points in Routing.java");
        }
        LatLng start = points[0];
        LatLng dest = points[1];
        System.out.println("****************** start"+ points[0] + ", end" + points[1] + "in Routing.java");
        final StringBuffer mBuf = new StringBuffer(AbstractRouting.DIRECTIONS_API_URL);
        mBuf.append("origin=");
        mBuf.append(start.latitude);
        mBuf.append(',');
        mBuf.append(start.longitude);
        mBuf.append("&destination=");
        mBuf.append(dest.latitude);
        mBuf.append(',');
        mBuf.append(dest.longitude);
        mBuf.append("&sensor=true&mode=");
        mBuf.append(_mTravelMode.getValue());

        mBuf.append("&key=");
        mBuf.append("AIzaSyCuaxh6R7omz4zHJ-j57CxawPiyZy55VAw");

        System.out.println("****************** " + mBuf.toString());
        return mBuf.toString();
    }
}
