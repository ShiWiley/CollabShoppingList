package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails.EditListDialogFragment;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Adds a new shopping list
 */
public class AddListDialogFragment extends EditListDialogFragment {
    EditText mEditTextListName;

    /**
     * Public static constructor that creates fragment and
     * passes a bundle with data into it when adapter is created
     */
    public static AddListDialogFragment newInstance() {
        AddListDialogFragment addListDialogFragment = new AddListDialogFragment();
        Bundle bundle = new Bundle();
        addListDialogFragment.setArguments(bundle);
        return addListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_list, null);
        mEditTextListName = (EditText) rootView.findViewById(R.id.edit_text_list_name);

        /**
         * Call addShoppingList() when user taps "Done" keyboard action
         */
        mEditTextListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    addShoppingList();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addShoppingList();
                    }
                });

        return builder.create();
    }

    /**
     * Add new active list
     */
    public void addShoppingList() {
        // Get the reference to the root node in Firebase
        // Get the string that the user entered into the EditText and make an object with it
        // We'll use "Anonymous Owner" for the owner because we don't have user accounts yet
        String userEnteredName = mEditTextListName.getText().toString();
        String owner = "Anonymous Owner";
        //ShoppingList currentList = new ShoppingList(userEnteredName, owner);

        // Go to the "activeList" child node of the root node.
        // This will create the node for you if it doesn't already exist.
        // Then using the setValue menu it will serialize the ShoppingList POJO
        //Firebase newPostRef = ref.push();
        //ref.child("activeList").setValue(currentList);

        //edit text input not empty
        if(!userEnteredName.equals("")) {

            //create Firebase ref
            Firebase listRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS);
            Firebase newListRef = listRef.push();

            // save uid
            final String listId = newListRef.getKey();

            //Set raw version of date to the server value timestamp
            HashMap<String, Object> timestampCreated = new HashMap<String, Object>();
            timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            //Build shopping list
            ShoppingList shoppingList = new ShoppingList(userEnteredName, owner, timestampCreated);

            //add shopping list value
            newListRef.setValue(shoppingList);

            //Close dialog
            AddListDialogFragment.this.getDialog().cancel();
        }
        /*
        //Get reference to the root node in Firebase
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        //Get the string that the user entered into the EditText and make an object with it
        //Use anonymous Owner for the owner for now since there is no user accounts yet
        String userEnteredName = mEditTextListName.getText().toString();
        String owner = "Anonymous Owner";
        HashMap<String, Object> timestampLastChanged = new HashMap<String, Object>();
        timestampLastChanged.put("timestamp", ServerValue.TIMESTAMP);
        ShoppingList shoppingList = new ShoppingList(userEnteredName, owner, timestampLastChanged);
        ref.child("activeList").setValue(shoppingList);
        //ref.child("listName").setValue(userEnteredName);*/
    }
    @Override
    protected void doListEdit() {

    }

}

