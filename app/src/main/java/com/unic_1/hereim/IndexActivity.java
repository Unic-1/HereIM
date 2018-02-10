package com.unic_1.hereim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class IndexActivity extends AppCompatActivity {

    private EditText etNumber;
    private Button bSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = preferences.edit();

        etNumber = findViewById(R.id.etID);
        bSubmit = findViewById(R.id.bSubmit);

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etNumber.getText())) {
                    Snackbar.make(getWindow().getDecorView(), "Enter mobile number", Snackbar.LENGTH_LONG)
                            .setAction("No action", null)
                            .show();
                } else {
                    edit.putString("number", etNumber.getText().toString()).apply();
                    Intent i = new Intent(IndexActivity.this, LandingActivity.class);

                    startActivity(i);
                }
            }
        });

        if (!TextUtils.isEmpty(preferences.getString("number", ""))) {
            Intent i = new Intent(IndexActivity.this, LandingActivity.class);

            startActivity(i);
        }

    }
}
