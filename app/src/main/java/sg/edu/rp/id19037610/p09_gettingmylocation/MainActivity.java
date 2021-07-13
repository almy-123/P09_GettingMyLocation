package sg.edu.rp.id19037610.p09_gettingmylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnGetLocationUpdate, btnRemoveLocationUpdate, btnCheckRecords;
    TextView tvLastKnown;
    GoogleMap map;
    FusedLocationProviderClient client;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    ArrayList<String> locations;
    String folderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLastKnown = findViewById(R.id.tvLastKnown);
        btnGetLocationUpdate = findViewById(R.id.btnGetLocationUpdate);
        btnRemoveLocationUpdate = findViewById(R.id.btnRemoveLocationUpdate);
        btnCheckRecords = findViewById(R.id.btnCheckRecords);
        client = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback();
        locations = new ArrayList<String>();
        folderLocation = getFilesDir().getAbsolutePath() + "/MyLocations";
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        if (checkPermission()) {
            setLastKnownLocation();
        }

        // Create
        File folder = new File(folderLocation);
        if (!folder.exists()) {
            boolean result = folder.mkdir();
            if (result) {
                Log.d("My Locations", "Folder created");
            }
        }

        btnGetLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    getLocationUpdate();
                    writeToFile();
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        });

        btnRemoveLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.removeLocationUpdates(mLocationCallback);
                setLastKnownLocation();
            }
        });

        btnCheckRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CheckRecords.class);
                startActivity(intent);
            }
        });
    }

    private void getLocationUpdate() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(500);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();

                    String msg = "New Location\n"
                            + "Lat: " + String.valueOf(lat) + ", "
                            + "Lng: " + String.valueOf(lng);
                    locations.add(lat + ", " + lng);

                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    setLastKnownLocation();
                }
            }
        };
    }

    private void setLastKnownLocation() {
        if (checkPermission()) {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    String msg = "";
                    if (location != null) {
                        msg = "Last known location: \n"
                                + "Latitude: " + location.getLatitude()
                                + "\nLongitude: " + location.getLongitude();

                        if (map != null) {
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker marker = map.addMarker(new
                                    MarkerOptions()
                                    .position(loc)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                        }
                    } else {
                        msg = "No Last Known Location found";
                    }
                    tvLastKnown.setText(msg);
                }
            });
        }

    }

    private void writeToFile() {
        // Write
        try {
            File targetFile = new File(folderLocation, "locations.txt");
            FileWriter writer = new FileWriter(targetFile, true);
            for (int i = 0; i < locations.size(); i++) {
                writer.write(locations.get(i) + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to write", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private boolean checkPermission() {
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkPermission()) {
            setLastKnownLocation();
        }
    }
}