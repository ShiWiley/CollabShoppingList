package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;

import org.w3c.dom.Text;

/**
 * Created by wileyshi on 3/30/16.
 */

//Populates list_view_shopping_list _items inside ActiveListDetialActivity
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem>{

    //constructor that initializes private instance variables when adapter is created
    public ActiveListItemAdapter(Activity activity, Class modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    //popultates the view attached to the adapter (list_view_friends_autocomplete)
    //with items inflated from single_active_list_item.xml
    //populateView also handles data changes and updates the listView accordingly
    @Override
    protected void populateView(View view, final ShoppingListItem item) {
        ImageButton buttonRemoveItem = (ImageButton) view.findViewById(R.id.button_remove_item);

        TextView textViewItemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
        textViewItemName.setText(item.getItemName());


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
                                removeItem();
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

    private void removeItem() {

    }
}
