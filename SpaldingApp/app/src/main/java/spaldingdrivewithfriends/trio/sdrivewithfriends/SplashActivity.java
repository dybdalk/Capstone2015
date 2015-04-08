package spaldingdrivewithfriends.trio.sdrivewithfriends;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.widget.Toast;

import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by chris spalding on 4/7/15.
 * NOTE: Added this class to install required
 * resources from assets/MapResources.zip.
 */
public class SplashActivity extends Activity implements SKPrepareMapTextureListener, SKMapUpdateListener
{
    /**
     * Path to the MapResources directory
     * NOTE: From Android demo
     */
    public static String mapResourcesDirPath = "";


    protected void onCreate(Bundle savedInstanceState)
    {
        if (!new File(mapResourcesDirPath).exists()) {
            // if map resources are not already present copy them to
            // mapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(this, mapResourcesDirPath, "SKMaps.zip", this).start();
            // copy some other resource needed
            copyOtherResources();
            //prepareMapCreatorFile();
        } else {
            // map resources have already been copied - start the map activity
            Toast.makeText(SplashActivity.this, "Map resources copied in a previous run", Toast.LENGTH_SHORT).show();
            //prepareMapCreatorFile();
            //DemoUtils.initializeLibrary(this);
            SKVersioningManager.getInstance().setMapUpdateListener(this);
            finish();
            startActivity(new Intent(this, MapActivity.class));
        }
    }

    @Override
    public void onNewVersionDetected(int i) {

    }

    @Override
    public void onMapVersionSet(int i) {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    @Override
    public void onNoNewVersionDetected() {

    }

    @Override
    public void onMapTexturesPrepared(boolean b) {

    }


    /**
     * Copy some additional resources from assets
     */
    private void copyOtherResources() {
        new Thread() {

            public void run() {
                //try {
                    String tracksPath = mapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    if (!tracksDir.exists()) {
                        tracksDir.mkdirs();
                    }
                    //DemoUtils.copyAssetsToFolder(getAssets(), "GPXTracks", mapResourcesDirPath + "GPXTracks");

                    String imagesPath = mapResourcesDirPath + "images";
                    File imagesDir = new File(imagesPath);
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    //DemoUtils.copyAssetsToFolder(getAssets(), "images", mapResourcesDirPath + "images");
                //} catch (IOException e) {
                   // e.printStackTrace();
                }
           // }
        }.start();
    }


    //TODO make a copyOtherResources method as well as a DemoUtils class for our purposes.
}
