package com.udacity.firebase.shoppinglistplusplus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wileyshi on 3/24/16.
 */
public class ShoppingList {
    private String listName;
    private String owner;
    private HashMap<String,Object> timeStamp;

    public ShoppingList() {
    }

    public ShoppingList(String name, String owner, HashMap<String,Object> timeStamp) {
        this.owner = owner;
        this.listName = name;
        this.timeStamp = timeStamp;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, Object> getTimeStamp(){
        return timeStamp;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public String getTimeStampString(){

        return Utils.SIMPLE_DATE_FORMAT.format(timeStamp.get("date")).toString();
    }
}
