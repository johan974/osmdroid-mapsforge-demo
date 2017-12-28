package nl.deholtmans.osmdroidmapsforgedemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.security.AccessController.getContext;

/*
 * ERROR:
 *
 * I/OsmDroid: Error downloading tile: /9/55/139
            java.lang.NoSuchFieldError: No field DEBUG_TILE_PROVIDERS of type Z in class Lorg/osmdroid/tileprovider/constants/OpenStreetMapTileProviderConstants; or its superclasses (declaration of 'org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants' appears in /data/app/nl.deholtmans.osmdroidmapsforgedemo-2/split_lib_dependencies_apk.apk)
                at org.osmdroid.mapsforge.MapsForgeTileModuleProvider$TileLoader.loadTile(MapsForgeTileModuleProvider.java:92)
                at org.osmdroid.tileprovider.modules.MapTileModuleProviderBase$TileLoader.run(MapTileModuleProviderBase.java:297)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
                at java.lang.Thread.run(Thread.java:761)
 */

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_SETTING = 99;
    private static final int RESULT_PARAMS_SHOW_MAP = 11;
    private static final String[] PARAMS_SHOW_MAP = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private MapController mapController;
    private MapView mapView;
    MapsForgeTileSource fromFiles = null;
    MapsForgeTileProvider forge = null;
    AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't set the content view ... because Osmdroid will immediately try to setup a cache.db.
        //   Without the right Manifest and RUNTIME permissions, this will result in an error.
        checkPermissionForShowingTheMap();
    }
    /* ONLINE EXAMPLE: works
    private void selectAndShowOnlineMap() {
        setContentView( R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(17);
        GeoPoint startPoint = new GeoPoint(52.2222, 6.6123);
        mapController.setCenter(startPoint);
    }*/

    private void selectAndShowMapsforgeFile() {
        // Build maps context AFTER getting the Manifest & Runtime permissions.
        // 1 - Set the VIEW context: mapview, toolbar and FAB
        setContentView( R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mapView = (MapView) findViewById(R.id.mapview);
        // Interactively select a Mapsforge file
        new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                if( file == null) {
                    return;
                }
                // Show the file
                showMapsforgeFile(file);
            }
        }).showDialog();
    }
    private void showMapsforgeFile( File mapFile) {
        File[] maps = new File[1];
        maps[0] = mapFile;
        MapsForgeTileSource.createInstance( this.getApplication());
        XmlRenderTheme theme = null;
        try {
            theme = new AssetsRenderTheme(this.getApplicationContext(), "renderthemes/", "rendertheme-v4.xml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        fromFiles = MapsForgeTileSource.createFromFiles(maps, theme, "rendertheme-v4");
        forge = new MapsForgeTileProvider(
            new SimpleRegisterReceiver(this),
            fromFiles, null);


        mapView.setTileProvider(forge);


        //now for a magic trick
        //since we have no idea what will be on the
        //user's device and what geographic area it is, this will attempt to center the map
        //on whatever the map data provides
        mapView.getController().setZoom(fromFiles.getMinimumZoomLevel());
        mapView.zoomToBoundingBox(fromFiles.getBoundsOsmdroid(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionForShowingTheMap() {
        // Step 1 - check if you gave already sufficient permissions: checkSelfPermission()
        if ( alreadyGrantedPermissionToShowMap()) {
            Toast.makeText(this, "You already gave permissions ... ", Toast.LENGTH_SHORT).show();
            selectAndShowMapsforgeFile();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                   ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Should we show an explanation?
            Toast.makeText(this, "Further explanation ... You should give permission to get show the map", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, netPermisssion( PARAMS_SHOW_MAP), RESULT_PARAMS_SHOW_MAP);
        } else {
            // No explanation needed, we can request the permission.
            Toast.makeText(this, "No explanation needed ...  ", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, netPermisssion( PARAMS_SHOW_MAP), RESULT_PARAMS_SHOW_MAP);
        }
    }

    private String[] netPermisssion(String[] wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String permission : wantedPermissions) {
            if (!hasPermission(permission)) {
                result.add(permission);
            }
        }
        return (result.toArray(new String[result.size()]));
    }

    private boolean alreadyGrantedPermissionToShowMap() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }
    private boolean hasPermission(String permissionString) {
        return (ContextCompat.checkSelfPermission(this, permissionString) == PackageManager.PERMISSION_GRANTED);
    }
    // When your app requests permissions, the system presents a dialog box to the user. When the user responds, the system
    // invokes your app's onRequestPermissionsResult() method, passing it the user response.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RESULT_PARAMS_SHOW_MAP) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "OK, thank you, showing the map ... ", Toast.LENGTH_SHORT).show();
                // permission was granted, yay! Do the contacts-related task you need to do.
                selectAndShowMapsforgeFile();
            } else {
                Toast.makeText(this, "No permissions granted, no maps! ", Toast.LENGTH_SHORT).show();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }
        // other 'case' lines to check for other permissions this app might request
    }
}
