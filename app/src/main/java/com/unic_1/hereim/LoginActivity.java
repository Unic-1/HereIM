package com.unic_1.hereim;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.database.FirebaseDatabase;
import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Constants.FirebaseConstants;

import java.util.concurrent.TimeUnit;

/*
* This activity is unused for the time being because of the following:
* OTP is not received in different network carrier
* */

// TODO: 29/11/17 If the OTP session is expired display the message accordingly

public class LoginActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "LOGIN ACTIVITY";
    private static final int ENTER_NUMBER = 0;
    private static final int ENTER_OTP = 1;
    private static int sPageStatus = ENTER_NUMBER;
    private static String sCountryCode;

    private static String sTempNumberStore;
    private static String sTempUsernameStore;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private EditText mPhoneNumberField;
    private EditText mUsername;
    private Spinner mCountrySpinner;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sCountryCode = Constant.COUNTRYCODE[0];

        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("number","8240353705"); // TODO: 24/11/17 Remove this line while releasing
        edit.apply();
        if (!TextUtils.isEmpty(preferences.getString("number", ""))) {
            Intent i = new Intent(LoginActivity.this, LandingActivity.class);

            startActivity(i);
        }

        mPhoneNumberField = findViewById(R.id.etphone);
        mUsername = findViewById(R.id.etUsername);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Verifying OTP");
        mCountrySpinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Constant.COUNTRYLIST);
        mCountrySpinner.setAdapter(adapter);

        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sCountryCode = Constant.COUNTRYCODE[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setting the callback
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the mPhoneNumberField number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("number", mPhoneNumberField.getText().toString());
                editor.apply();

                FirebaseDatabase.getInstance()
                        .getReference(mPhoneNumberField.getText().toString())
                        .child(FirebaseConstants.NAME)
                        .setValue(mUsername.getText().toString());


                Intent i = new Intent(LoginActivity.this, LandingActivity.class);
                //startActivity(i); // TODO: 25/11/17 Uncomment this line
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the mPhoneNumberField number format is not valid.
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
                // The SMS verification code has been sent to the provided mPhoneNumberField number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                // ...
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if (sPageStatus == ENTER_NUMBER) {
            hideSecond();
            showFirst();
        } else if (sPageStatus == ENTER_OTP) {
            hideFirst();
            showSecond();
        }

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    public void login(View v) {
        if (checkConnectivity()) {
            if (sPageStatus == ENTER_NUMBER) {
                if (!TextUtils.isEmpty(mPhoneNumberField.getText()) && sCountryCode != null && !TextUtils.isEmpty(mUsername.getText())) {
                    sTempNumberStore = "+" + sCountryCode + mPhoneNumberField.getText().toString();
                    sTempUsernameStore = mUsername.getText().toString();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                             sTempNumberStore,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks

                    // Hide mPhoneNumberField verification components
                    hideFirst();

                    // Shows OTP components
                    showSecond();
                } else {
                    Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                }
            } else if (sPageStatus == ENTER_OTP) {
                if (!TextUtils.isEmpty(mPhoneNumberField.getText())) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mPhoneNumberField.getText().toString());
                }
            }
        } else {
            showSnack(false);
        }
    }

    /*public void otpVerify(View v) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, ((TextView)findViewById(R.id.etotp)).getText().toString());

        signInWithPhoneAuthCredential(credential);
    }*/

    public void resendOtp(View v) {
        resendVerificationCode(sTempNumberStore, mResendToken);
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
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mPhoneNumberField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                        }
                    }
                });
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]


    // Hides the components used for mPhoneNumberField verification
    public void hideFirst() {
        /*findViewById(R.id.cardViewVerify).setVisibility(View.GONE);
        findViewById(R.id.cardViewVerifyText).setVisibility(View.GONE);*/
        mCountrySpinner.setVisibility(View.GONE);
        findViewById(R.id.bPhoneSubmit).setVisibility(View.GONE);
        mPhoneNumberField.setVisibility(View.GONE);
        mUsername.setVisibility(View.GONE);

        mCountrySpinner.setSelection(0);
        mPhoneNumberField.setText("");
        mUsername.setText("");
    }

    // Shows the components used for mPhoneNumberField verification
    public void showFirst() {
        /*findViewById(R.id.cardViewVerify).setVisibility(View.VISIBLE);
        findViewById(R.id.cardViewVerifyText).setVisibility(View.VISIBLE);*/
        mCountrySpinner.setVisibility(View.VISIBLE);
        findViewById(R.id.bPhoneSubmit).setVisibility(View.VISIBLE);
        mPhoneNumberField.setVisibility(View.VISIBLE);
        mUsername.setVisibility(View.VISIBLE);

    }

    // Hides the components used for OTP verification
    public void hideSecond() {
        /*findViewById(R.id.cardViewVerifyOTP).setVisibility(View.GONE);*/
        //findViewById(R.id.resendCode).setVisibility(View.GONE);
        //mPhoneNumberField.setHint(getString(R.string.enter_phone_number));
        findViewById(R.id.refreshImageButton).setVisibility(View.GONE);

        mPhoneNumberField.setText("");
    }

    // Hides the components used for OTP verification
    public void showSecond() {
        findViewById(R.id.bPhoneSubmit).setVisibility(View.VISIBLE);
        findViewById(R.id.refreshImageButton).setVisibility(View.VISIBLE);
        mPhoneNumberField.setVisibility(View.VISIBLE);
        mPhoneNumberField.setHint("Enter OTP");
    }

    public boolean checkConnectivity() {
        return ConnectivityReceiver.isConnected();
    }

    public void showSnack(boolean isConnected) {
        String message = "";

        if (isConnected) {
            message = "Network connected";
        } else {
            message = "Network not connected!!";
        }

        Snackbar.make(findViewById(R.id.logincontainer), message, Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
