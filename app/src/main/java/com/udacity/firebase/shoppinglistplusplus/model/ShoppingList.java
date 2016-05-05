package com.udacity.firebase.shoppinglistplusplus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.firebase.client.ServerValue;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wileyshi on 3/24/16.
 */
public class ShoppingList {

    private String listName;
    private String owner;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, User> usersShopping;

    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner, HashMap<String, Object> timestampCreated) {
        this.listName = listName;
        this.owner = owner;
        //HashMap<String, Object> timestampLastChangedObj = new HashMap<String, Object>();
        //timestampLastChangedObj.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        //this.timestampLastChanged = timestampLastChangedObj;
        //this.timestampCreated = timestampLastChangedObj;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
        usersShopping = new HashMap<>();

    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampLastChanged;
    }

    public HashMap<String, User> getUsersShopping() {
        return usersShopping;
    }
    @JsonIgnore
    public long getTimestampLastChangedLong() {
        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @JsonIgnore
    public long getTimestampCreatedLong() {
        return (long) timestampCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }
    /*private String listName;
    private String owner;
    private HashMap<String,Object> timestampLastChanged;

    public ShoppingList() {
    }

    public ShoppingList(String name, String owner, HashMap<String,Object> timestampLastChanged) {
        this.owner = owner;
        this.listName = name;
        this.timestampLastChanged = timestampLastChanged;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, Object> getTimeStamp(){
        return timestampLastChanged;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public String getTimeStampString(){
        return Utils.SIMPLE_DATE_FORMAT.format(timestampLastChanged.get("timestamp")).toString();}*/

}
