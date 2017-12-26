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
                at java.lang.Thread.run(Thr
 */

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_SETTING = 99;
    private static final int RESULT_PARAMS_SHOW_MAP = 11;
    private static final String[] PARAMS_TAKE_PHOTO = {
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
        checkPermissionForShowingTheMap();
    }
    /* ONLINE EXAMPLE: private void selectAndShowMapsforgeFile() {
        // ONLINE EXAMPLE ... can only move forward after having all permissions granted
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
        // Build maps context
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
        Toast.makeText(this, "You can take PHOTO", Toast.LENGTH_SHORT).show();
        // Get the file
        new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                // Show the file
                showMapsforgeFile(file);
            }
        }).showDialog();
    }
    private void showMapsforgeFile( File mapFile) {
        File[] files = new File[1];
        files[0] = mapFile;
        MapsForgeTileSource.createInstance( this.getApplication());
        fromFiles = MapsForgeTileSource.createFromFiles( files);
        forge = new MapsForgeTileProvider( new SimpleRegisterReceiver( getBaseContext()), fromFiles);
        mapView.setTileProvider(forge);
        mapView.getController().setZoom( 9);
        GeoPoint startPoint = new GeoPoint(52.2222, 36.6123);
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
        if ( alreadyGrantedPermissionToShowMap()) {
            Toast.makeText(this, "You can show the map", Toast.LENGTH_SHORT).show();
            selectAndShowMapsforgeFile();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                   ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "You should give permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, netPermisssion(PARAMS_TAKE_PHOTO), RESULT_PARAMS_SHOW_MAP);
        } else {
            ActivityCompat.requestPermissions(this, netPermisssion(PARAMS_TAKE_PHOTO), RESULT_PARAMS_SHOW_MAP);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RESULT_PARAMS_SHOW_MAP) {
            if (alreadyGrantedPermissionToShowMap()) {
                Toast.makeText(this, "You can take picture", Toast.LENGTH_SHORT).show();
                selectAndShowMapsforgeFile();
            } else if (!(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {

                final AlertDialog.Builder settingDialog = new AlertDialog.Builder( MainActivity.this);
                settingDialog.setTitle("Permissioin");
                settingDialog.setMessage("Now you need to enable permisssion from the setting because without permission this app won't run properly \n\n  goto -> setting -> appInfo");
                settingDialog.setCancelable(false);
                settingDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant all permission ENABLE", Toast.LENGTH_LONG).show();
                    }
                });
                settingDialog.show();
                Toast.makeText(this, "You need to grant permission from setting", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (alreadyGrantedPermissionToShowMap()) {
                selectAndShowMapsforgeFile();
            }
        }
    }
}
