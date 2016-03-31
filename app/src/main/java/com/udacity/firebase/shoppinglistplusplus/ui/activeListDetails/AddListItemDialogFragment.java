package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.ui.activeLists.AddListDialogFragment;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item, listId);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    public void addShoppingListItem() {
        String mItemName = mEditTextForList.getText().toString();

        if(mItemName != "" && mItemName != null) {
            //create firebase ref
            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            Firebase itemsRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);;

            //make map for the item you are adding
            HashMap<String, Object> updatedItemToAddMap = new HashMap<String,Object>();

            //save push to maintain push id
            Firebase newRef = itemsRef.push();
            String itemId = newRef.getKey();

            //Make POJO for item and turn it into HashMap
            ShoppingListItem itemToAddObject = new ShoppingListItem(mItemName);
            HashMap<String, Object> itemToAdd = (HashMap<String,Object>) new ObjectMapper().convertValue(itemToAddObject, Map.class);

            //add the item to the update map
            updatedItemToAddMap.put("/" + Constants.FIRBASE_LOCATION_SHOPPING_LIST_ITEMS + "/" + mListId + "/" + itemId, itemToAdd);

            //Make timestamp fpr last changed
            HashMap<String, Object> changedTimestampMap = new HashMap<>();
            changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            //Add the updated timestamp
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS + "/" + mListId + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, changedTimestampMap);

            //Update
            firebaseRef.updateChildren(updatedItemToAddMap);

            AddListItemDialogFragment.this.getDialog().cancel();
        }
    }

    @Override
    protected void doListEdit() {
        addShoppingListItem();
    }
}
