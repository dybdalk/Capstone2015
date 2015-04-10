package spaldingdrivewithfriends.trio.sdrivewithfriends;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;


public class MapActivity extends ActionBarActivity {

    /**
     * Surface view for displaying the map
     */
    private SKMapSurfaceView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SKMapViewHolder mapHolder = (SKMapViewHolder)
                findViewById(R.id.view_group_map);
        mapView = mapHolder.getMapSurfaceView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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

    //We need this code to work:
    /*public class MapActivity extends Activity implements SKMapSurfaceListener {

        *//**
         * Surface view for displaying the map
         *//*
        private SKMapSurfaceView mapView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            SKMapViewHolder mapHolder = (SKMapViewHolder)
                    findViewById(R.id.view_group_map);
            mapView = mapHolder.getMapSurfaceView();

        }

        @Override
        protected void onPause() {
            super.onPause();
            mapView.onPause();
        }

        @Override
        protected void onResume() {
            super.onResume();
            mapView.onResume();
        }

        ...

        @Override
        public void onSurfaceCreated() {
            //insert your code here
        }

    }
   }*/
}
