package com.unic_1.hereim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN ACTIVITY";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private EditText phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*getSupportActionBar().setTitle("Login");*/

        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=preferences.edit();
        edit.putString("number","9647884306");
        edit.apply();
        if(!TextUtils.isEmpty(preferences.getString("number", ""))) {
            Intent i = new Intent(LoginActivity.this, LandingActivity.class);

            startActivity(i);
        }

        Spinner countrySpinner = (Spinner) findViewById(R.id.spinner);
        String[] list = {"India", "Nepal", "Bhutan", "Sri Lanka", "Bangladesh"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        countrySpinner.setAdapter(adapter);

        // Setting the callback
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_APPEND);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("number", phone.getText().toString());
                editor.apply();

                Intent i = new Intent(LoginActivity.this, LandingActivity.class);

                startActivity(i);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                // ...

                // Hide phone verification components
                hideFirst();

                // Shows OTP components
                showSecond();


            }
        };

    }

    public void login(View v) {
        phone = (EditText) findViewById(R.id.etphone);

        if (!TextUtils.isEmpty(phone.getText())) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91"+phone.getText().toString(),        // Phone number to verify // FIXME: 7/9/17 add the country code at the start of the number
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks

            /*FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Users").child(phone.getText().toString());

            ref.setValue("Satyam");

            Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();*/
        } else {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
        }
    }

    /*public void otpVerify(View v) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, ((TextView)findViewById(R.id.etotp)).getText().toString());

        signInWithPhoneAuthCredential(credential);
    }*/

    public void resendOtp(View v) {

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    // Hides the components used for phone verification
    public void hideFirst() {
        /*findViewById(R.id.cardViewVerify).setVisibility(View.GONE);
        findViewById(R.id.cardViewVerifyText).setVisibility(View.GONE);*/
        findViewById(R.id.spinner).setVisibility(View.GONE);
        findViewById(R.id.bPhoneSubmit).setVisibility(View.GONE);
        findViewById(R.id.etphone).setVisibility(View.GONE);
    }

    // Shows the components used for phone verification
    public void showFirst() {
        /*findViewById(R.id.cardViewVerify).setVisibility(View.VISIBLE);
        findViewById(R.id.cardViewVerifyText).setVisibility(View.VISIBLE);*/
        findViewById(R.id.spinner).setVisibility(View.VISIBLE);
        findViewById(R.id.bPhoneSubmit).setVisibility(View.VISIBLE);
        findViewById(R.id.etphone).setVisibility(View.VISIBLE);
    }

    // Hides the components used for OTP verification
    public void hideSecond() {
        /*findViewById(R.id.cardViewVerifyOTP).setVisibility(View.GONE);*/
        /*findViewById(R.id.botpSubmit).setVisibility(View.GONE);
        findViewById(R.id.etotp).setVisibility(View.GONE);
        findViewById(R.id.resendCode).setVisibility(View.GONE);*/
    }

    // Hides the components used for OTP verification
    public void showSecond() {
        /*findViewById(R.id.cardViewVerifyOTP).setVisibility(View.VISIBLE);
        findViewById(R.id.botpSubmit).setVisibility(View.VISIBLE);
        findViewById(R.id.etotp).setVisibility(View.VISIBLE);
        findViewById(R.id.resendCode).setVisibility(View.VISIBLE);*/
    }
}
