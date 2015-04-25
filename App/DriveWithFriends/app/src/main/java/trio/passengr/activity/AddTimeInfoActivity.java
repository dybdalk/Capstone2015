package trio.passengr.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.CheckedTextView;

import com.parse.ParseObject;
import com.parse.ParseGeoPoint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;

import trio.passengr.R;

// http://www.tutorialspoint.com/android/android_timepicker_control.htm
public class AddTimeInfoActivity extends Activity {
    // START and END pass current location to MapActivity
    public final static String START_LAT = "trio.passengr.START_LAT";
    public final static String START_LNG = "trio.passengr.START_LNG";
    public final static String END_LAT = "trio.passengr.END_LAT";
    public final static String END_LNG = "trio.passengr.END_LNG";
    public final static String HOUR = "trio.passengr.HOUR";
    public final static String MINUTE = "trio.passengr.MINUTE";

    private String startLat;
    private String startLng;
    private String endLat;
    private String endLng;

    private TimePicker timePicker;
    private TextView time;
    private Calendar calendar;
    private String format = "";

    private CheckedTextView[] checkedDays = new CheckedTextView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_time_info);

        // get route start and end points
        Intent intent = getIntent();
        startLat= intent.getStringExtra(MapActivity.START_LAT);
        startLng = intent.getStringExtra(MapActivity.START_LNG);
        endLat= intent.getStringExtra(MapActivity.END_LAT);
        endLng = intent.getStringExtra(MapActivity.END_LNG);

        timePicker = (TimePicker) findViewById(R.id.timePicker);

        checkedDays[0] = (CheckedTextView) findViewById(R.id.checkedSunday0);
        checkedDays[1] = (CheckedTextView) findViewById(R.id.checkedMonday1);
        checkedDays[2] = (CheckedTextView) findViewById(R.id.checkedTuesday2);
        checkedDays[3] = (CheckedTextView) findViewById(R.id.checkedWednesday3);
        checkedDays[4] = (CheckedTextView) findViewById(R.id.checkedThursday4);
        checkedDays[5] = (CheckedTextView) findViewById(R.id.checkedFriday5);
        checkedDays[6] = (CheckedTextView) findViewById(R.id.checkedSaturday6);

        calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
    }

    public void onDayChecked(View v) {
        CheckedTextView checkable = (CheckedTextView) v;
        if (!checkable.isChecked()) {
            checkable.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
        }
        else {
            checkable.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
        }
        checkable.toggle();
    }

    public void saveTime(View view) {
        int hour = timePicker.getCurrentHour();
        int min = timePicker.getCurrentMinute();
        Intent intent = new Intent(this, ViewRoutesActivity.class);

        intent.putExtra(START_LAT, startLat);
        intent.putExtra(START_LNG, startLng);
        intent.putExtra(END_LAT, endLat);
        intent.putExtra(END_LNG, endLng);
        intent.putExtra(HOUR, hour);
        intent.putExtra(MINUTE, min);

        // create a ParseObject to be posted to the server. these objects are
        // just rows in a table.
        ParseObject route = new ParseObject("Route");

        ParseGeoPoint startGeoPoint = new ParseGeoPoint(Double.parseDouble(startLat), Double.parseDouble(startLng));
        ParseGeoPoint endGeoPoint = new ParseGeoPoint(Double.parseDouble(endLat), Double.parseDouble(endLng));
        route.put("endPoint", endGeoPoint);
        route.put("startPoint", startGeoPoint);

        // read time input from user and add to our ParseObject
        JSONArray time = new JSONArray();
        time.put(hour);
        time.put(min);
        route.put("timeOfRoute", time);
        route.put("userId", "1");

        // read days of the week input from user, add the array to our route ParseObject
        String[] textDays = new String[7];
        textDays[0] = "sunday";
        textDays[1] = "monday";
        textDays[2] = "tuesday";
        textDays[3] = "wednesday";
        textDays[4] = "thursday";
        textDays[5] = "friday";
        textDays[6] = "saturday";
        JSONArray myDays = new JSONArray();
        for (int i=0; i<7; i++){
            if (checkedDays[i].isChecked()) {
                myDays.put(textDays[i]);
            }
        }
        route.put("daysOfWeek",myDays);

        // post our route object to the server
        route.saveInBackground();



        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_time_info, menu);
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



}
