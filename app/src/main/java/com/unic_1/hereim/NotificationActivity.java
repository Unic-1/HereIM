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
import com.unic_1.hereim.Model.Request;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

            SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);

            final ArrayList<Request> requestList = new ArrayList<>();

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("Request").child(preferences.getString("number", ""));

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        // FIXME: 2/10/17 second parameter to the Request object is worng
                        requestList.add(new Request(new Long(data.child("timestamp").getValue().toString()), data.child("timestamp").getValue().toString(), data.child("number").getValue().toString()));
                        Log.i("Notification", ""+data.child("action").getValue());
                        Log.i("Notification", ""+data.child("timestamp").getValue());
                        Log.i("Notification", ""+data.child("number").getValue());
                        Log.i("Notification", ""+data.child("location").getValue());
                    };
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            RecyclerView.Adapter adapter = new NotificationAdapter(requestList);

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
    }
}
