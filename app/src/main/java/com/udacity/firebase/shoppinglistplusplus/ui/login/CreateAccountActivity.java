package com.udacity.firebase.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.LoginFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android. widget.Toast;

import com.fasterxml.jackson.databind.deser.Deserializers;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wileyshi on 3/31/16.
 */

//Represents Aign up screen and functionality of the app
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate;
    private Firebase mFirebaseRef;
    private String mUserName, mUserEmail,mPassword;
    private SecureRandom mRandom = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Create Firebase ref
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        //link layout elements from XML and setup the progress dialog
        initializeScreen();
    }

    //Override onCreateOptionsMenu to inflate nothing
    //@param menu - the menu with which nothing will happen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //Link layout elements from Xml and setup the progress dialog
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        //setup the profress dialog that is displayed later when authenticating with Firebase
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_check_inbox));
        mAuthProgressDialog.setCancelable(false);
    }

    //Open LoginActivity when user taps on Sign in textView
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //create account using Firebase email/password provider
    public void onCreateAccountPressed(View view) {
        //Start by doing a client side error check of the three value that the user entered
        //if any of the three values fail a check, stop this method
        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString();
        mPassword = new BigInteger(130, mRandom).toString(32);

        //check email and user name are valid
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);

        if(!validEmail || !validUserName) {
            return;
        }

        //Everything is valid, show the progress dialog to indicate that account creation has started
        mAuthProgressDialog.show();

        //Create new user with specified email and password
        mFirebaseRef.createUser(mUserEmail,mPassword,new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d(LOG_TAG, "Firebase ref " + mFirebaseRef.toString());
                mFirebaseRef.resetPassword(mUserEmail, new Firebase.ResultHandler(){
                    @Override
                    public void onSuccess() {
                        mAuthProgressDialog.dismiss();
                        Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CreateAccountActivity.this);
                        SharedPreferences.Editor spe = sp.edit();

                        //save name and email to shared preferences to create user database record
                        //when the refistered user will sign in for the first time
                        spe.putString(Constants.KEY_SIGNUP_EMAIL, mUserEmail).apply();

                        //Encode user email replacing "." with ","
                        createUserInFirebaseHelper();

                        //Passwird reset email sent open app chooser to pick app for handling inbox email intent
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        try {
                            startActivity(intent);
                            finish();
                        } catch (android.content.ActivityNotFoundException e){
                            //user has no app to handle email
                        }

                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        //Error occured log the error and dismiss the progress dialog
                        Log.d(LOG_TAG, getString(R.string.log_error_occurred) + firebaseError.getMessage());
                        mAuthProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                //Error occured
                Log.d(LOG_TAG, getString(R.string.log_error_occurred));
                mAuthProgressDialog.dismiss();

                //display error message
                if(firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
                    mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                }
                else {
                    showErrorToast(firebaseError.getMessage());  //general error
                }
            }
        });
    }

    //create new account using Firebase email / password provider
    //Stores key to an uid
    public void createUserInFirebaseHelper(String uid) {
        final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(uid);

        //check if there is already a user
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if there is no user make one
                if(dataSnapshot.getValue() == null) {
                    //set raw version of date to ServerValue.TimeStamp and save into dateCreatedMap
                    HashMap<String, Object> timestampJoined = new HashMap<String, Object>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(mUserName, mUserEmail, timestampJoined);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(LOG_TAG, getString(R.string.log_error_occurred));
            }
        });
    }
    //Stores the key to a encoded email
    public void createUserInFirebaseHelper(){
        final String encodedEmail = Utils.encodeEmail(mUserEmail);
        final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        //check if there's already a user (logged in with associated google account)
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if there is no user make one
                if(dataSnapshot.getValue() == null) {
                    //set raw version of dateto servervalue.timestamp and save into dateCreatedMap
                    HashMap<String, Object> timeStampJoined = new HashMap<String, Object>();
                    timeStampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(mUserName,mUserEmail, timeStampJoined);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(LOG_TAG, "Error occured");
            }
        });
    }

    private boolean isEmailValid(String email) {
        //return whether or not the email is valid
        boolean isValidEmail = (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if(isValidEmail) {
            return  isValidEmail;
        }
        else {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid),email));
            return false;
        }
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        else {
            return true;
        }
    }

    //Show error toast to users
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
