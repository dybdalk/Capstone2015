package com.skobbler.sdkdemo.activity;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.skobbler.ngx.SKCategories.SKPOICategory;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationView;
import com.skobbler.ngx.map.SKBoundingBox;
import com.skobbler.ngx.map.SKCalloutView;
import com.skobbler.ngx.map.SKCircle;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings.SKMapFollowerMode;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolygon;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.map.realreach.SKRealReachListener;
import com.skobbler.ngx.map.realreach.SKRealReachSettings;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.navigation.SKNavigationListener;
import com.skobbler.ngx.navigation.SKNavigationManager;
import com.skobbler.ngx.navigation.SKNavigationSettings;
import com.skobbler.ngx.navigation.SKNavigationSettings.SKNavigationType;
import com.skobbler.ngx.navigation.SKNavigationState;
import com.skobbler.ngx.poitracker.SKDetectedPOI;
import com.skobbler.ngx.poitracker.SKPOITrackerListener;
import com.skobbler.ngx.poitracker.SKPOITrackerManager;
import com.skobbler.ngx.poitracker.SKTrackablePOI;
import com.skobbler.ngx.poitracker.SKTrackablePOIType;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.reversegeocode.SKReverseGeocoderManager;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.routing.SKRouteSettings.SKRouteMode;
import com.skobbler.ngx.routing.SKViaPoint;
import com.skobbler.ngx.sdktools.download.SKToolsDownloadItem;
import com.skobbler.ngx.sdktools.download.SKToolsDownloadListener;
import com.skobbler.ngx.sdktools.download.SKToolsDownloadManager;
import com.skobbler.ngx.sdktools.navigationui.SKToolsAdvicePlayer;
import com.skobbler.ngx.sdktools.navigationui.SKToolsNavigationConfiguration;
import com.skobbler.ngx.sdktools.navigationui.SKToolsNavigationListener;
import com.skobbler.ngx.sdktools.navigationui.SKToolsNavigationManager;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.DemoApplication;
import com.skobbler.sdkdemo.database.MapDownloadResource;
import com.skobbler.sdkdemo.util.DemoUtils;
import com.skobbler.sdkdemo.util.PreferenceTypes;

/**
 * Activity displaying the map
 */

