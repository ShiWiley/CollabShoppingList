package com.udacity.firebase.shoppinglistplusplus;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;
/**
 * Includes one-time initialization of Firebase related code
 */
public class ShoppingListApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase initialization
        //can also be in the onCreate() methods but this is good for global setup
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
    }

}