package com.udacity.firebase.shoppinglistplusplus.model;

/**
 * Created by wileyshi on 3/30/16.
 */
public class ShoppingListItem {
    String itemName;
    String owner;
    String Bought;
    String BoughtBy;

    public ShoppingListItem() {

    }

    public ShoppingListItem(String itemName) {
        this.itemName = itemName;
        this.owner = "Anonymous Owner";
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }
}
