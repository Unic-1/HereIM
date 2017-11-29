package com.unic_1.hereim;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText et1 = (EditText) findViewById(R.id.editText);
        final EditText et2 = (EditText) findViewById(R.id.editText3);
        Button sum = (Button) findViewById(R.id.button2);

        sum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                * Sum of two number

                int num1 = Integer.parseInt(et1.getText().toString());
                int num2 = Integer.parseInt(et2.getText().toString());
                int sum = num1+num2;
                Toast.makeText(MainActivity.this, sum, Toast.LENGTH_SHORT).show();

                */

                /*
                * Login
                String username = et1.getText().toString();
                String password = et2.getText().toString();

                if(username.equals("Partha") && password.equals("1234")) {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                }
                */
            }
        });


    }
}
