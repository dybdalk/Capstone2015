package trio.passengr.application;

import android.app.Application;

/**
 * Created by dshurbert on 4/25/15.
 *
 * Base class for those who need to maintain global application state.
 * You can provide your own implementation by specifying its name in your
 * AndroidManifest.xml's tag, which will cause that class to be instantiated
 * for you when the process for your application/package is created.
 */
import com.parse.Parse;
import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // enable local data store for Parse and Initialize.
        // This should not be done in an activity, as it creates errors when the activity is closed
        // with the app still running, and you start the activity again.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "T1TTXPJcPzM830GZeCBIupyVU1cMT4tiqByRVFVt", "akwdXIApXkPOGA50FmcbBnWmFCEGatusbOJ1CQnr");
    }
}
