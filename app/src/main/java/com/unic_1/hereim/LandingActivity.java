package com.unic_1.hereim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class LandingActivity extends AppCompatActivity {

    /*Gets triggered when ask button is clicked*/
    public void ask(View view) {

    }

    /*Gets triggered when answer button is clicked*/
    public void answer(View view) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.Notification) {
            Intent i = new Intent(this, NotificationActivity.class);

            startActivity(i);
        } else if(item.getItemId() == R.id.map) {
            Intent i = new Intent(this, MapsActivity.class);

            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
