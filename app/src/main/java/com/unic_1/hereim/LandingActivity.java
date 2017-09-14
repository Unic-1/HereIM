package com.unic_1.hereim;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;

import java.util.Date;

public class LandingActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static Location location;

    private final String TAG = "LANDING_ACTIVITY";
    private String number;

    AlertDialog.Builder alertDialog;

    /*Gets triggered when ask button is clicked*/
    public void ask(View view) {
        // Dialogue pop's up
        alertDialog.show();
    }

    /*Gets triggered when answer button is clicked*/
    public void answer(View view) {
        pushLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        createDialog();

        // Gets the users phone number
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        number = preferences.getString("number", "");

        Log.i(TAG, "onCreate: "+number);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Listens to the location
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Updates the location variable to the latest location
                LandingActivity.location = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Sets the last known location as default value
            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

            // Requests for any change in location
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

            pushLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.Notification) {
            Intent i = new Intent(this, NotificationActivity.class);

            startActivity(i);
        } else if (item.getItemId() == R.id.map) {
            Intent i = new Intent(this, MapsActivity.class);

            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checks if the permission is granted or not
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    // Creating dialog to send location request to other person
    public void createDialog() {
        alertDialog = new AlertDialog.Builder(LandingActivity.this);
        alertDialog.setTitle("Request");
        alertDialog.setMessage("Enter phone number:");

        final EditText input = new EditText(LandingActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_menu_gallery);

        alertDialog.setPositiveButton("Send Request",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(LandingActivity.this, "Pushing", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Dialog onClick: Pushing...");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("Request");

                        long timestamp = new Date().getTime();

                        // Creates a unique key to store the request
                        DatabaseReference user1_ref = reference.child(number).push();
                        Request req1 = new Request(Constant.Actions.REQUEST_SENT.value, timestamp, input.getText().toString());
                        user1_ref.setValue(req1);

                        // Same unique key is used to store the request
                        DatabaseReference user2_ref = reference.child(input.getText().toString()).child(user1_ref.getKey());
                        Request req2 = new Request(Constant.Actions.REQUEST_RECEIVED.value, timestamp, number);
                        user2_ref.setValue(req2);

                        Log.d(TAG, "Dialog onClick: Completed");
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
    }

    public void pushLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users").child(number);

        double lat = 0;
        double lon = 0;
        // If system finds last known location
        if(location != null) {
            lat = LandingActivity.location.getLatitude();
            lon = LandingActivity.location.getLongitude();
        }

        System.out.println("Latitude: "+lat+" Longitude: "+lon);
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();

        // Stores the coordinates in an object
        LocationCoordinates locationCoordinates = new LocationCoordinates(lat, lon);

        ref.setValue(locationCoordinates);
    }
}
