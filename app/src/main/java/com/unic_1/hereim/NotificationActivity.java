package com.unic_1.hereim;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unic_1.hereim.Adapter.NotificationAdapter;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NOTIFICATION ACTIVITY";
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

            SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);

        final ArrayList<Request> requestList = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(preferences.getString("number", "")).child("request_list");

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
                        requestList.notify();
                    }
                    Log.i(TAG, "onDataChange: for ended");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



            final RecyclerView.Adapter adapter = new NotificationAdapter(requestList, this);

            recyclerView.setLayoutManager(layoutManager);



        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(requestList.size()> count) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
            recyclerView.setAdapter(adapter);


    }
}
