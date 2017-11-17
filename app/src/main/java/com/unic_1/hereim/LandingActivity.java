package com.unic_1.hereim;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Adapter.NotificationAdapter;
import com.unic_1.hereim.Adapter.RequestAdapter;
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

    ///
    public static Location location;
    private static String sNumber;
    private final int STATUS_INQUIRE_LOCATION = 0;
    private final int STATUS_SEND_LOCATION = 1;
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private final long MIN_TIME_BW_UPDATES = 1000; // 1 sec
    private final String TAG = "LANDING_ACTIVITY";
    private int alertStatus;
    ///
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mProgressDialog = new ProgressDialog(LandingActivity.this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        // Gets the users phone number
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        sNumber = preferences.getString("number", "");

        Log.i(TAG, "logged in with " + sNumber);

        settingUpLocation();

        initializeNotification();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checks if the permission is granted or not
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                Toast.makeText(LandingActivity.this, "Location request granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Reads a number, send's request asking for location
    public void askLocation(View view) {
        alertStatus = STATUS_INQUIRE_LOCATION;
        createDialog();
    }

    // Reads a number, send's own location to the other number
    public void sendLocation(View view) {
        alertStatus = STATUS_SEND_LOCATION;
        createDialog();
    }

    // Creating dialog to send location request to other person
    public void createDialog() {
        alertDialogBuilder = new AlertDialog.Builder(LandingActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        final EditText sendNumber = (EditText) view.findViewById(R.id.etSendNumber);
        Button bSend = (Button) view.findViewById(R.id.bSendDialog);
        Button bCancel = (Button) view.findViewById(R.id.bCancelDialog);

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Pushing location...");

                long timestamp = new Date().getTime();
                Request req = null;

                // Asking location
                if (alertStatus == STATUS_INQUIRE_LOCATION) {
                    req = new Request(
                            timestamp,
                            sNumber,
                            sendNumber.getText().toString()
                    );
                }
                // Sending location
                else if (alertStatus == STATUS_SEND_LOCATION) {
                    req = new Request(
                            timestamp,
                            sNumber,
                            sendNumber.getText().toString(),
                            new LocationCoordinates(
                                    location.getLatitude(),
                                    location.getLongitude()
                            )
                    );
                }

                new RequestAdapter().addData(req, sendNumber.getText().toString(), sNumber, alertStatus);
                Log.d(TAG, "Dialog onClick: Completed");
                alertDialog.cancel();
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // TODO: 17/11/17 Learn from this method
    @Deprecated
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
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                //return;
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

    // Sets up Location manager
    private void settingUpLocation() {
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

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "setupLocation: Location disabled");
            showGPSDisabledAlertToUser();
        } else {
            // FIXME: 3/10/17 last known location is only retrived when you pinpoint yourself on google maps since previous locations are being erased when you reinstall the app

///
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(getApplicationContext(), "No provider found!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "setupLocation: enabled");
                if (isGPSEnabled) {
                    // If build version is less than sdk 23
                    if (Build.VERSION.SDK_INT < 23) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    } else {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        }
    }


    @Deprecated
    private void initNotification() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(sNumber).child("request_list");

        final DatabaseReference requestReference = database.getReference("Request");

        final RecyclerView.Adapter adapter = new NotificationAdapter(requestList, this);
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
                        final JSONObject object = new JSONObject(data.getValue().toString());
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

                                    /*
                                    * Before UserRequestReference was added to Request class as data member
                                    * This code works fine
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


                                    }*/

                                    // After adding UserRequestReference as data member in Request class
                                    if (action == Constant.Actions.REQUEST_SENT.value || action == Constant.Actions.REQUEST_RECEIVED.value) {
                                        Log.i(TAG, "onDataChange: Request Received/ Request Sent");
                                        requestList.add(
                                                new Request(
                                                        new UserRequestReference(
                                                                (action == Constant.Actions.REQUEST_SENT.value) ? Constant.Actions.REQUEST_SENT.value : Constant.Actions.REQUEST_RECEIVED.value,
                                                                object.getString("request_reference")
                                                        ),
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
                                                        new UserRequestReference(
                                                                (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT.value : Constant.Actions.LOCATION_RECEIVED.value,
                                                                object.getString("request_reference")
                                                        ),
                                                        requestData.getLong("timestamp"),
                                                        requestData.getString("to"),
                                                        requestData.getString("from"),
                                                        locationCoordinates
                                                )
                                        );
                                    } else {
                                        requestList.add(
                                                new Request(
                                                        new UserRequestReference(
                                                                Constant.Actions.REQEUST_DECLINED.value,
                                                                object.getString("request_reference")
                                                        ),
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

        Log.i(TAG, "initNotification: list length " + requestList.size());
    }

    // Initialized notification
    private void initializeNotification() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(sNumber).child("request_list");

        final DatabaseReference requestReference = database.getReference("Request");

        final RecyclerView.Adapter adapter = new NotificationAdapter(requestList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildAdded: " + dataSnapshot + ":" + dataSnapshot.child("request_reference").getValue().toString() + " : " + dataSnapshot.child("action").getValue().toString());
                // Parsing the user request list using JSON object
                final int action = new Integer(dataSnapshot.child("action").getValue().toString());
                // Referencing a particular request
                final DatabaseReference request = requestReference.child(dataSnapshot.child("request_reference").getValue().toString());
                request.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        Log.i(TAG, "onDataChange: request");
                        try {
                            Log.i(TAG, "onDataChange: request: " + dataSnapshot1.getValue());
                            // Parsing request data using JSON object
                            JSONObject requestData = new JSONObject(dataSnapshot1.getValue().toString());

                            // After adding UserRequestReference as data member in Request class
                            if (action == Constant.Actions.REQUEST_SENT.value || action == Constant.Actions.REQUEST_RECEIVED.value) {
                                Log.i(TAG, "onDataChange: Request Received/ Request Sent");
                                requestList.add(
                                        new Request(
                                                new UserRequestReference(
                                                        (action == Constant.Actions.REQUEST_SENT.value) ? Constant.Actions.REQUEST_SENT.value : Constant.Actions.REQUEST_RECEIVED.value,
                                                        dataSnapshot.child("request_reference").getValue().toString()
                                                ),
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
                                                new UserRequestReference(
                                                        (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT.value : Constant.Actions.LOCATION_RECEIVED.value,
                                                        dataSnapshot.child("request_reference").getValue().toString()
                                                ),
                                                requestData.getLong("timestamp"),
                                                requestData.getString("to"),
                                                requestData.getString("from"),
                                                locationCoordinates
                                        )
                                );
                            } else {
                                requestList.add(
                                        new Request(
                                                new UserRequestReference(
                                                        Constant.Actions.REQEUST_DECLINED.value,
                                                        dataSnapshot.child("request_reference").getValue().toString()
                                                ),
                                                requestData.getLong("timestamp"),
                                                requestData.getString("to"),
                                                requestData.getString("from")
                                        )
                                );


                            }
                            Log.i(TAG, "list size: "+requestList.size());
                            if(mProgressDialog.isShowing()) {
                                mProgressDialog.hide();
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
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged: ");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildRemoved: ");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildMoved: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: ");
            }
        });

        Log.i(TAG, "initNotification: list length " + requestList.size());
    }

    // GPS Disabled alert
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                        //Settings.ACTION_NETWORK_OPERATOR_SETTINGS

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
