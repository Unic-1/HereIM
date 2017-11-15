package com.unic_1.hereim.Adapter;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.Model.UserRequestReference;
import com.unic_1.hereim.RequestInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by unic-1 on 15/9/17.
 *
 * This is the Adapter class for Request module which add, update and retrieve
 * the data from the Firebase.
 */

public class RequestAdapter implements RequestInterface {

    final String TAG = "REQUEST ADAPTER";

    @Override
    public void addData(Request req, String to, String from, int requestId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Creates unique id for every request
        DatabaseReference reference = database.getReference("Request").push();
        // Gets Users reference
        DatabaseReference userReference = database.getReference("Users");



        DatabaseReference user1_ref = userReference.child(from).child("request_list").child(reference.getKey());
        DatabaseReference user2_ref = userReference.child(to).child("request_list").child(reference.getKey());

        if (requestId == 0) {
            // Updates the request list of user
            user1_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_SENT, reference.getKey()));
            user2_ref.setValue(new UserRequestReference(Constant.Actions.REQUEST_RECEIVED, reference.getKey()));
        } else if(requestId == 1) {
            // Updates the request list of user
            user1_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_SENT, reference.getKey()));
            user2_ref.setValue(new UserRequestReference(Constant.Actions.LOCATION_RECEIVED, reference.getKey()));
        }

        reference.setValue(req);
    }


    // FIXME: 4/10/17 Since firebase is itself running on a different thread there is no need of AsyncTask
    @Override
    public ArrayList<Request> getData(String number) {
        Log.i(TAG, "getData: Called");
        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(number).child("request_list");

        final DatabaseReference requestReference = database.getReference("Request");

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
                                            requestList.add(
                                                    new Request(
                                                            (action == Constant.Actions.LOCATION_SENT.value) ? Constant.Actions.LOCATION_SENT : Constant.Actions.LOCATION_RECEIVED,
                                                            requestData.getLong("timestamp"),
                                                            requestData.getString("to"),
                                                            requestData.getString("from"),
                                                            (LocationCoordinates) requestData.get("Location")
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
    public void updateRequest(String to, String from, String requestid, int action, LocationCoordinates locationCoordinates) {
        Log.i(TAG, "updateRequest: Called");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (action == Constant.Actions.LOCATION_SENT.value) {

            // Updating location in request of the request is sent
            DatabaseReference reference = database.getReference("Request").child(requestid).child("location");
            reference.setValue(locationCoordinates);
            Log.i(TAG, "updateRequest: Location Sent");

            // Updating user request status on both sender and receiver
            database.getReference("User").child(to).child("request_list").child(requestid).child("action").setValue(Constant.Actions.LOCATION_SENT.value);
            database.getReference("User").child(from).child("request_list").child(requestid).child("action").setValue(Constant.Actions.LOCATION_RECEIVED.value);
            Log.i(TAG, "updateRequest: Updated status to both user");

        } else if (action == Constant.Actions.REQEUST_DECLINED.value) {

            Log.i(TAG, "updateRequest: Request Declined");
            // Updating user request status on both sender and receiver
            database.getReference("User").child(to).child("request_list").child(requestid).child("action").setValue(Constant.Actions.REQEUST_DECLINED.value);
            database.getReference("User").child(from).child("request_list").child(requestid).child("action").setValue(Constant.Actions.REQUEST_RECEIVED.value);
            Log.i(TAG, "updateRequest: Updated status to both user");

        }
    }
    
}
