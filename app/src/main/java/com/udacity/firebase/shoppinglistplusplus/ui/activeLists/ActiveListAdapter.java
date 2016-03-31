package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;

import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import org.w3c.dom.Text;

/**
 * Created by wileyshi on 3/29/16.
 */

//Populates the list_view_active_lists inside shoppinglistfragment
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {

    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    //Protected method that populates the view attached to the adapter (list_view_active_lists)
    //with items inflated from single_active_lists
    //populateCiew also handles data changes and updates the listView accordingly
    @Override
    protected void populateView(View view, ShoppingList list) {
        //populate the single_active_list layout with the data in the current shopping list
        //similar to what was being displayed in ShoppingListfragment.

        //Grab TextViews and strings
        TextView textViewListName = (TextView) view.findViewById(R.id.text_view_list_name);
        TextView textViewCreatedByUser = (TextView) view.findViewById(R.id.text_view_created_by_user);
        TextView textViewTimestamp = (TextView) view.findViewById(R.id.text_view_edit_time);

        //Set the list name and owner
        textViewCreatedByUser.setText(list.getOwner());
        textViewListName.setText(list.getListName());
        textViewTimestamp.setText(Utils.SIMPLE_DATE_FORMAT.format(list.getTimestampLastChangedLong()));
    }

}
