package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by wileyshi on 3/30/16.
 */

//Populates list_view_shopping_list _items inside ActiveListDetialActivity
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem>{
    private ShoppingList mShoppingList;
    private String mListId;
    private String mEncodedEmail;

    //constructor that initializes private instance variables when adapter is created
    public ActiveListItemAdapter(Activity activity, Class<ShoppingListItem> modelClass, int modelLayout, Query ref, String listId, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
        this.mEncodedEmail = encodedEmail;
    }

    //public method that is used to pass shoppingList object when it is loaded in ValueEventListener
    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    //popultates the view attached to the adapter (list_view_friends_autocomplete)
    //with items inflated from single_active_list_item.xml
    //populateView also handles data changes and updates the listView accordingly
    @Override
    protected void populateView(View view, final ShoppingListItem item, int position) {
        ImageButton buttonRemoveItem = (ImageButton) view.findViewById(R.id.button_remove_item);

        TextView textViewItemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
        final TextView textViewBoughtByUser = (TextView) view.findViewById(R.id.text_view_bought_by_user);
        TextView textViewBoughtBy = (TextView) view.findViewById(R.id.text_view_bought_by);

        String owner = item.getOwner();

        textViewItemName.setText(item.getItemName());

        setItemApperanceBaseOnBoughtStatus(owner, textViewBoughtByUser, textViewBoughtBy, buttonRemoveItem, textViewItemName, item);


        //get id of the item to remove
        final String itemToRemoveId = this.getRef(position).getKey();

        //Set up the view so that it shows the name of the item and the trash can button
        //trashcan button triggers a dialog to appear
        //makes the dialog
        buttonRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(itemToRemoveId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void removeItem(String itemId) {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
        //make map for the removal
        HashMap<String,Object> updatedRemoveItemMap = new HashMap<String, Object>();

        //remove the item by passing null
        updatedRemoveItemMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS +"/" +mListId + "/" + itemId, null);

        //Make the timestamp for lastchanged
        HashMap<String,Object> changedTimestampMap = new HashMap<>();
        changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        //Add the updated timestamp
        updatedRemoveItemMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS + "/" + mListId + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, changedTimestampMap);

        //Do update
        firebaseRef.updateChildren(updatedRemoveItemMap);
    }

    private void setItemApperanceBaseOnBoughtStatus(String owner, final TextView textViewBoughtByUser, final TextView textViewBoughtBy, ImageButton buttonRemoveItem, TextView textViewItemName, ShoppingListItem item) {
        //If selected item is bought
        //Set bought by text to You if current user is owner of the list
        //set bought by text to username if current user if not owner of the list
        //set the remove item button invisibility if current user is not list or item owner
        if (item.isBought() && item.getBoughtBy() != null) {
            textViewBoughtBy.setVisibility(View.VISIBLE);
            textViewBoughtByUser.setVisibility(View.VISIBLE);
            buttonRemoveItem.setVisibility(View.INVISIBLE);

            //Add strike through
            textViewItemName.setPaintFlags(textViewItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            if(item.getBoughtBy().equals(mEncodedEmail)) {
                textViewBoughtByUser.setText(mActivity.getString(R.string.text_you));
            }
            else {
                Firebase boughtByUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(item.getBoughtBy());
                //Get the items owners name use a singlevalueevent listener for memory efficiency
                boughtByUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user != null) {
                            textViewBoughtByUser.setText(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(mActivity.getClass().getSimpleName(), mActivity.getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
                    }
                });
            }
        }
        else {
            //if selected item is not bought
            //set bought by text to be empty and invisible
            //set the remove item button to visible if current user is owner of the list or selected item

            //remove the strike through
            textViewItemName.setPaintFlags(textViewItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            textViewBoughtBy.setVisibility(View.INVISIBLE);
            textViewBoughtByUser.setVisibility(View.INVISIBLE);
            textViewBoughtByUser.setText("");
            buttonRemoveItem.setVisibility(View.VISIBLE);
        }
    }
}
