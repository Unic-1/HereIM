package com.unic_1.hereim;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Adapter.NotificationAdapter;
import com.unic_1.hereim.Adapter.RequestAdapter;
import com.unic_1.hereim.Adapter.SuggestionAdapter;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Constants.FirebaseConstants;
import com.unic_1.hereim.Database.ContactDataSouce;
import com.unic_1.hereim.Model.ContactModel;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.Model.UserRequestReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LandingActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener {

    public static Location location;
    private static String sNumber;
    private final int CONTACT_REQUEST_CODE = 100;
    private final int LOCATION_REQUEST_CODE = 101;
    private final int STATUS_INQUIRE_LOCATION = 0;
    private final int STATUS_SEND_LOCATION = 1;
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private final long MIN_TIME_BW_UPDATES = 1000; // 1 sec
    private final String TAG = "LANDING_ACTIVITY";
    private int alertStatus;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ProgressDialog mProgressDialog;
    private SetupUsersContact mUsersContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mProgressDialog = new ProgressDialog(LandingActivity.this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (!checkConnectivity()) {
            mProgressDialog.hide();
            showSnack(false);
        }

        // Gets the users phone number
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        sNumber = preferences.getString("number", "");

        mUsersContact = new SetupUsersContact();

        Log.i(TAG, "logged in with " + sNumber);

        settingUpLocation(); // Setup location permission
        initializeNotification(); // Populate the notification data
        setContactPermission(); // Setup contact permission
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: " + requestCode);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                // Checks if the permission is granted or not
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                        Toast.makeText(LandingActivity.this, "Location request granted", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case CONTACT_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        mUsersContact.execute();
                    }
                }
                break;
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
        Button bSend = (Button) view.findViewById(R.id.bSendDialog);
        Button bCancel = (Button) view.findViewById(R.id.bCancelDialog);

        //Setting up AutoCompleteTextView
        final AutoCompleteTextView autoCompleteNumber = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteNumber);
        /*ArrayList<ContactModel> contact = getContacts();
        System.out.println("Contact: " + contact.get(0));
        SuggestionAdapter adapter = new SuggestionAdapter(LandingActivity.this, R.layout.suggestion_item, contact);
        autoCompleteNumber.setAdapter(adapter);*/

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnectivity()) {
                    Log.d(TAG, "Pushing location...");

                    long timestamp = new Date().getTime();
                    Request req = null;

                    double latitude, longitude;

                    if (location != null) {

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                    } else {
                        latitude = 0;
                        longitude = 0;
                    }

                    // Asking location
                    if (alertStatus == STATUS_INQUIRE_LOCATION) {
                        req = new Request(
                                timestamp,
                                autoCompleteNumber.getText().toString(),
                                sNumber
                        );
                    }
                    // TODO: 18/11/17 location is null since lastKnownLocation is not working
                    // Sending location
                    else if (alertStatus == STATUS_SEND_LOCATION) {
                        req = new Request(
                                timestamp,
                                sNumber,
                                autoCompleteNumber.getText().toString(),
                                new LocationCoordinates(
                                        latitude,
                                        longitude
                                )
                        );
                    }

                    new RequestAdapter().addData(req, autoCompleteNumber.getText().toString(), sNumber, alertStatus);
                    Log.d(TAG, "Dialog onClick: Completed");
                    alertDialog.cancel();
                } else {
                    showSnack(false);
                }
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                            return;
                        }

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                        //locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
                        List<String> providers = locationManager.getProviders(true);
                        for (String provider : providers) {
                            Location l = locationManager.getLastKnownLocation(provider);
                            if (l == null) {
                                continue;
                            }
                            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                                // Found best last known location: %s", l);
                                location = l;
                            }
                        }
                        Log.i(TAG, "last known location: " + location);

                    } else {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                            //locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
                            List<String> providers = locationManager.getProviders(true);
                            for (String provider : providers) {
                                Location l = locationManager.getLastKnownLocation(provider);
                                if (l == null) {
                                    continue;
                                }
                                if (location == null || l.getAccuracy() < location.getAccuracy()) {
                                    // Found best last known location: %s", l);
                                    location = l;
                                }
                            }
                            Log.i(TAG, "last known location: " + location);
                        }
                    }
                }
            }
        }
    }


    /*@Deprecated
    private void initNotification() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(FirebaseConstants.USER)
                .child(sNumber)
                .child(FirebaseConstants.REQUEST_LIST);

        final DatabaseReference requestReference = database.getReference(FirebaseConstants.REQUEST);

        final RecyclerView.Adapter adapter = new NotificationAdapter(requestList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        reference.orderByChild(FirebaseConstants.TIMESTAMP).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: ");
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.i(TAG, "onDataChange: data: " + data.getValue());
                    try {
                        // Parsing the user request list using JSON object
                        final JSONObject object = new JSONObject(data.getValue().toString());
                        final int action = new Integer(object.get(FirebaseConstants.ACTION).toString());

                        // Referencing a particular request
                        final DatabaseReference request = requestReference.child(object.get(FirebaseConstants.REQUEST_LIST).toString());
                        request.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i(TAG, "onDataChange: request");
                                try {
                                    Log.i(TAG, "onDataChange: request: " + dataSnapshot.getValue());
                                    // Parsing request data using JSON object
                                    JSONObject requestData = new JSONObject(dataSnapshot.getValue().toString());

                                    *//*
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


                                    }*//*

                                    // After adding UserRequestReference as data member in Request class
                                    if (action == Constant.Actions.REQUEST_SENT.value || action == Constant.Actions.REQUEST_RECEIVED.value) {
                                        Log.i(TAG, "onDataChange: Request Received/ Request Sent");
                                        requestList.add(
                                                new Request(
                                                        new UserRequestReference(
                                                                (action == Constant.Actions.REQUEST_SENT.value) ? Constant.Actions.REQUEST_SENT.value : Constant.Actions.REQUEST_RECEIVED.value,
                                                                object.getString("requestID")
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
                                                                object.getString("requestID")
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
                                                                object.getString("requestID")
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
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    // Initialized notification
    private void initializeNotification() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(FirebaseConstants.USER)
                .child(sNumber)
                .child(FirebaseConstants.REQUEST_LIST);

        final DatabaseReference requestReference = database.getReference(FirebaseConstants.REQUEST);

        final RecyclerView.Adapter adapter = new NotificationAdapter(requestList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        reference.orderByChild(FirebaseConstants.ORDER).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildAdded: " + dataSnapshot);// + ":" + dataSnapshot.child(FirebaseConstants.REQUEST_ID).getValue().toString() + " : " + dataSnapshot.child(FirebaseConstants.ACTION).getValue().toString());
                // Parsing the user request list using JSON object

                final int action = new Integer(dataSnapshot.child(FirebaseConstants.ACTION).getValue().toString());
                // Referencing a particular request
                final DatabaseReference request = requestReference.child(dataSnapshot.child(FirebaseConstants.REQUEST_ID).getValue().toString());
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
                                                        dataSnapshot.getKey(),
                                                        (action == Constant.Actions.REQUEST_SENT.value) ? Constant.Actions.REQUEST_SENT.value : Constant.Actions.REQUEST_RECEIVED.value,
                                                        dataSnapshot.child(FirebaseConstants.REQUEST_ID).getValue().toString()
                                                ),
                                                requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                requestData.getString(FirebaseConstants.TO),
                                                requestData.getString(FirebaseConstants.FROM)
                                        )
                                );
                            } else if (action == Constant.Actions.LOCATION_SENT.value || action == Constant.Actions.LOCATION_RECEIVED.value) {
                                JSONObject obj = new JSONObject(requestData.get(FirebaseConstants.LOCATION).toString());
                                LocationCoordinates locationCoordinates = new LocationCoordinates(
                                        obj.getDouble(FirebaseConstants.LATITUDE),
                                        obj.getDouble(FirebaseConstants.LONGITUDE)
                                );

                                requestList.add(
                                        new Request(
                                                new UserRequestReference(
                                                        dataSnapshot.getKey(),
                                                        (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT.value : Constant.Actions.LOCATION_RECEIVED.value,
                                                        dataSnapshot.child(FirebaseConstants.REQUEST_ID).getValue().toString()
                                                ),
                                                requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                requestData.getString(FirebaseConstants.TO),
                                                requestData.getString(FirebaseConstants.FROM),
                                                locationCoordinates
                                        )
                                );
                            } else {
                                requestList.add(
                                        new Request(
                                                new UserRequestReference(
                                                        dataSnapshot.getKey(),
                                                        Constant.Actions.REQEUST_DECLINED.value,
                                                        dataSnapshot.child(FirebaseConstants.REQUEST_ID).getValue().toString()
                                                ),
                                                requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                requestData.getString(FirebaseConstants.TO),
                                                requestData.getString(FirebaseConstants.FROM)
                                        )
                                );


                            }
                            Log.i(TAG, "list size: " + requestList.size());
                            if (mProgressDialog.isShowing()) {
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

        if (mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }

        Log.i(TAG, "initNotification: list length " + requestList.size());
        /*if(requestList.size() == 0)
            mProgressDialog.hide();*/
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

    private void setContactPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                LandingActivity.this,
                Manifest.permission.READ_CONTACTS
        )) {
            Toast.makeText(this, "Contact permission needed", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_REQUEST_CODE);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_REQUEST_CODE);
                } else {
                    mUsersContact.execute();
                }
            }
        }
    }

    private ArrayList<ContactModel> getContacts() {
        ContactDataSouce cd = new ContactDataSouce();
        cd.getDBHelper(this);
        cd.openDatabase();
        ArrayList<ContactModel> contact = cd.getData();
        cd.closeDatabase();
        return contact;
    }

    public boolean checkConnectivity() {
        return ConnectivityReceiver.isConnected();
    }

    public void showSnack(boolean isConnected) {
        String message;

        if (isConnected) {
            message = "Network connected";
        } else {
            message = "Network not connected!!";
        }

        Snackbar.make(findViewById(R.id.landingcontainer), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    public class SetupUsersContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, ArrayList<String>> contactMap = getContactList();

            new RequestAdapter().isPresent(contactMap, LandingActivity.this);

            ContactDataSouce cd = new ContactDataSouce();
            cd.getDBHelper(LandingActivity.this);
            cd.openDatabase();
            System.out.println(cd.getData());
            cd.closeDatabase();
            return null;
        }

        private HashMap<String, ArrayList<String>> getContactList() {
            HashMap<String, ArrayList<String>> contactMap = new HashMap<>();
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = parseNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    Log.i(TAG, "Name: " + name + ", Number: " + number);
                    if (contactMap.containsKey(name)) {
                        contactMap.get(name).add(number);
                    } else {
                        ArrayList<String> numberList = new ArrayList<>();
                        numberList.add(number);
                        contactMap.put(name, numberList);
                    }
                }

                cursor.close();
            }

            return contactMap;
        }

        private String parseNumber(String number) {
            StringBuilder sb = new StringBuilder();
            for (char c : number.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                }
            }
            if(sb.toString().length() > 10) {
                return sb.toString().substring(2);
            }
            return sb.toString();
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
