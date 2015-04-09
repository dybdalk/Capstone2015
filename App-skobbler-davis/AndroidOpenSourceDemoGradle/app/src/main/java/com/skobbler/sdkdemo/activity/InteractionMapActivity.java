package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.sdkdemo.R;


public class InteractionMapActivity extends Activity implements SKMapSurfaceListener {

    private SKMapSurfaceView mapView;

    private boolean mapSurfaceCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        SKMapViewHolder mapViewGroup = (SKMapViewHolder) findViewById(R.id.view_group_map);
        mapView = mapViewGroup.getMapSurfaceView();
        mapView.setMapSurfaceListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView = null;
    }

    @Override
    public void onActionPan() {
    }

    @Override
    public void onActionZoom() {
    }

    @Override
    public void onSurfaceCreated() {
        // a chess background is displayed until the map becomes available
        if (!mapSurfaceCreated) {
            mapSurfaceCreated = true;
            // hiding the chess background when map is available
            final RelativeLayout chessBackground = (RelativeLayout) findViewById(R.id.chess_table_background);
            chessBackground.setVisibility(View.GONE);

            mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));
        }
    }

    @Override
    public void onScreenOrientationChanged() {
    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion region) {
    }

    @Override
    public void onDoubleTap(SKScreenPoint point) {
    }

    @Override
    public void onSingleTap(SKScreenPoint point) {
    }

    @Override
    public void onRotateMap() {
    }

    @Override
    public void onLongPress(SKScreenPoint point) {
    }

    @Override
    public void onInternetConnectionNeeded() {
    }

    @Override
    public void onMapActionDown(SKScreenPoint point) {
    }

    @Override
    public void onMapActionUp(SKScreenPoint point) {
    }

    @Override
    public void onMapPOISelected(SKMapPOI mapPOI) {
    }

    @Override
    public void onAnnotationSelected(SKAnnotation annotation) {
    }

    @Override
    public void onCompassSelected() {
    }

    @Override
    public void onInternationalisationCalled(int result) {
    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI customPoi) {
    }

    @Override
    public void onPOIClusterSelected(SKPOICluster arg0) {
    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion arg0) {
    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion arg0) {
    }


    @Override
    public void onCurrentPositionSelected() {
    }

    @Override
    public void onObjectSelected(int arg0) {
    }



    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String messsage) {

    }

}
