package com.unic_1.hereim;

import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.concurrent.ExecutionException;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        try {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

            SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
            ArrayList<Request> requestList = new RequestThread().execute(preferences.getString("number", "")).get(); // FIXME: 8/9/17 check if the number exists in shared preference

            RecyclerView.Adapter adapter = new NotificationAdapter(requestList);

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    class RequestThread extends AsyncTask<String, Void, ArrayList<Request>> {

        @Override
        protected ArrayList<Request> doInBackground(String... params) {
            final ArrayList<Request> requestList = new ArrayList<>();

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("Request").child(params[0]);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        requestList.add(new Request(new Integer(data.child("action").getValue().toString()), new Long(data.child("timestamp").getValue().toString()), data.child("number").getValue().toString()));
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


            return requestList;
        }
    }
}
