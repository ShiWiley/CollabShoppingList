package com.udacity.firebase.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android. widget.Toast;

import com.fasterxml.jackson.databind.deser.Deserializers;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.Map;

/**
 * Created by wileyshi on 3/31/16.
 */

//Represents Aign up screen and functionality of the app
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private Firebase mFirebaseRef;
    private String mUserName, mUserEmail,mPassword;

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
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        //setup the profress dialog that is displayed later when authenticating with Firebase
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
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
        mPassword = mEditTextPasswordCreate.getText().toString();

        //check email and user name are valid
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);
        boolean validPassword = isPasswordValid(mPassword);

        if(!validEmail || !validPassword || !validUserName) {
            return;
        }

        //Everything is valid, show the progress dialog to indicate that account creation has started
        mAuthProgressDialog.show();

        //Create new user with specified email and password
        mFirebaseRef.createUser(mUserEmail,mPassword,new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                mAuthProgressDialog.dismiss();
                Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));
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
    public void createUserInFirebaseHelper(final String encodedEmail) {

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

    private boolean isPasswordValid(String password) {
        if(password.length() < 6) {
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_invalid_password_not_valid));
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
