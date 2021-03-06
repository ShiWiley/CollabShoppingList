package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;


/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private ListView mListView;
    private Firebase mActiveListRef, mCurrentUserRef;
    private String mListId;
    private ActiveListItemAdapter mActiveListItemAdapter;
    //stores whether the curent user is owner
    private boolean mCurrentUserIsOwner = false;
    private ShoppingList mShoppingList;
    private ValueEventListener mActiveListRefListener, mCurrentUserRefListener;
    private Button mButtonShopping;
    private User mCurrentUser;
    //stores whether current user is shopping
    private boolean mShopping = false;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);

        //Get push id from the extra passed by ShoppingListFragement
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(Constants.KEY_LIST_ID);
        if(mListId == null) {
            finish();
            return;
        }

        //Create Firebase reference
        mActiveListRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);
        mCurrentUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);
        Firebase listItemsRef = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();


        //Setup adapter
        mActiveListItemAdapter = new ActiveListItemAdapter(this, ShoppingListItem.class,
                R.layout.single_active_list_item, listItemsRef, mListId, mEncodedEmail);

        //set adapter to the mListView
        mListView.setAdapter(mActiveListItemAdapter);

        //Add valueEventListeners to Firebase references to control get data and control
        //behavior and visibility of elements

        //save the most up to date version of current user in mCurrentUser
        mCurrentUserRefListener = mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if(currentUser != null) mCurrentUser = currentUser;
                else finish();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
            }
        });

        final Activity thisActivity = this;

        //save the most recent version of current shopping list into mShoppingList instance
        //variable and update the UI to match the current list
        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //save most recent version of current shopping list into mShoppingList if present
                //finsh() the activity if the list is null
                //while current user is in the list details activity

                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                if (shoppingList == null) {
                    finish();

                    //call return or the rest of the method will still execute
                    return;
                }
                mShoppingList = shoppingList;

                //pass shopping list to the adapter if it is not null
                //mShoppingList is null when first created so this is done here
                mActiveListItemAdapter.setShoppingList(mShoppingList);

                //check if current user is the owner
                mCurrentUserIsOwner = Utils.checkIfOwner(shoppingList, mEncodedEmail);

            /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();

                setTitle(shoppingList.getListName());

                HashMap<String, User> usersShopping = mShoppingList.getUsersShopping();
                if(usersShopping != null && usersShopping.size() != 0 && usersShopping.containsKey(mEncodedEmail)) {
                    mShopping = true;
                    mButtonShopping.setText(getString(R.string.button_stop_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
                }
                else{
                    mButtonShopping.setText(getString(R.string.button_start_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                    mShopping = false;
                }
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
            }
        });

        /**
         * Set up click listeners for interaction.
         */

        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if(view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mActiveListItemAdapter.getItem(position);

                    if(shoppingListItem != null) {
                        String itemName = shoppingListItem.getItemName();
                        String itemId = mActiveListItemAdapter.getRef(position).getKey();

                        showEditListItemNameDialog(itemName, itemId);
                        return true;
                    }
                }
                return false;
            }
        });

        //perform buy/return action on listview item click event if current user is shopping
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Check that the view is not the empty footer item
                if(view.getId() != R.id.list_view_footer_empty) {
                    final ShoppingListItem selectedListItem = mActiveListItemAdapter.getItem(position);
                    String itemId = mActiveListItemAdapter.getRef(position).getKey();

                    if(selectedListItem != null) {
                        if (mShopping){
                            //create map and fill it in with deep path multi write operation list
                            HashMap<String, Object> updatedItemBoughtData = new HashMap<String, Object>();

                        //Buy selected item if it is not already bought
                        if (!selectedListItem.isBought()) {
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, true);
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, mEncodedEmail);
                        } else {
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, false);
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, null);
                        }

                        //Do Update
                        Firebase firebaseItemLocation = new Firebase(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId).child(itemId);
                        firebaseItemLocation.updateChildren(updatedItemBoughtData, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Log.d(LOG_TAG, getString(R.string.log_error_updating_data) + firebaseError.getMessage());
                                }
                            }
                        });
                    }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(mCurrentUserIsOwner);
        edit.setVisible(mCurrentUserIsOwner);
        share.setVisible(false);
        archive.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //@Override
    //public View onCreateView(LayoutInflater inflater, ViewGroup container,
    //                         Bundle savedInstanceState) {
    //
    //}

    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mActiveListRef.removeEventListener(mActiveListRefListener);
        mActiveListItemAdapter.cleanup();
        mCurrentUserRef.removeEventListener(mCurrentUserRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        mButtonShopping = (Button) findViewById(R.id.button_shopping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        /* Common toolbar setup */
        setSupportActionBar(toolbar);

        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);

        /*Firebase listNameRef = new Firebase(Constants.FIREBASE_URL).child("activeList");
        listNameRef.addValueEventListener(new ValueEventListener() {
              //data will change when app is launched and everytime listName changes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //datasnapshot is the snapshot of the firebase state when data gets changed
                //Log.e("ShoppingListFragment", dataSnapshot.toString());
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                if (shoppingList != null) {
                    getSupportActionBar().setTitle(shoppingList.getListName());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        Log.e("test", toolbar.getTitle().toString());*/

    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, itemName, itemId, mListId, mEncodedEmail);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {
        //If current user is already shopping, remove current user from usersShoppingMap
        Firebase usersShoppingRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS)
                .child(mListId).child(Constants.FIREBASE_PROPERTY_USERS_SHOPPING)
                .child(mEncodedEmail);
        //Either add or remove the current user from the usersShopping map
        if(mShopping) {
            usersShoppingRef.removeValue();
        }
        else {
            usersShoppingRef.setValue(mCurrentUser);
        }
    }
}