public class MapActivity extends Activity implements SKMapSurfaceListener, SKRouteListener, SKNavigationListener,
        SKRealReachListener, SKPOITrackerListener, SKCurrentPositionListener, SensorEventListener,
        SKMapUpdateListener, SKToolsNavigationListener {

    private static final byte GREEN_PIN_ICON_ID = 0;

    private static final byte RED_PIN_ICON_ID = 1;

    public static final byte VIA_POINT_ICON_ID = 4;

    private static final String TAG = "MapActivity";

    public static final int TRACKS = 1;

    private enum MapOption {
        MAP_DISPLAY, MAP_OVERLAYS, ALTERNATIVE_ROUTES, MAP_STYLES, REAL_REACH, TRACKS, ANNOTATIONS,
        ROUTING_AND_NAVIGATION, POI_TRACKING, HEAT_MAP, MAP_INTERACTION, NAVI_UI
    }

    private enum MapAdvices {
        TEXT_TO_SPEECH, AUDIO_FILES
    }


    public static SKPOICategory[] heatMapCategories;

    /**
     * Current option selected
     */
    private MapOption currentMapOption = MapOption.MAP_DISPLAY;

    /**
     * Application context object
     */
    private DemoApplication app;

    /**
     * Surface view for displaying the map
     */
    private SKMapSurfaceView mapView;

    /**
     * Options menu
     */
    private View menu;

    /**
     * View for selecting alternative routes
     */
    private View altRoutesView;

    /**
     * View for selecting the map style
     */
    private LinearLayout mapStylesView;

    /**
     * View for real reach time profile
     */
    private RelativeLayout realReachTimeLayout;

    /**
     * View for real reach energy profile
     */
    private RelativeLayout realReachEnergyLayout;

    /**
     * Buttons for selecting alternative routes
     */
    private Button[] altRoutesButtons;

    /**
     * Bottom button
     */
    private Button bottomButton;

    /**
     * The current position button
     */
    private Button positionMeButton;

    /**
     * Custom view for adding an annotation
     */
    private RelativeLayout customView;

    /**
     * The heading button
     */
    private Button headingButton;

    /**
     * The map popup view
     */
    private SKCalloutView mapPopup;

    /**
     * Custom callout view title
     */
    private TextView popupTitleView;

    /**
     * Custom callout view description
     */
    private TextView popupDescriptionView;

    /**
     * Ids for alternative routes
     */
    private List<Integer> routeIds = new ArrayList<Integer>();

    /**
     * Tells if a navigation is ongoing
     */
    private boolean navigationInProgress;

    /**
     * Tells if a navigation is ongoing
     */
    private boolean skToolsNavigationInProgress;

    /**
     * Tells if a route calculation is ongoing
     */
    private boolean skToolsRouteCalculated;

    /**
     * POIs to be detected on route
     */
    private Map<Integer, SKTrackablePOI> trackablePOIs;

    /**
     * Trackable POIs that are currently rendered on the map
     */
    private Map<Integer, SKTrackablePOI> drawnTrackablePOIs;

    /**
     * Tracker manager object
     */
    private SKPOITrackerManager poiTrackingManager;

    /**
     * Current position provider
     */
    private SKCurrentPositionProvider currentPositionProvider;

    /**
     * Current position
     */
    private SKPosition currentPosition;

    /**
     * Tells if heading is currently active
     */
    private boolean headingOn;


    /**
     * Real reach range
     */
    private int realReachRange;

    /**
     * Real reach default vehicle type
     */
    private byte realReachVehicleType = SKRealReachSettings.VEHICLE_TYPE_PEDESTRIAN;

    /**
     * Pedestrian button
     */
    private ImageButton pedestrianButton;

    /**
     * Bike button
     */
    private ImageButton bikeButton;

    /**
     * Car button
     */
    private ImageButton carButton;

    /**
     * Navigation UI layout
     */
    private RelativeLayout navigationUI;



    private boolean isStartPointBtnPressed = false, isEndPointBtnPressed = false, isViaPointSelected = false;

    /**
     * The start point(long/lat) for the route.
     */
    private SKCoordinate startPoint;

    /**
     * The destination(long/lat) point for the route
     */
    private SKCoordinate destinationPoint;

    /**
     * The via point(long/lat) for the route
     */
    private SKViaPoint viaPoint;

    /**
     * Text to speech engine
     */
    private TextToSpeech textToSpeechEngine;

    private SKToolsNavigationManager navigationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DemoUtils.initializeLibrary(this);
        setContentView(R.layout.activity_map);

        app = (DemoApplication) getApplication();

        currentPositionProvider = new SKCurrentPositionProvider(this);
        currentPositionProvider.setCurrentPositionListener(this);

        if (DemoUtils.hasGpsModule(this)) {
            currentPositionProvider.requestLocationUpdates(true, false, true);
        } else if (DemoUtils.hasNetworkModule(this)) {
            currentPositionProvider.requestLocationUpdates(false, true, true);
        }

        SKMapViewHolder mapViewGroup = (SKMapViewHolder) findViewById(R.id.view_group_map);
        mapView = mapViewGroup.getMapSurfaceView();
        mapView.setMapSurfaceListener(MapActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mapPopup = mapViewGroup.getCalloutView();
        View view = inflater.inflate(R.layout.layout_popup, null);
        popupTitleView = (TextView) view.findViewById(R.id.top_text);
        popupDescriptionView = (TextView) view.findViewById(R.id.bottom_text);
        mapPopup.setCustomView(view);


        applySettingsOnMapView();
        poiTrackingManager = new SKPOITrackerManager(this);

        menu = findViewById(R.id.options_menu);
        altRoutesView = findViewById(R.id.alt_routes);
        altRoutesButtons =
                new Button[]{(Button) findViewById(R.id.alt_route_1), (Button) findViewById(R.id.alt_route_2),
                        (Button) findViewById(R.id.alt_route_3)};

        mapStylesView = (LinearLayout) findViewById(R.id.map_styles);
        bottomButton = (Button) findViewById(R.id.bottom_button);
        positionMeButton = (Button) findViewById(R.id.position_me_button);
        headingButton = (Button) findViewById(R.id.heading_button);

        pedestrianButton = (ImageButton) findViewById(R.id.real_reach_pedestrian_button);
        bikeButton = (ImageButton) findViewById(R.id.real_reach_bike_button);
        carButton = (ImageButton) findViewById(R.id.real_reach_car_button);

        SKVersioningManager.getInstance().setMapUpdateListener(this);

        final TextView realReachTimeText = (TextView) findViewById(R.id.real_reach_time);
        final TextView realReachEnergyText = (TextView) findViewById(R.id.real_reach_energy);

        SeekBar realReachSeekBar = (SeekBar) findViewById(R.id.real_reach_seekbar);
        realReachSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                realReachRange = progress;
                realReachTimeText.setText(realReachRange + " min");
                showRealReach(realReachVehicleType, realReachRange);

            }


        });
        SeekBar realReachEnergySeekBar = (SeekBar) findViewById(R.id.real_reach_energy_seekbar);
        realReachEnergySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                realReachRange = progress;
                realReachEnergyText.setText(realReachRange + "%");
                showRealReachEnergy(realReachRange);


            }


        });
        realReachTimeLayout = (RelativeLayout) findViewById(R.id.real_reach_time_layout);
        realReachEnergyLayout = (RelativeLayout) findViewById(R.id.real_reach_energy_layout);
        navigationUI = (RelativeLayout) findViewById(R.id.navigation_ui_layout);

        initializeTrackablePOIs();


    }

    /**
     * Customize the map view
     */
    private void applySettingsOnMapView() {
        mapView.getMapSettings().setMapRotationEnabled(true);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(true);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
    }

    @SuppressLint("UseSparseArrays")
    /**
     * Populate the collection of trackable POIs
     */
    private void initializeTrackablePOIs() {

        trackablePOIs = new HashMap<Integer, SKTrackablePOI>();

        trackablePOIs.put(64142, new SKTrackablePOI(64142, 0, 37.735610, -122.446434, -1, "Teresita Boulevard"));
        trackablePOIs.put(64143, new SKTrackablePOI(64143, 0, 37.732367, -122.442033, -1, "Congo Street"));
        trackablePOIs.put(64144, new SKTrackablePOI(64144, 0, 37.732237, -122.429190, -1, "John F Foran Freeway"));
        trackablePOIs.put(64145, new SKTrackablePOI(64145, 1, 37.738090, -122.401470, -1, "Revere Avenue"));
        trackablePOIs.put(64146, new SKTrackablePOI(64146, 0, 37.741128, -122.398562, -1, "McKinnon Ave"));
        trackablePOIs.put(64147, new SKTrackablePOI(64147, 1, 37.746154, -122.394077, -1, "Evans Ave"));
        trackablePOIs.put(64148, new SKTrackablePOI(64148, 0, 37.750057, -122.392287, -1, "Cesar Chavez Street"));
        trackablePOIs.put(64149, new SKTrackablePOI(64149, 1, 37.762823, -122.392957, -1, "18th Street"));
        trackablePOIs.put(64150, new SKTrackablePOI(64150, 0, 37.760242, -122.392495, 180, "20th Street"));
        trackablePOIs.put(64151, new SKTrackablePOI(64151, 0, 37.755157, -122.392196, 180, "23rd Street"));

        trackablePOIs.put(64152, new SKTrackablePOI(64152, 0, 37.773526, -122.452706, -1, "Shrader Street"));
        trackablePOIs.put(64153, new SKTrackablePOI(64153, 0, 37.786535, -122.444528, -1, "Pine Street"));
        trackablePOIs.put(64154, new SKTrackablePOI(64154, 1, 37.792242, -122.424426, -1, "Franklin Street"));
        trackablePOIs.put(64155, new SKTrackablePOI(64155, 0, 37.716146, -122.409480, -1, "Campbell Ave"));
        trackablePOIs.put(64156, new SKTrackablePOI(64156, 0, 37.719133, -122.388280, -1, "Fitzgerald Ave"));

        drawnTrackablePOIs = new HashMap<Integer, SKTrackablePOI>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        if (headingOn) {
            startOrientationSensor();
        }

        if (currentMapOption == MapOption.NAVI_UI) {
            final ToggleButton selectStartPointBtn = (ToggleButton) findViewById(R.id.select_start_point_button);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String prefNavigationType = sharedPreferences.getString(PreferenceTypes.K_NAVIGATION_TYPE,
                    "1");
            if (prefNavigationType.equals("0")) { // real navi
                selectStartPointBtn.setVisibility(View.GONE);
            } else if (prefNavigationType.equals("1")) {
                selectStartPointBtn.setVisibility(View.VISIBLE);
            }
        }

        if (currentMapOption == MapOption.HEAT_MAP && heatMapCategories != null) {
            mapView.showHeatMapsWithPoiType(heatMapCategories);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (headingOn) {
            stopOrientationSensor();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentPositionProvider.stopLocationUpdates();
        SKMaps.getInstance().destroySKMaps();
        if (textToSpeechEngine != null) {
            textToSpeechEngine.stop();
            textToSpeechEngine.shutdown();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onSurfaceCreated() {
        View chessBackground = findViewById(R.id.chess_board_background);
        chessBackground.setVisibility(View.GONE);
        mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }


    @Override
    public void onGLInitializationError(String messsage) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TRACKS:
                    if (currentMapOption.equals(MapOption.TRACKS) && TrackElementsActivity.selectedTrackElement !=
                            null) {
                        mapView.drawTrackElement(TrackElementsActivity.selectedTrackElement);
                        mapView.fitTrackElementInView(TrackElementsActivity.selectedTrackElement, false);

                        SKRouteManager.getInstance().setRouteListener(this);
                        SKRouteManager.getInstance().createRouteFromTrackElement(
                                TrackElementsActivity.selectedTrackElement, SKRouteMode.BICYCLE_FASTEST, true, true,
                                false);
                    }
                    break;

                default:
                    break;
            }
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && !skToolsNavigationInProgress && !skToolsRouteCalculated) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (menu.getVisibility() == View.VISIBLE) {
                    menu.setVisibility(View.GONE);
                } else if (menu.getVisibility() == View.GONE) {
                    menu.setVisibility(View.VISIBLE);
                    menu.bringToFront();
                }
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @SuppressLint("ResourceAsColor")
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.alt_route_1:
                selectAlternativeRoute(0);
                break;
            case R.id.alt_route_2:
                selectAlternativeRoute(1);
                break;
            case R.id.alt_route_3:
                selectAlternativeRoute(2);
                break;
            case R.id.map_style_day:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "daystyle/", "daystyle.json"));
                break;
            case R.id.map_style_night:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "nightstyle/", "nightstyle.json"));
                break;
            case R.id.map_style_outdoor:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "outdoorstyle/", "outdoorstyle.json"));
                break;
            case R.id.map_style_grayscale:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "grayscalestyle/",
                        "grayscalestyle.json"));
                break;
            case R.id.bottom_button:
                if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION || currentMapOption == MapOption.TRACKS) {
                    if (bottomButton.getText().equals(getResources().getString(R.string.calculate_route))) {
                        launchRouteCalculation();
                    } else if (bottomButton.getText().equals(getResources().getString(R.string.start_navigation))) {
                        new AlertDialog.Builder(this)
                                .setMessage("Choose the advice type")
                                .setCancelable(false)
                                .setPositiveButton("Scout audio", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        bottomButton.setText(getResources().getString(R.string.stop_navigation));
                                        setAdvicesAndStartNavigation(MapAdvices.AUDIO_FILES);
                                    }
                                })
                                .setNegativeButton("Text to speech", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (textToSpeechEngine == null) {
                                            Toast.makeText(MapActivity.this, "Initializing TTS engine",
                                                    Toast.LENGTH_LONG).show();
                                            textToSpeechEngine = new TextToSpeech(MapActivity.this,
                                                    new TextToSpeech.OnInitListener() {
                                                        @Override
                                                        public void onInit(int status) {
                                                            if (status == TextToSpeech.SUCCESS) {
                                                                int result = textToSpeechEngine.setLanguage(Locale.ENGLISH);
                                                                if (result == TextToSpeech.LANG_MISSING_DATA || result ==
                                                                        TextToSpeech.LANG_NOT_SUPPORTED) {
                                                                    Toast.makeText(MapActivity.this,
                                                                            "This Language is not supported",
                                                                            Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                            bottomButton.setText(getResources().getString(R.string
                                                                    .stop_navigation));
                                                            setAdvicesAndStartNavigation(MapAdvices.TEXT_TO_SPEECH);
                                                        }
                                                    });
                                        } else {
                                            bottomButton.setText(getResources().getString(R.string.stop_navigation));
                                            setAdvicesAndStartNavigation(MapAdvices.TEXT_TO_SPEECH);
                                        }

                                    }
                                })
                                .show();
                        bottomButton.setText(getResources().getString(R.string.stop_navigation));
                    } else if (bottomButton.getText().equals(getResources().getString(R.string.stop_navigation))) {
                        stopNavigation();
                        bottomButton.setText(getResources().getString(R.string.start_navigation));
                    }
                }
                break;
            case R.id.position_me_button:
                if (headingOn) {
                    setHeading(false);
                }
                if (currentPosition != null) {
                    mapView.centerMapOnCurrentPositionSmooth(17, 500);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_position_available), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.heading_button:
                if (currentPosition != null) {
                    setHeading(true);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_position_available), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.real_reach_pedestrian_button:
                realReachVehicleType = SKRealReachSettings.VEHICLE_TYPE_PEDESTRIAN;
                showRealReach(realReachVehicleType, realReachRange);
                pedestrianButton.setBackgroundColor(getResources().getColor(R.color.blue_filling));
                bikeButton.setBackgroundColor(getResources().getColor(R.color.grey));
                carButton.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            case R.id.real_reach_bike_button:
                realReachVehicleType = SKRealReachSettings.VEHICLE_TYPE_BICYCLE;
                showRealReach(realReachVehicleType, realReachRange);
                bikeButton.setBackgroundColor(getResources().getColor(R.color.blue_filling));
                pedestrianButton.setBackgroundColor(getResources().getColor(R.color.grey));
                carButton.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            case R.id.real_reach_car_button:
                realReachVehicleType = SKRealReachSettings.VEHICLE_TYPE_CAR;
                showRealReach(realReachVehicleType, realReachRange);
                carButton.setBackgroundColor(getResources().getColor(R.color.blue_filling));
                pedestrianButton.setBackgroundColor(getResources().getColor(R.color.grey));
                bikeButton.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            case R.id.exit_real_reach_time:
                realReachTimeLayout.setVisibility(View.GONE);
                clearMap();
                break;
            case R.id.exit_real_reach_energy:
                realReachEnergyLayout.setVisibility(View.GONE);
                clearMap();
                break;
            case R.id.navigation_ui_back_button:
                Button backButton = (Button) findViewById(R.id.navigation_ui_back_button);
                LinearLayout naviButtons = (LinearLayout) findViewById(R.id.navigation_ui_buttons);
                if (backButton.getText().equals(">")) {
                    naviButtons.setVisibility(View.VISIBLE);
                    backButton.setText("<");
                } else {
                    naviButtons.setVisibility(View.GONE);
                    backButton.setText(">");
                }
                break;
            case R.id.calculate_routes_button:
                calculateRouteFromSKTools();
                break;

            case R.id.settings_button:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.start_free_drive_button:
                startFreeDriveFromSKTools();
                break;
            case R.id.clear_via_point_button:
                viaPoint = null;
                mapView.deleteAnnotation(VIA_POINT_ICON_ID);
                findViewById(R.id.clear_via_point_button).setVisibility(View.GONE);
                break;
            case R.id.position_me_navigation_ui_button:
                if (currentPosition != null) {
                    mapView.centerMapOnCurrentPositionSmooth(15, 1000);
                    mapView.getMapSettings().setOrientationIndicatorType(
                            SKMapSurfaceView.SKOrientationIndicatorType.DEFAULT);
                    mapView.getMapSettings()
                            .setFollowerMode(SKMapFollowerMode.NONE);
                } else {
                    Toast.makeText(MapActivity.this,
                            getString(R.string.no_position_available),
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void startFreeDriveFromSKTools() {
        SKToolsNavigationConfiguration configuration = new SKToolsNavigationConfiguration();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prefDistanceFormat = sharedPreferences.getString(PreferenceTypes.K_DISTANCE_UNIT, "0");
        if (prefDistanceFormat.equals("0")) {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_KILOMETER_METERS);
        } else if (prefDistanceFormat.equals("1")) {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_FEET);
        } else {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_YARDS);
        }

        //set speed in town
        String prefSpeedInTown = sharedPreferences.getString(PreferenceTypes.K_IN_TOWN_SPEED_WARNING, "0");
        if (prefSpeedInTown.equals("0")) {
            configuration.setSpeedWarningThresholdInCity(5.0);
        } else if (prefSpeedInTown.equals("1")) {
            configuration.setSpeedWarningThresholdInCity(10.0);
        } else if (prefSpeedInTown.equals("2")) {
            configuration.setSpeedWarningThresholdInCity(15.0);
        } else if (prefSpeedInTown.equals("3")) {
            configuration.setSpeedWarningThresholdInCity(20.0);
        }
        //set speed out
        String prefSpeedOutTown = sharedPreferences.getString(PreferenceTypes.K_OUT_TOWN_SPEED_WARNING, "0");
        if (prefSpeedOutTown.equals("0")) {
            configuration.setSpeedWarningThresholdOutsideCity(5.0);
        } else if (prefSpeedOutTown.equals("1")) {
            configuration.setSpeedWarningThresholdOutsideCity(10.0);
        } else if (prefSpeedOutTown.equals("2")) {
            configuration.setSpeedWarningThresholdOutsideCity(15.0);
        } else if (prefSpeedOutTown.equals("3")) {
            configuration.setSpeedWarningThresholdOutsideCity(20.0);
        }
        boolean dayNight = sharedPreferences.getBoolean(PreferenceTypes.K_AUTO_DAY_NIGHT, true);
        if (!dayNight) {
            configuration.setAutomaticDayNight(false);
        }
        configuration.setNavigationType(SKNavigationType.FILE);
        configuration.setFreeDriveNavigationFilePath(app.getMapResourcesDirPath() + "logFile/Seattle.log");
        configuration.setDayStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "daystyle/",
                "daystyle.json"));
        configuration.setNightStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "nightstyle/",
                "nightstyle.json"));

        navigationUI.setVisibility(View.GONE);
        navigationManager = new SKToolsNavigationManager(this, R.id.map_layout_root);
        navigationManager.setNavigationListener(this);
        navigationManager.startFreeDriveWithConfiguration(configuration, mapView);

    }

    private void calculateRouteFromSKTools() {

        SKToolsNavigationConfiguration configuration = new SKToolsNavigationConfiguration();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //set navigation type
        String prefNavigationType = sharedPreferences.getString(PreferenceTypes.K_NAVIGATION_TYPE,
                "1");
        if (prefNavigationType.equals("0")) {
            configuration.setNavigationType(SKNavigationType.REAL);
            if (currentPosition == null) {
                showNoCurrentPosDialog();
                return;
            }
            startPoint = new SKCoordinate(currentPosition.getLongitude(), currentPosition.getLatitude());
        } else if (prefNavigationType.equals("1")) {
            configuration.setNavigationType(SKNavigationType.SIMULATION);

        }

        //set route type
        String prefRouteType = sharedPreferences.getString(PreferenceTypes.K_ROUTE_TYPE,
                "2");
        if (prefRouteType.equals("0")) {
            configuration.setRouteType(SKRouteMode.CAR_SHORTEST);
        } else if (prefRouteType.equals("1")) {
            configuration.setRouteType(SKRouteMode.CAR_FASTEST);
        } else if (prefRouteType.equals("2")) {
            configuration.setRouteType(SKRouteMode.EFFICIENT);
        } else if (prefRouteType.equals("3")) {
            configuration.setRouteType(SKRouteMode.PEDESTRIAN);
        } else if (prefRouteType.equals("4")) {
            configuration.setRouteType(SKRouteMode.BICYCLE_FASTEST);
        } else if (prefRouteType.equals("5")) {
            configuration.setRouteType(SKRouteMode.BICYCLE_SHORTEST);
        } else if (prefRouteType.equals("6")) {
            configuration.setRouteType(SKRouteMode.BICYCLE_QUIETEST);
        }

        //set distance format
        String prefDistanceFormat = sharedPreferences.getString(PreferenceTypes.K_DISTANCE_UNIT,
                "0");
        if (prefDistanceFormat.equals("0")) {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_KILOMETER_METERS);
        } else if (prefDistanceFormat.equals("1")) {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_FEET);
        } else {
            configuration.setDistanceUnitType(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_YARDS);
        }

        //set speed in town
        String prefSpeedInTown = sharedPreferences.getString(PreferenceTypes.K_IN_TOWN_SPEED_WARNING, "0");
        if (prefSpeedInTown.equals("0")) {
            configuration.setSpeedWarningThresholdInCity(5.0);
        } else if (prefSpeedInTown.equals("1")) {
            configuration.setSpeedWarningThresholdInCity(10.0);
        } else if (prefSpeedInTown.equals("2")) {
            configuration.setSpeedWarningThresholdInCity(15.0);
        } else if (prefSpeedInTown.equals("3")) {
            configuration.setSpeedWarningThresholdInCity(20.0);
        }

        //set speed out
        String prefSpeedOutTown = sharedPreferences.getString(PreferenceTypes.K_OUT_TOWN_SPEED_WARNING, "0");
        if (prefSpeedOutTown.equals("0")) {
            configuration.setSpeedWarningThresholdOutsideCity(5.0);
        } else if (prefSpeedOutTown.equals("1")) {
            configuration.setSpeedWarningThresholdOutsideCity(10.0);
        } else if (prefSpeedOutTown.equals("2")) {
            configuration.setSpeedWarningThresholdOutsideCity(15.0);
        } else if (prefSpeedOutTown.equals("3")) {
            configuration.setSpeedWarningThresholdOutsideCity(20.0);
        }
        boolean dayNight = sharedPreferences.getBoolean(PreferenceTypes.K_AUTO_DAY_NIGHT, true);
        if (!dayNight) {
            configuration.setAutomaticDayNight(false);
        }
        boolean tollRoads = sharedPreferences.getBoolean(PreferenceTypes.K_AVOID_TOLL_ROADS, false);
        if (tollRoads) {
            configuration.setTollRoadsAvoided(true);
        }
        boolean avoidFerries = sharedPreferences.getBoolean(PreferenceTypes.K_AVOID_FERRIES, false);
        if (avoidFerries) {
            configuration.setFerriesAvoided(true);
        }
        boolean highWays = sharedPreferences.getBoolean(PreferenceTypes.K_AVOID_HIGHWAYS, false);
        if (highWays) {
            configuration.setHighWaysAvoided(true);
        }
        boolean freeDrive = sharedPreferences.getBoolean(PreferenceTypes.K_FREE_DRIVE, true);
        if (!freeDrive) {
            configuration.setContinueFreeDriveAfterNavigationEnd(false);
        }

        navigationUI.setVisibility(View.GONE);
        configuration.setStartCoordinate(startPoint);
        configuration.setDestinationCoordinate(destinationPoint);
        List<SKViaPoint> viaPointList = new ArrayList<SKViaPoint>();
        if (viaPoint != null) {
            viaPointList.add(viaPoint);
            configuration.setViaPointCoordinateList(viaPointList);
        }
        configuration.setDayStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "daystyle/",
                "daystyle.json"));
        configuration.setNightStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "nightstyle/",
                "nightstyle.json"));
        navigationManager = new SKToolsNavigationManager(this, R.id.map_layout_root);
        navigationManager.setNavigationListener(this);

        if (configuration.getStartCoordinate() != null && configuration.getDestinationCoordinate() != null) {
            navigationManager.launchRouteCalculation(configuration, mapView);
        }


    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void onMenuOptionClick(View v) {
        clearMap();
        switch (v.getId()) {
            case R.id.option_map_display:
                mapView.clearHeatMapsDisplay();
                currentMapOption = MapOption.MAP_DISPLAY;
                bottomButton.setVisibility(View.GONE);
                SKRouteManager.getInstance().clearCurrentRoute();
                break;
            case R.id.option_overlays:
                currentMapOption = MapOption.MAP_OVERLAYS;
                drawShapes();
                mapView.setZoom(14);
                mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));
                break;
            case R.id.option_alt_routes:
                currentMapOption = MapOption.ALTERNATIVE_ROUTES;
                altRoutesView.setVisibility(View.VISIBLE);
                launchAlternativeRouteCalculation();
                break;
            case R.id.option_map_styles:
                currentMapOption = MapOption.MAP_STYLES;
                mapStylesView.setVisibility(View.VISIBLE);
                selectStyleButton();
                break;
            case R.id.option_map_creator:
                currentMapOption = MapOption.MAP_DISPLAY;
                mapView.applySettingsFromFile(app.getMapCreatorFilePath());
                break;
            case R.id.option_tracks:
                currentMapOption = MapOption.TRACKS;
                Intent intent = new Intent(this, TracksActivity.class);
                startActivityForResult(intent, TRACKS);
                break;
            case R.id.option_real_reach:
                new AlertDialog.Builder(this)
                        .setMessage("Choose the real reach type")
                        .setCancelable(false)
                        .setPositiveButton("Time profile", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                currentMapOption = MapOption.REAL_REACH;
                                realReachTimeLayout.setVisibility(View.VISIBLE);
                                showRealReach(realReachVehicleType, realReachRange);
                            }
                        })
                        .setNegativeButton("Energy profile", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                currentMapOption = MapOption.REAL_REACH;
                                realReachEnergyLayout.setVisibility(View.VISIBLE);
                                showRealReachEnergy(realReachRange);

                            }
                        })
                        .show();
                break;
            case R.id.option_map_xml_and_downloads:
                if (DemoUtils.isInternetAvailable(this)) {
                    startActivity(new Intent(MapActivity.this, ResourceDownloadsListActivity.class));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.option_reverse_geocoding:
                startActivity(new Intent(this, ReverseGeocodingActivity.class));
                break;
            case R.id.option_address_search:
                startActivity(new Intent(this, OfflineAddressSearchActivity.class));
                break;
            case R.id.option_nearby_search:
                startActivity(new Intent(this, NearbySearchActivity.class));
                break;
            case R.id.option_annotations:
                currentMapOption = MapOption.ANNOTATIONS;
                prepareAnnotations();
                break;
            case R.id.option_category_search:
                startActivity(new Intent(this, CategorySearchResultsActivity.class));
                break;
            case R.id.option_routing_and_navigation:
                currentMapOption = MapOption.ROUTING_AND_NAVIGATION;
                bottomButton.setVisibility(View.VISIBLE);
                bottomButton.setText(getResources().getString(R.string.calculate_route));
                break;
            case R.id.option_poi_tracking:
                currentMapOption = MapOption.POI_TRACKING;
                if (trackablePOIs == null) {
                    initializeTrackablePOIs();
                }
                launchRouteCalculation();
                break;
            case R.id.option_heat_map:
                currentMapOption = MapOption.HEAT_MAP;
                startActivity(new Intent(this, POICategoriesListActivity.class));
                break;
            case R.id.option_map_updates:
                SKVersioningManager.getInstance().checkNewVersion(3);
                break;
            case R.id.option_map_interaction:
                currentMapOption = MapOption.MAP_INTERACTION;
                handleMapInteractionOption();
                break;
            case R.id.option_navigation_ui:
                currentMapOption = MapOption.NAVI_UI;
                initializeNavigationUI(true);
                break;
            default:
                break;
        }
        if (currentMapOption != MapOption.MAP_DISPLAY) {
            positionMeButton.setVisibility(View.GONE);
            headingButton.setVisibility(View.GONE);
        }
        menu.setVisibility(View.GONE);
    }


    private void initializeNavigationUI(boolean showStartingAndDestinationAnnotations) {
        final ToggleButton selectViaPointBtn = (ToggleButton) findViewById(R.id.select_via_point_button);
        final ToggleButton selectStartPointBtn = (ToggleButton) findViewById(R.id.select_start_point_button);
        final ToggleButton selectEndPointBtn = (ToggleButton) findViewById(R.id.select_end_point_button);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prefNavigationType = sharedPreferences.getString(PreferenceTypes.K_NAVIGATION_TYPE,
                "1");
        if (prefNavigationType.equals("0")) { // real navi
            selectStartPointBtn.setVisibility(View.GONE);
        } else if (prefNavigationType.equals("1")) {
            selectStartPointBtn.setVisibility(View.VISIBLE);
        }

        if (showStartingAndDestinationAnnotations) {
            startPoint = new SKCoordinate(13.34615707397461, 52.513086884218325);
            SKAnnotation annotation = new SKAnnotation(GREEN_PIN_ICON_ID);
            annotation
                    .setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
            annotation.setLocation(startPoint);
            mapView.addAnnotation(annotation,
                    SKAnimationSettings.ANIMATION_NONE);

            destinationPoint = new SKCoordinate(13.398685455322266, 52.50995268098114);
            annotation = new SKAnnotation(RED_PIN_ICON_ID);
            annotation
                    .setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
            annotation.setLocation(destinationPoint);
            mapView.addAnnotation(annotation,
                    SKAnimationSettings.ANIMATION_NONE);

        }
        mapView.setZoom(11);
        mapView.centerMapOnPosition(startPoint);


        selectStartPointBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isStartPointBtnPressed = true;
                    isEndPointBtnPressed = false;
                    isViaPointSelected = false;
                    selectEndPointBtn.setChecked(false);
                    selectViaPointBtn.setChecked(false);
                    Toast.makeText(MapActivity.this, getString(R.string.long_tap_for_position),
                            Toast.LENGTH_LONG).show();
                } else {
                    isStartPointBtnPressed = false;
                }
            }
        });
        selectEndPointBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isEndPointBtnPressed = true;
                    isStartPointBtnPressed = false;
                    isViaPointSelected = false;
                    selectStartPointBtn.setChecked(false);
                    selectViaPointBtn.setChecked(false);
                    Toast.makeText(MapActivity.this, getString(R.string.long_tap_for_position),
                            Toast.LENGTH_LONG).show();
                } else {
                    isEndPointBtnPressed = false;
                }
            }
        });

        selectViaPointBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isViaPointSelected = true;
                    isStartPointBtnPressed = false;
                    isEndPointBtnPressed = false;
                    selectStartPointBtn.setChecked(false);
                    selectEndPointBtn.setChecked(false);
                    Toast.makeText(MapActivity.this, getString(R.string.long_tap_for_position),
                            Toast.LENGTH_LONG).show();
                } else {
                    isViaPointSelected = false;
                }
            }
        });

        navigationUI.setVisibility(View.VISIBLE);
    }

    private void showNoCurrentPosDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
