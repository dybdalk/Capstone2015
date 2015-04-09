package com.skobbler.sdkdemo.application;

import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.skobbler.sdkdemo.database.DownloadResource;

/**
 * Created by Tudor on 12/5/2014.
 */
public class ApplicationPreferences {

    public static final String DOWNLOAD_STEP_INDEX_PREF_KEY = "downloadStepIndex";

    public static final String DOWNLOAD_QUEUE_PREF_KEY = "downloadQueue";

    /**
     * preference name
     */
    public static final String PREFS_NAME = "demoAppPrefs";

    /**
     * used for modifying values in a SharedPreferences prefs
     */
    private SharedPreferences.Editor prefsEditor;

    /**
     * reference to preference
     */
    private SharedPreferences prefs;

    /**
     * the context
     */
    private Context context;

    public ApplicationPreferences(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    public int getIntPreference(String key) {
        return prefs.getInt(key, 0);
    }

    public String getStringPreference(String key) {
        return prefs.getString(key, "");
    }

    public void saveDownloadStepPreference(int downloadStepIndex) {
        prefsEditor.putInt(DOWNLOAD_STEP_INDEX_PREF_KEY, downloadStepIndex);
        prefsEditor.commit();
    }

    public void saveDownloadQueuePreference(List<DownloadResource> downloads) {
        String[] resourceCodes = new String[downloads.size()];
        for (int i = 0; i < downloads.size(); i++) {
            resourceCodes[i] = downloads.get(i).getCode();
        }
        prefsEditor.putString(DOWNLOAD_QUEUE_PREF_KEY, new Gson().toJson(resourceCodes));
        prefsEditor.commit();
    }
}