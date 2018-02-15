package com.unic_1.hereim.Adapter;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Constants.FirebaseConstants;
import com.unic_1.hereim.Database.ContactDataSouce;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.Model.UserRequestReference;
import com.unic_1.hereim.RequestInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by unic-1 on 15/9/17.
 *
 * This is the Adapter class for Request module which add, update and retrieve
 * the data from the Firebase.
 */

public class RequestAdapter implements RequestInterface {

    final String TAG = "REQUEST ADAPTER";
    final long UPPER_LIMIT = 99999999999999L;

    @Override
    public void addData(Request req, String to, String from, int requestId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Creates unique id for every request
        DatabaseReference reference = database.getReference(FirebaseConstants.REQUEST).push();
        // Gets Users reference
        DatabaseReference userReference = database.getReference(FirebaseConstants.USER);



        DatabaseReference user1_ref = userReference.child(from).child(FirebaseConstants.REQUEST_LIST).push();
        DatabaseReference user2_ref = userReference.child(to).child(FirebaseConstants.REQUEST_LIST).child(user1_ref.getKey());

        if (requestId == 0) {
            // Updates the request list of user
            user1_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_SENT.value, reference.getKey(), UPPER_LIMIT - req.getTimestamp()));
            user2_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_RECEIVED.value, reference.getKey(), UPPER_LIMIT - req.getTimestamp()));
        } else if(requestId == 1) {
            // Updates the request list of user
            user1_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_SENT.value, reference.getKey(), UPPER_LIMIT - req.getTimestamp()));
            user2_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_RECEIVED.value, reference.getKey(), UPPER_LIMIT - req.getTimestamp()));
        }

        reference.setValue(req);
    }


    // FIXME: 4/10/17 Since firebase is itself running on a different thread there is no need of AsyncTask
    @Override
    public ArrayList<Request> getData(String number) {
        Log.i(TAG, "getData: Called");
        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(FirebaseConstants.USER)
                .child(number)
                .child(FirebaseConstants.REQUEST_LIST);

        final DatabaseReference requestReference = database.getReference(FirebaseConstants.REQUEST);

        synchronized (this) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: ");
                    for (final DataSnapshot data : dataSnapshot.getChildren()) {
                        Log.i(TAG, "onDataChange: data: " + data.getValue());
                        try {
                            // Parsing the user request list using JSON object
                            JSONObject object = new JSONObject(data.getValue().toString());
                            final int action = new Integer(object.get(FirebaseConstants.ACTION).toString());

                            // Referencing a particular request
                            final DatabaseReference request = requestReference.child(object.get(FirebaseConstants.REQUEST_ID).toString());
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
                                                            requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                            requestData.getString(FirebaseConstants.TO),
                                                            requestData.getString(FirebaseConstants.FROM)
                                                    )
                                            );
                                        } else if (action == Constant.Actions.LOCATION_SENT.value || action == Constant.Actions.LOCATION_RECEIVED.value) {
                                            requestList.add(
                                                    new Request(
                                                            (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT : Constant.Actions.LOCATION_RECEIVED,
                                                            requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                            requestData.getString(FirebaseConstants.TO),
                                                            requestData.getString(FirebaseConstants.FROM),
                                                            (LocationCoordinates) requestData.get(FirebaseConstants.LOCATION)
                                                    )
                                            );
                                        } else {
                                            requestList.add(
                                                    new Request(
                                                            Constant.Actions.REQEUST_DECLINED,
                                                            requestData.getLong(FirebaseConstants.TIMESTAMP),
                                                            requestData.getString(FirebaseConstants.TO),
                                                            requestData.getString(FirebaseConstants.FROM)
                                                    )
                                            );
                                            //requestList.notify();
                                        }
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
                    }
                    Log.i(TAG, "onDataChange: for ended");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        Log.i(TAG, "getData: list length "+requestList.size());
        return requestList;
    }

    @Override
    public void updateRequest(String to, String from, String userReferenceId, String requestid, int action, LocationCoordinates locationCoordinates) {
        Log.i(TAG, "updateRequest: Called");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Updating location in request of the request is sent
        DatabaseReference reference = database.getReference(FirebaseConstants.REQUEST).child(requestid);
        long timestamp = new Date().getTime();

        if (action == Constant.Actions.LOCATION_SENT.value) {
            reference.child(FirebaseConstants.LOCATION).setValue(locationCoordinates);
            Log.i(TAG, "updateRequest: Location Sent");

            // Updating user request status on both sender and receiver

            database.getReference(FirebaseConstants.USER)
                    .child(to)
                    .child(FirebaseConstants.REQUEST_LIST)
                    .child(userReferenceId)
                    .setValue(
                            new UserRequestReference(Constant.Actions.LOCATION_RECEIVED.value, requestid, UPPER_LIMIT - timestamp)
                    );
            database.getReference(FirebaseConstants.USER)
                    .child(from)
                    .child(FirebaseConstants.REQUEST_LIST)
                    .child(userReferenceId)
                    .setValue(
                            new UserRequestReference(Constant.Actions.LOCATION_SENT.value, requestid, UPPER_LIMIT - timestamp)
                    );
            Log.i(TAG, "updateRequest: Updated status to both user");

        } else if (action == Constant.Actions.REQEUST_DECLINED.value) {

            Log.i(TAG, "updateRequest: Request Declined");
            // Updating user request status on both sender and receiver
            // TODO: 29/11/17 Change this part looking at the location send code
            database.getReference(FirebaseConstants.USER)
                    .child(to)
                    .child(FirebaseConstants.REQUEST_LIST)
                    .child(userReferenceId)
                    .setValue(
                            new UserRequestReference(Constant.Actions.REQEUST_DECLINED.value, requestid, UPPER_LIMIT - timestamp)
                    );
            database.getReference(FirebaseConstants.USER)
                    .child(from)
                    .child(FirebaseConstants.REQUEST_LIST)
                    .child(userReferenceId)
                    .setValue(
                            new UserRequestReference(Constant.Actions.REQEUST_DECLINED.value, requestid, UPPER_LIMIT - timestamp)
                    );
            Log.i(TAG, "updateRequest: Updated status to both user");

        }
        reference.child("timestamp").setValue(timestamp);
    }

    @Override
    public void isPresent(final HashMap<String, ArrayList<String>> contactMap, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(FirebaseConstants.USER);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ContactDataSouce contactDataSouce = new ContactDataSouce();
                contactDataSouce.getDBHelper(context);
                contactDataSouce.openDatabase();
                for (String key : contactMap.keySet()) {
                    System.out.println("Checking "+key);

                    for(String number: contactMap.get(key)) {
                        if(dataSnapshot.hasChild(number)) {
                            Log.i(TAG, "Present: " + key + " " + number);
                            contactDataSouce.insertData(number, key);
                        } else {
                            System.out.println(number+" not present");
                        }
                    }
                }
                contactDataSouce.closeDatabase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
