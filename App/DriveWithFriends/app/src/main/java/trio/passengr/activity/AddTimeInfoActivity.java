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
