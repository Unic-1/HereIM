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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Adapter.NotificationAdapter;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.Model.UserRequestReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import android.support.v7.widget.ThemedSpinnerAdapter.Helper;

public class LandingActivity extends AppCompatActivity {

    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 2 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 sec
    ///
    public static boolean isGPSEnabled = false;
    public static boolean isNetworkEnabled = false;
    static boolean canGetLocation = false;
    ///
    private static Location location;
    private final String TAG = "LANDING_ACTIVITY";
    AlertDialog.Builder alertDialog;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int REQUEST_ID;
    private String number;
    private int count = 0;

    // Reads a number, send's request asking for location
    public void ask(View view) {
        // Dialogue pop's up
        REQUEST_ID = 0;
        alertDialog.show();
    }

    // Reads a number, send's own location to the other number
    public void answer(View view) {
        REQUEST_ID = 1;
        alertDialog.show();
        //pushLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        createDialog();

        // Gets the users phone number
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        number = preferences.getString("number", "");

        Log.i(TAG, "onCreate: " + number);

        setupLocation();

        initNotification();
    }

    // Sets up Location manager
    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Listens to the location
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Updates the location variable to the latest location
                LandingActivity.location = location;
                Log.i(TAG, "onLocationChanged: " + location);
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

        // If build version is less than sdk 23
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.i(TAG, "setupLocation: version<23");
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "setupLocation: Location disabled");
                showGPSDisabledAlertToUser();
            }

            {
                // FIXME: 3/10/17 last known location is only retrived when you pinpoint yourself on google maps since previous locations are being erased when you reinstall the app

///
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "No provider found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "setupLocation: enabled");
                    if (isGPSEnabled) {
                        if (location == null) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                            Log.d("Network", "GPS");
                            if (locationManager != null) {
                                Log.d("locationManager", "is not null ");

                                //*
                                List<String> providers = locationManager.getProviders(true);
                                Location bestLocation = null;
                                for (String provider : providers) {
                                    location = locationManager.getLastKnownLocation(provider);
                                    if (location == null) {
                                        continue;
                                    }
                                    break;
                                }

                                //*

                                //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    Log.d("Network", "GPS lat and long gets ");
                                    Toast.makeText(getApplicationContext(), "isNetworkEnabled!", Toast.LENGTH_SHORT).show();
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    Log.d(TAG, "setupLocation: " + latitude + ", " + longitude);

                                /*Helper.savePreferences(getApplicationContext(), "LATITUDE",String.valueOf(latitude));
                                Helper.savePreferences(getApplicationContext(), "LONGITUDE",String.valueOf(longitude));*/
                                } else {
                                    Log.d("location", "is getting null ");
                                }
                            }
                        }
                    } else if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                //Toast.makeText(getApplicationContext(), "isGPSEnabled!", Toast.LENGTH_SHORT).show();
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                            /*Helper.savePreferences(getApplicationContext(), NameConversion.LATITUDE,String.valueOf(latitude));
                            Helper.savePreferences(getApplicationContext(), NameConversion.LONGITUDE,String.valueOf(longitude));*/
                            }
                        }
                    } else {
                        Log.d(TAG, "setupLocation: Nothing enabled");
                    }
                }
            }