//        alert.setTitle("Really quit?");
        alert.setMessage("There is no current position available");
        alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alert.show();
    }

    private void handleMapInteractionOption() {

        mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));

        // get the annotation object
        SKAnnotation annotation1 = new SKAnnotation(10);
        // set annotation location
        annotation1.setLocation(new SKCoordinate(-122.4200, 37.7765));
        // set minimum zoom level at which the annotation should be visible
        annotation1.setMininumZoomLevel(5);
        // set the annotation's type
        annotation1.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        // render annotation on map
        mapView.addAnnotation(annotation1, SKAnimationSettings.ANIMATION_NONE);

        SKAnnotation annotation2 = new SKAnnotation(11);
        annotation2.setLocation(new SKCoordinate(-122.419789, 37.775428));
        annotation2.setMininumZoomLevel(5);
        annotation2.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
        mapView.addAnnotation(annotation2, SKAnimationSettings.ANIMATION_NONE);

        final float density = getResources().getDisplayMetrics().density;

        TextView topText = (TextView) mapPopup.findViewById(R.id.top_text);
        topText.setText("Get details");
        topText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, InteractionMapActivity.class));
            }
        });
        mapPopup.findViewById(R.id.bottom_text).setVisibility(View.GONE);

        mapPopup.setVerticalOffset(30 * density);
        mapPopup.showAtLocation(annotation1.getLocation(), true);

    }

    /**
     * Launches a single route calculation
     */
    private void launchRouteCalculation() {
        // get a route object and populate it with the desired properties
        SKRouteSettings route = new SKRouteSettings();
        // set start and destination points
        route.setStartCoordinate(new SKCoordinate(-122.397674, 37.761278));
        route.setDestinationCoordinate(new SKCoordinate(-122.448270, 37.738761));
        // set the number of routes to be calculated
        route.setNoOfRoutes(1);
        // set the route mode
        route.setRouteMode(SKRouteMode.CAR_FASTEST);
        // set whether the route should be shown on the map after it's computed
        route.setRouteExposed(true);
        // set the route listener to be notified of route calculation
        // events
        SKRouteManager.getInstance().setRouteListener(this);
        // pass the route to the calculation routine
        SKRouteManager.getInstance().calculateRoute(route);
    }

    /**
     * Launches the calculation of three alternative routes
     */
    private void launchAlternativeRouteCalculation() {
        SKRouteSettings route = new SKRouteSettings();
        route.setStartCoordinate(new SKCoordinate(-122.392284, 37.787189));
        route.setDestinationCoordinate(new SKCoordinate(-122.484378, 37.856300));
        // number of alternative routes specified here
        route.setNoOfRoutes(3);
        route.setRouteMode(SKRouteMode.CAR_FASTEST);
        route.setRouteExposed(true);
        SKRouteManager.getInstance().setRouteListener(this);
        SKRouteManager.getInstance().calculateRoute(route);
    }

    /**
     * Initiate real reach time profile
     */
    private void showRealReach(byte vehicleType, int range) {

        // set listener for real reach calculation events
        mapView.setRealReachListener(this);
        // get object that can be used to specify real reach calculation
        // properties
        SKRealReachSettings realReachSettings = new SKRealReachSettings();
        // set center position for real reach
        SKCoordinate realReachCenter = new SKCoordinate(23.593957, 46.773361);
        realReachSettings.setLocation(realReachCenter);
        // set measurement unit for real reach
        realReachSettings.setMeasurementUnit(SKRealReachSettings.UNIT_SECOND);
        // set the range value (in the unit previously specified)
        realReachSettings.setRange(range * 60);
        // set the transport mode
        realReachSettings.setTransportMode(vehicleType);
        // initiate real reach
        mapView.displayRealReachWithSettings(realReachSettings);
    }

    /**
     * The cunsumption values
     */
    private float[] energyConsumption = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (float) 3.7395504, (float) 4.4476889, (float) 5.4306439, (float) 6.722719,
            (float) 8.2830299, (float) 10.0275093, (float) 11.8820908, (float) 13.799201, (float) 15.751434, (float) 17.7231534, (float) 19.7051378, (float) 21.6916725,
            (float) 23.679014, (float) 25.6645696, (float) 27.6464437, (float) 29.6231796, (float) 31.5936073};

    /**
     * Initiate real reach energy profile
     */

    private void showRealReachEnergy(int range) {

        //set listener for real reach calculation events
        mapView.setRealReachListener(this);
        // get object that can be used to specify real reach calculation
        // properties
        SKRealReachSettings realReachSettings = new SKRealReachSettings();
        SKCoordinate realReachCenter = new SKCoordinate(23.593957, 46.773361);
        realReachSettings.setLocation(realReachCenter);
        // set measurement unit for real reach
        realReachSettings.setMeasurementUnit(SKRealReachSettings.UNIT_MILIWATT_HOURS);
        // set consumption values
        realReachSettings.setConsumption(energyConsumption);
        // set the range value (in the unit previously specified)
        realReachSettings.setRange(range * 100);
        // set the transport mode
        realReachSettings.setTransportMode(SKRealReachSettings.VEHICLE_TYPE_BICYCLE);
        // initiate real reach
        mapView.displayRealReachWithSettings(realReachSettings);

    }

    /**
     * Draws annotations on map
     */
    private void prepareAnnotations() {

        // Add annotation using texture ID - from the json files.
        // get the annotation object
        SKAnnotation annotation1 = new SKAnnotation(10);
        // set annotation location
        annotation1.setLocation(new SKCoordinate(-122.4200, 37.7765));
        // set minimum zoom level at which the annotation should be visible
        annotation1.setMininumZoomLevel(5);
        // set the annotation's type
        annotation1.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        // render annotation on map
        mapView.addAnnotation(annotation1, SKAnimationSettings.ANIMATION_NONE);


        // Add an annotation using the absolute path to the image.
        SKAnnotation annotation = new SKAnnotation(13);
        annotation.setLocation(new SKCoordinate(-122.434516, 37.770712));
        annotation.setMininumZoomLevel(5);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (metrics.densityDpi < DisplayMetrics.DENSITY_HIGH) {
            annotation.setImagePath(SKMaps.getInstance().getMapInitSettings().getMapResourcesPath()
                    + "/.Common/icon_bluepin@2x.png");
            // set the size of the image in pixels
            annotation.setImageSize(128);
        } else {
            annotation.setImagePath(SKMaps.getInstance().getMapInitSettings().getMapResourcesPath()
                    + "/.Common/icon_bluepin@3x.png");
            // set the size of the image in pixels
            annotation.setImageSize(256);

        }
        // by default the center of the image corresponds with the location .annotation.setOffset can be use to position the image around the location.
        mapView.addAnnotation(annotation, SKAnimationSettings.ANIMATION_NONE);


        // add an annotation with a drawable resource
        SKAnnotation annotationDrawable = new SKAnnotation(14);
        annotationDrawable.setLocation(new SKCoordinate(-122.437182, 37.777079));
        annotationDrawable.setMininumZoomLevel(5);


        SKAnnotationView annotationView = new SKAnnotationView();
        annotationView.setDrawableResourceId(R.drawable.icon_map_popup_navigate);
        // set the width and height of the image in pixels . If they are not power of 2 the actual size of the image will be the next power of 2 of max(width,height)
        annotationView.setWidth(128);
        annotationView.setHeight(128);
        annotationDrawable.setAnnotationView(annotationView);
        mapView.addAnnotation(annotationDrawable, SKAnimationSettings.ANIMATION_NONE);


        // // add an annotation with a view
        SKAnnotation annotationFromView = new SKAnnotation(15);
        annotationFromView.setLocation(new SKCoordinate(-122.423573, 37.761349));
        annotationFromView.setMininumZoomLevel(5);
        annotationView = new SKAnnotationView();
        customView =
                (RelativeLayout) ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        R.layout.layout_custom_view, null, false);
        //  If width and height of the view  are not power of 2 the actual size of the image will be the next power of 2 of max(width,height).
        annotationView.setView(customView);
        annotationFromView.setAnnotationView(annotationView);
        mapView.addAnnotation(annotationFromView, SKAnimationSettings.ANIMATION_NONE);

        // set map zoom level
        mapView.setZoom(13);
        // center map on a position
        mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));
    }

    /**
     * Draws shapes on map
     */
    private void drawShapes() {

        // get a polygon shape object
        SKPolygon polygon = new SKPolygon();
        polygon.setIdentifier(1);
        // set the polygon's nodes
        List<SKCoordinate> nodes = new ArrayList<SKCoordinate>();
        nodes.add(new SKCoordinate(-122.4342, 37.7765));
        nodes.add(new SKCoordinate(-122.4141, 37.7765));
        nodes.add(new SKCoordinate(-122.4342, 37.7620));
        polygon.setNodes(nodes);
        // set the outline size
        polygon.setOutlineSize(3);
        // set colors used to render the polygon
        polygon.setOutlineColor(new float[]{1f, 0f, 0f, 1f});
        polygon.setColor(new float[]{1f, 0f, 0f, 0.2f});
        // render the polygon on the map
        mapView.addPolygon(polygon);

        // get a circle mask shape object
        SKCircle circleMask = new SKCircle();
        circleMask.setIdentifier(2);
        // set the shape's mask scale
        circleMask.setMaskedObjectScale(1.3f);
        // set the colors
        circleMask.setColor(new float[]{1f, 1f, 0.5f, 0.67f});
        circleMask.setOutlineColor(new float[]{0f, 0f, 0f, 1f});
        circleMask.setOutlineSize(3);
        // set circle center and radius
        circleMask.setCircleCenter(new SKCoordinate(-122.4200, 37.7665));
        circleMask.setRadius(300);
        // set outline properties
        circleMask.setOutlineDottedPixelsSkip(6);
        circleMask.setOutlineDottedPixelsSolid(10);
        // set the number of points for rendering the circle
        circleMask.setNumberOfPoints(150);
        // render the circle mask
        mapView.addCircle(circleMask);


        // get a polyline object
        SKPolyline polyline = new SKPolyline();
        polyline.setIdentifier(3);
        // set the nodes on the polyline
        nodes = new ArrayList<SKCoordinate>();
        nodes.add(new SKCoordinate(-122.4342, 37.7898));
        nodes.add(new SKCoordinate(-122.4141, 37.7898));
        nodes.add(new SKCoordinate(-122.4342, 37.7753));
        polyline.setNodes(nodes);
        // set polyline color
        polyline.setColor(new float[]{0f, 0f, 1f, 1f});
        // set properties for the outline
        polyline.setOutlineColor(new float[]{0f, 0f, 1f, 1f});
        polyline.setOutlineSize(4);
        polyline.setOutlineDottedPixelsSolid(3);
        polyline.setOutlineDottedPixelsSkip(3);
        mapView.addPolyline(polyline);
    }

    private void selectMapStyle(SKMapViewStyle newStyle) {
        mapView.getMapSettings().setMapStyle(newStyle);
        selectStyleButton();
    }

    /**
     * Selects the style button for the current map style
     */
    private void selectStyleButton() {
        for (int i = 0; i < mapStylesView.getChildCount(); i++) {
            mapStylesView.getChildAt(i).setSelected(false);
        }
        SKMapViewStyle mapStyle = mapView.getMapSettings().getMapStyle();
        if (mapStyle == null || mapStyle.getStyleFileName().equals("daystyle.json")) {
            findViewById(R.id.map_style_day).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("nightstyle.json")) {
            findViewById(R.id.map_style_night).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("outdoorstyle.json")) {
            findViewById(R.id.map_style_outdoor).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("grayscalestyle.json")) {
            findViewById(R.id.map_style_grayscale).setSelected(true);
        }
    }

    /**
     * Clears the map
     */
    private void clearMap() {
        setHeading(false);
        switch (currentMapOption) {
            case MAP_DISPLAY:
                break;
            case MAP_OVERLAYS:
                // clear all map overlays (shapes)
                mapView.clearAllOverlays();
                break;
            case ALTERNATIVE_ROUTES:
                hideAlternativeRoutesButtons();
                // clear the alternative routes
                SKRouteManager.getInstance().clearRouteAlternatives();
                // clear the selected route
                SKRouteManager.getInstance().clearCurrentRoute();
                routeIds.clear();
                break;
            case MAP_STYLES:
                mapStylesView.setVisibility(View.GONE);
                break;
            case TRACKS:
                if (navigationInProgress) {
                    // stop the navigation
                    stopNavigation();
                }
                bottomButton.setVisibility(View.GONE);
                if (TrackElementsActivity.selectedTrackElement != null) {
                    mapView.clearTrackElement(TrackElementsActivity.selectedTrackElement);
                    SKRouteManager.getInstance().clearCurrentRoute();
                }
                TrackElementsActivity.selectedTrackElement = null;
                break;
            case REAL_REACH:
                // removes real reach from the map
                mapView.clearRealReachDisplay();
                realReachTimeLayout.setVisibility(View.GONE);
                realReachEnergyLayout.setVisibility(View.GONE);
                break;
            case ANNOTATIONS:
                mapPopup.setVisibility(View.GONE);
                // removes the annotations and custom POIs currently rendered
                mapView.deleteAllAnnotationsAndCustomPOIs();
            case ROUTING_AND_NAVIGATION:
                bottomButton.setVisibility(View.GONE);
                SKRouteManager.getInstance().clearCurrentRoute();
                if (navigationInProgress) {
                    // stop navigation if ongoing
                    stopNavigation();
                }
                break;
            case POI_TRACKING:
                if (navigationInProgress) {
                    // stop the navigation
                    stopNavigation();
                }
                SKRouteManager.getInstance().clearCurrentRoute();
                // remove the detected POIs from the map
                mapView.deleteAllAnnotationsAndCustomPOIs();
                // stop the POI tracker
                poiTrackingManager.stopPOITracker();
                break;
            case HEAT_MAP:
                heatMapCategories = null;
                mapView.clearHeatMapsDisplay();
                break;
            case MAP_INTERACTION:
                mapPopup.setVisibility(View.GONE);
                mapView.deleteAllAnnotationsAndCustomPOIs();
                ((TextView) findViewById(R.id.top_text)).setOnClickListener(null);
                ((TextView) findViewById(R.id.top_text)).setText("Title text");
                ((TextView) findViewById(R.id.bottom_text)).setText("Subtitle text");
                break;
            case NAVI_UI:
                navigationUI.setVisibility(View.GONE);
                mapView.deleteAllAnnotationsAndCustomPOIs();
                break;
            default:
                break;
        }
        currentMapOption = MapOption.MAP_DISPLAY;
        positionMeButton.setVisibility(View.VISIBLE);
        headingButton.setVisibility(View.VISIBLE);
    }

    private void deselectAlternativeRoutesButtons() {
        for (Button b : altRoutesButtons) {
            b.setSelected(false);
        }
    }

    private void hideAlternativeRoutesButtons() {
        deselectAlternativeRoutesButtons();
        altRoutesView.setVisibility(View.GONE);
        for (Button b : altRoutesButtons) {
            b.setText("distance\ntime");
        }
    }

    private void selectAlternativeRoute(int routeIndex) {
        if (routeIds.size() > routeIndex) {
            deselectAlternativeRoutesButtons();
            altRoutesButtons[routeIndex].setSelected(true);
            SKRouteManager.getInstance().zoomToRoute(1, 1, 110, 8, 8, 8);
            SKRouteManager.getInstance().setCurrentRouteByUniqueId(routeIds.get(routeIndex));
        }

    }

    /**
     * Launches a navigation on the current route
     */
    private void launchNavigation() {
        if (TrackElementsActivity.selectedTrackElement != null) {
            mapView.clearTrackElement(TrackElementsActivity.selectedTrackElement);

        }
        // get navigation settings object
        SKNavigationSettings navigationSettings = new SKNavigationSettings();
        // set the desired navigation settings
        navigationSettings.setNavigationType(SKNavigationType.SIMULATION);
        navigationSettings.setPositionerVerticalAlignment(-0.25f);
        navigationSettings.setShowRealGPSPositions(false);
        // get the navigation manager object
        SKNavigationManager navigationManager = SKNavigationManager.getInstance();
        navigationManager.setMapView(mapView);
        // set listener for navigation events
        navigationManager.setNavigationListener(this);

        // start navigating using the settings
        navigationManager.startNavigation(navigationSettings);
        navigationInProgress = true;
    }

    /**
     * Setting the audio advices
     */
    private void setAdvicesAndStartNavigation(MapAdvices currentMapAdvices) {
        final SKAdvisorSettings advisorSettings = new SKAdvisorSettings();
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorConfigPath(app.getMapResourcesDirPath() + "/Advisor");
        advisorSettings.setResourcePath(app.getMapResourcesDirPath() + "/Advisor/Languages");
        advisorSettings.setAdvisorVoice("en");
        switch (currentMapAdvices) {
            case AUDIO_FILES:
                advisorSettings.setAdvisorType(SKAdvisorSettings.SKAdvisorType.AUDIO_FILES);
                break;
            case TEXT_TO_SPEECH:
                advisorSettings.setAdvisorType(SKAdvisorSettings.SKAdvisorType.TEXT_TO_SPEECH);
                break;
        }
        SKRouteManager.getInstance().setAudioAdvisorSettings(advisorSettings);
        launchNavigation();

    }


    /**
     * Stops the navigation
     */
    private void stopNavigation() {
        navigationInProgress = false;
        routeIds.clear();
        if (textToSpeechEngine != null && !textToSpeechEngine.isSpeaking()) {
            textToSpeechEngine.stop();
        }
        if (currentMapOption.equals(MapOption.TRACKS) && TrackElementsActivity.selectedTrackElement !=
                null) {
            SKRouteManager.getInstance().clearCurrentRoute();
            mapView.drawTrackElement(TrackElementsActivity.selectedTrackElement);
            mapView.fitTrackElementInView(TrackElementsActivity.selectedTrackElement, false);

            SKRouteManager.getInstance().setRouteListener(this);
            SKRouteManager.getInstance().createRouteFromTrackElement(
                    TrackElementsActivity.selectedTrackElement, SKRouteMode.BICYCLE_FASTEST, true, true,
                    false);
        }
        SKNavigationManager.getInstance().stopNavigation();

    }

    // route computation callbacks ...
    @Override
    public void onAllRoutesCompleted() {

        SKRouteManager.getInstance().zoomToRoute(1, 1, 8, 8, 8, 8);
        if (currentMapOption == MapOption.POI_TRACKING) {
            // start the POI tracker
            poiTrackingManager.startPOITrackerWithRadius(10000, 0.5);
            // set warning rules for trackable POIs
            poiTrackingManager.addWarningRulesforPoiType(SKTrackablePOIType.SPEEDCAM);
            // launch navigation
            launchNavigation();
        }
    }


    @Override
    public void onReceivedPOIs(SKTrackablePOIType type, List<SKDetectedPOI> detectedPois) {
        updateMapWithLatestDetectedPOIs(detectedPois);
    }

    /**
     * Updates the map when trackable POIs are detected such that only the
     * currently detected POIs are rendered on the map
     *
     * @param detectedPois
     */
    private void updateMapWithLatestDetectedPOIs(List<SKDetectedPOI> detectedPois) {

        List<Integer> detectedIdsList = new ArrayList<Integer>();
        for (SKDetectedPOI detectedPoi : detectedPois) {
            detectedIdsList.add(detectedPoi.getPoiID());
        }
        for (int detectedPoiId : detectedIdsList) {
            if (detectedPoiId == -1) {
                continue;
            }
            if (drawnTrackablePOIs.get(detectedPoiId) == null) {
                drawnTrackablePOIs.put(detectedPoiId, trackablePOIs.get(detectedPoiId));
                drawDetectedPOI(detectedPoiId);
            }
        }
        for (int drawnPoiId : new ArrayList<Integer>(drawnTrackablePOIs.keySet())) {
            if (!detectedIdsList.contains(drawnPoiId)) {
                drawnTrackablePOIs.remove(drawnPoiId);
                mapView.deleteAnnotation(drawnPoiId);
            }
        }
    }

    /**
     * Draws a detected trackable POI as an annotation on the map
     *
     * @param poiId
     */
    private void drawDetectedPOI(int poiId) {
        SKAnnotation annotation = new SKAnnotation(poiId);
        SKTrackablePOI poi = trackablePOIs.get(poiId);
        annotation.setLocation(poi.getCoordinate());
        annotation.setMininumZoomLevel(5);
        annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
        mapView.addAnnotation(annotation, SKAnimationSettings.ANIMATION_NONE);
    }

    @Override
    public void onUpdatePOIsInRadius(double latitude, double longitude, int radius) {

        // set the POIs to be tracked by the POI tracker
        poiTrackingManager.setTrackedPOIs(SKTrackablePOIType.SPEEDCAM,
                new ArrayList<SKTrackablePOI>(trackablePOIs.values()));
    }

    @Override
    public void onSensorChanged(SensorEvent t) {
        mapView.reportNewHeading(t.values[0]);
    }

    /**
     * Enables/disables heading mode
     *
     * @param enabled
     */
    private void setHeading(boolean enabled) {
        if (enabled) {
            headingOn = true;
            mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.POSITION_PLUS_HEADING);
            startOrientationSensor();
        } else {
            headingOn = false;
            mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
            stopOrientationSensor();
        }
    }

    /**
     * Activates the orientation sensor
     */
    private void startOrientationSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Deactivates the orientation sensor
     */
    private void stopOrientationSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onCurrentPositionUpdate(SKPosition currentPosition) {
        this.currentPosition = currentPosition;
        mapView.reportNewGPSPosition(this.currentPosition);
    }

    @Override
    public void onOnlineRouteComputationHanging(int status) {

    }


    // map interaction callbacks ...
    @Override
    public void onActionPan() {
        if (headingOn) {
            setHeading(false);
        }
    }

    @Override
    public void onActionZoom() {

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (navigationManager != null && skToolsNavigationInProgress) {
            navigationManager.notifyOrientationChanged();
        }
    }

    @Override
    public void onAnnotationSelected(final SKAnnotation annotation) {
        final DisplayMetrics metrics = new DisplayMetrics();
        final float density = getResources().getDisplayMetrics().density;
        if (navigationUI.getVisibility() == View.VISIBLE) {
            return;
        }
        // show the popup at the proper position when selecting an
        // annotation
        switch (annotation.getUniqueID()) {
            case 10:
                if (density <= 1) {
                    SKLogging.writeLog(TAG, "Density 1 ", SKLogging.LOG_ERROR);
                    mapPopup.setVerticalOffset(48 / density);
                } else if (density <= 2) {
                    SKLogging.writeLog(TAG, "Density 2 ", SKLogging.LOG_ERROR);
                    mapPopup.setVerticalOffset(96 / density);

                } else {
                    SKLogging.writeLog(TAG, "Density 3 ", SKLogging.LOG_ERROR);
                    mapPopup.setVerticalOffset(192 / density);
                }
                popupTitleView.setText("Annotation using texture ID");
                popupDescriptionView.setText(" Red pin ");
                break;
            case 13:
                // because the location of the annotation is the center of the image the vertical offset has to be imageSize/2
                mapPopup.setVerticalOffset(annotation.getImageSize() / 2 / density);
                popupTitleView.setText("Annotation using absolute \n image path");
                popupDescriptionView.setText(null);
                break;
            case 14:
                int properSize =
                        calculateProperSizeForView(annotation.getAnnotationView().getWidth(), annotation
                                .getAnnotationView().getHeight());
                // If  imageWidth and imageHeight for the annotationView  are not power of 2 the actual size of the image will be the next power of 2 of max(width,
                // height) so the vertical offset
                // for the callout has to be half of the annotation's size
                mapPopup.setVerticalOffset(properSize / 2 / density);
                popupTitleView.setText("Annotation using  \n drawable resource ID ");
                popupDescriptionView.setText(null);
                break;
            case 15:
                properSize = calculateProperSizeForView(customView.getWidth(), customView.getHeight());
                // If  width and height of the view  are not power of 2 the actual size of the image will be the next power of 2 of max(width,height) so the vertical offset
                // for the callout has to be half of the annotation's size
                mapPopup.setVerticalOffset(properSize / 2 / density);
                popupTitleView.setText("Annotation using custom view");
                popupDescriptionView.setText(null);
                break;

        }
        mapPopup.showAtLocation(annotation.getLocation(), true);
    }


    private int calculateProperSizeForView(int width, int height) {
        int maxDimension = Math.max(width, height);
        int power = 2;

        while (maxDimension > power) {
            power *= 2;
        }

        return power;

    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI customPoi) {

    }


    @Override
    public void onDoubleTap(SKScreenPoint point) {
        // zoom in on a position when double tapping
        mapView.zoomInAt(point);
    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onLongPress(SKScreenPoint point) {
        SKCoordinate poiCoordinates = mapView.pointToCoordinate(point);
        final SKSearchResult place = SKReverseGeocoderManager
                .getInstance().reverseGeocodePosition(poiCoordinates);

        boolean selectPoint = isStartPointBtnPressed || isEndPointBtnPressed || isViaPointSelected;
        if (poiCoordinates != null && place != null && selectPoint) {
            SKAnnotation annotation = new SKAnnotation(GREEN_PIN_ICON_ID);
            if (isStartPointBtnPressed) {
                annotation.setUniqueID(GREEN_PIN_ICON_ID);
                annotation
                        .setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
                startPoint = place.getLocation();
            } else if (isEndPointBtnPressed) {
                annotation.setUniqueID(RED_PIN_ICON_ID);
                annotation
                        .setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
                destinationPoint = place.getLocation();
            } else if (isViaPointSelected) {
                annotation.setUniqueID(VIA_POINT_ICON_ID);
                annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
                viaPoint = new SKViaPoint(VIA_POINT_ICON_ID, place.getLocation());
                findViewById(R.id.clear_via_point_button).setVisibility(View.VISIBLE);
            }

            annotation.setLocation(place.getLocation());
            annotation.setMininumZoomLevel(5);
            mapView.addAnnotation(annotation,
                    SKAnimationSettings.ANIMATION_NONE);
        }

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
    public void onMapRegionChanged(SKCoordinateRegion mapRegion) {
    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onScreenOrientationChanged() {

    }

    @Override
    public void onSingleTap(SKScreenPoint point) {
        mapPopup.setVisibility(View.GONE);
    }


    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onInternationalisationCalled(int result) {

    }

    @Override
    public void onDestinationReached() {
        Toast.makeText(MapActivity.this, "Destination reached", Toast.LENGTH_SHORT).show();
        // clear the map when reaching destination
        clearMap();
    }


    @Override
    public void onFreeDriveUpdated(String countryCode, String streetName, SKNavigationState.SKStreetType streetType,
                                   double currentSpeed,
                                   double speedLimit) {

    }

    @Override
    public void onReRoutingStarted() {

    }

    @Override
    public void onSpeedExceededWithAudioFiles(String[] adviceList, boolean speedExceeded) {

    }

    @Override
    public void onUpdateNavigationState(SKNavigationState navigationState) {
    }


    @Override
    public void onVisualAdviceChanged(boolean firstVisualAdviceChanged, boolean secondVisualAdviceChanged,
                                      SKNavigationState navigationState) {
    }

    @Override
    public void onRealReachCalculationCompleted(SKBoundingBox bbox) {
        // fit the reachable area on the screen when real reach calculataion
        // ends
        mapView.fitRealReachInView(bbox, false, 0);
    }


    @Override
    public void onPOIClusterSelected(SKPOICluster poiCluster) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTunnelEvent(boolean tunnelEntered) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion mapRegion) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion mapRegion) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapVersionSet(int newVersion) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNewVersionDetected(final int newVersion) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
        alertDialog.setMessage("New map version available");
        alertDialog.setCancelable(true);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.update_label),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        SKVersioningManager manager = SKVersioningManager.getInstance();
                        boolean updated = manager.updateMapsVersion(newVersion);
                        if (updated) {
                            Toast.makeText(MapActivity.this,
                                    "The map has been updated to version " + newVersion, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(MapActivity.this, "An error occurred in updating the map ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel_label),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        alertDialog.cancel();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onNoNewVersionDetected() {
        Toast.makeText(MapActivity.this, "No new versions were detected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVersionFileDownloadTimeout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCurrentPositionSelected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onObjectSelected(int id) {
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (menu.getVisibility() == View.VISIBLE) {
            menu.setVisibility(View.GONE);
        } else if (skToolsNavigationInProgress || skToolsRouteCalculated) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
            alert.setTitle("Really quit?");
            alert.setMessage("Do you want to exit navigation?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    if (skToolsNavigationInProgress) {
                        navigationManager.stopNavigation();
                    } else {
                        navigationManager.removeRouteCalculationScreen();
                    }
                    initializeNavigationUI(false);
                    skToolsRouteCalculated = false;
                    skToolsNavigationInProgress = false;
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
            alert.setTitle("Really quit? ");
            alert.setMessage("Do you really want to exit the app?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    if (ResourceDownloadsListActivity.mapsDAO != null) {
                        SKToolsDownloadManager downloadManager = SKToolsDownloadManager.getInstance(new SKToolsDownloadListener() {
                            @Override
                            public void onDownloadProgress(SKToolsDownloadItem currentDownloadItem) {

                            }

                            @Override
                            public void onDownloadCancelled(String currentDownloadItemCode) {

                            }

                            @Override
                            public void onDownloadPaused(SKToolsDownloadItem currentDownloadItem) {
                                MapDownloadResource mapResource = (MapDownloadResource) ResourceDownloadsListActivity
                                        .allMapResources.get(currentDownloadItem.getItemCode());
                                mapResource.setDownloadState(currentDownloadItem.getDownloadState());
                                mapResource.setNoDownloadedBytes(currentDownloadItem.getNoDownloadedBytes());
                                ResourceDownloadsListActivity.mapsDAO.updateMapResource(mapResource);
                                app.getAppPrefs().saveDownloadStepPreference(currentDownloadItem.getCurrentStepIndex());
                                finish();
                            }

                            @Override
                            public void onInternetConnectionFailed(SKToolsDownloadItem currentDownloadItem,
                                                                   boolean responseReceivedFromServer) {

                            }

                            @Override
                            public void onAllDownloadsCancelled() {

                            }

                            @Override
                            public void onNotEnoughMemoryOnCurrentStorage(SKToolsDownloadItem currentDownloadItem) {

                            }

                            @Override
                            public void onInstallStarted(SKToolsDownloadItem currentInstallingItem) {

                            }

                            @Override
                            public void onInstallFinished(SKToolsDownloadItem currentInstallingItem) {

                            }
                        });
                        if (downloadManager.isDownloadProcessRunning()) {
                            // pause downloads when exiting app if one is currently in progress
                            downloadManager.pauseDownloadThread();
                            return;
                        }
                    }
                    finish();
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();

        }

    }

    @Override
    public void onRouteCalculationCompleted(final SKRouteInfo routeInfo) {
        if (currentMapOption == MapOption.ALTERNATIVE_ROUTES) {
            int routeIndex = routeIds.size();
            routeIds.add(routeInfo.getRouteID());
            altRoutesButtons[routeIndex].setText(DemoUtils.formatDistance(routeInfo.getDistance()) + "\n"
                    + DemoUtils.formatTime(routeInfo.getEstimatedTime()));
            if (routeIndex == 0) {
                // select 1st alternative by default
                selectAlternativeRoute(0);
            }
        } else if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION || currentMapOption == MapOption.POI_TRACKING
                || currentMapOption == MapOption.NAVI_UI) {
            // select the current route (on which navigation will run)
            SKRouteManager.getInstance().setCurrentRouteByUniqueId(routeInfo.getRouteID());
            // zoom to the current route
            SKRouteManager.getInstance().zoomToRoute(1, 1, 8, 8, 8, 8);

            if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION) {
                bottomButton.setText(getResources().getString(R.string.start_navigation));
            }
        } else if (currentMapOption == MapOption.TRACKS) {
            SKRouteManager.getInstance().zoomToRoute(1, 1, 8, 8, 8, 8);
            bottomButton.setVisibility(View.VISIBLE);
            bottomButton.setText(getResources().getString(R.string.start_navigation));
        }
    }

    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode arg0) {
        Toast.makeText(MapActivity.this, getResources().getString(R.string.route_calculation_failed),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSignalNewAdviceWithAudioFiles(String[] audioFiles, boolean specialSoundFile) {
        // a new navigation advice was received
        SKLogging.writeLog(TAG, " onSignalNewAdviceWithAudioFiles " + Arrays.asList(audioFiles), Log.DEBUG);
        SKToolsAdvicePlayer.getInstance().playAdvice(audioFiles, SKToolsAdvicePlayer.PRIORITY_NAVIGATION);
    }

    @Override
    public void onSignalNewAdviceWithInstruction(String instruction) {
        SKLogging.writeLog(TAG, " onSignalNewAdviceWithInstruction " + instruction, Log.DEBUG);
        textToSpeechEngine.speak(instruction, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void onSpeedExceededWithInstruction(String instruction, boolean speedExceeded) {
    }

    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onViaPointReached(int index) {
    }

    @Override
    public void onNavigationStarted() {
        skToolsNavigationInProgress = true;
        if (navigationUI.getVisibility() == View.VISIBLE) {
            navigationUI.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNavigationEnded() {
        skToolsRouteCalculated = false;
        skToolsNavigationInProgress = false;
        initializeNavigationUI(false);
    }

    @Override
    public void onRouteCalculationStarted() {
        skToolsRouteCalculated = true;
    }

    @Override
    public void onRouteCalculationCompleted() {

    }


    @Override
    public void onRouteCalculationCanceled() {
        skToolsRouteCalculated = false;
        skToolsNavigationInProgress = false;
        initializeNavigationUI(false);
    }

}