///


            // Requests for any change in location
            // locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

            // Sets the last known location as default value
            //location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Log.i(TAG, "setupLocation: version>23");

                // Requests for any change in location
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                // Sets the last known location as default value
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                //pushLocation();
            }
        }
    }

    // Initialized notification
    private void initNotification() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(preferences.getString("number", "")).child("request_list");

        final DatabaseReference requestReference = database.getReference("Request");

        final RecyclerView.Adapter adapter = new NotificationAdapter(requestList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);




        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: ");
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.i(TAG, "onDataChange: data: " + data.getValue());
                    try {
                        // Parsing the user request list using JSON object
                        JSONObject object = new JSONObject(data.getValue().toString());
                        final int action = new Integer(object.get("action").toString());

                        // Referencing a particular request
                        final DatabaseReference request = requestReference.child(object.get("request_reference").toString());
                        request.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i(TAG, "onDataChange: request");
                                try {
                                    Log.i(TAG, "onDataChange: request: " + dataSnapshot.getValue());
                                    // Parsing request data using JSON object
                                    JSONObject requestData = new JSONObject(dataSnapshot.getValue().toString());

                                    if (action == Constant.Actions.REQUEST_SENT.value || action == Constant.Actions.REQUEST_RECEIVED.value) {
                                        Log.i(TAG, "onDataChange: Request Received/ Request Sent");
                                        requestList.add(
                                                new Request(
                                                        (action == Constant.Actions.REQUEST_SENT.value) ? Constant.Actions.REQUEST_SENT : Constant.Actions.REQUEST_RECEIVED,
                                                        requestData.getLong("timestamp"),
                                                        requestData.getString("to"),
                                                        requestData.getString("from")
                                                )
                                        );
                                    } else if (action == Constant.Actions.LOCATION_SENT.value || action == Constant.Actions.LOCATION_RECEIVED.value) {
                                        JSONObject obj = new JSONObject(requestData.get("location").toString());
                                        LocationCoordinates locationCoordinates = new LocationCoordinates(
                                                obj.getDouble("latitude"),
                                                obj.getDouble("longitude")
                                        );

                                        requestList.add(
                                                new Request(
                                                        (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT : Constant.Actions.LOCATION_RECEIVED,
                                                        requestData.getLong("timestamp"),
                                                        requestData.getString("to"),
                                                        requestData.getString("from"),
                                                        locationCoordinates
                                                )
                                        );
                                    } else {
                                        requestList.add(
                                                new Request(
                                                        Constant.Actions.REQEUST_DECLINED,
                                                        requestData.getLong("timestamp"),
                                                        requestData.getString("to"),
                                                        requestData.getString("from")
                                                )
                                        );


                                    }
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "onDataChange: data received");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //requestList.notifyAll();
//                    requestList.notifyAll();
                }
                Log.i(TAG, "onDataChange: for ended");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: " + databaseError);
            }
        });


        //ArrayList<String> requestList = new RequestThread().execute(preferences.getString("number", "")).get();
        //ArrayList<Request> requestList = new RequestThread().execute(preferences.getString("number", "")).get();

        Log.i(TAG, "initNotification: list length " + requestList.size());

        /*final Handler handler = new Handler();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                //code you want to run every second
                //Log.i(TAG, "run: "+requestList.size());
                if (requestList.size() > count) {
                    adapter.notifyDataSetChanged();
                    count = requestList.size();
                }
                if (true) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(task, 1000);*/


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
                        // Creates unique id for every request
                        DatabaseReference reference = database.getReference("Request").push();
                        // Gets Users reference
                        DatabaseReference userReference = database.getReference("Users");
                        long timestamp = new Date().getTime();

                        DatabaseReference user1_ref = userReference.child(number).child("request_list").push();
                        DatabaseReference user2_ref = userReference.child(input.getText().toString()).child("request_list").push();

                        Request req = null;

                        // Asking location
                        if (REQUEST_ID == 0) {
                            req = new Request(
                                    timestamp,
                                    input.getText().toString(),
                                    number
                            );

                            // Updates the request list of user
                            user1_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_SENT.value, reference.getKey()));
                            user2_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_RECEIVED.value, reference.getKey()));
                        }
                        // Sending location
                        else if (REQUEST_ID == 1) {
                            req = new Request(
                                    timestamp,
                                    input.getText().toString(),
                                    number,
                                    new LocationCoordinates(
                                            location.getLatitude(),
                                            location.getLongitude()
                                    )
                            );

                            // Updates the request list of user
                            user1_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_SENT.value, reference.getKey()));
                            user2_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_RECEIVED.value, reference.getKey()));
                        }
                        reference.setValue(req);

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

    // Creates Location object and pushes to firebase
    public void pushLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users").child(number);

        double lat = 0;
        double lon = 0;
        // If system finds last known location
        if (location != null) {
            lat = LandingActivity.location.getLatitude();
            lon = LandingActivity.location.getLongitude();
        }

        System.out.println("Latitude: " + lat + " Longitude: " + lon);
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();

        // Stores the coordinates in an object
        LocationCoordinates locationCoordinates = new LocationCoordinates(lat, lon);

        ref.setValue(locationCoordinates);
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                        //mapFrag.getMapAsync(LandingActivity.this);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
