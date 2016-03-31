package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;

import org.w3c.dom.Text;

import com.udacity.firebase.shoppinglistplusplus.utils.Utils;


/**
 * A simple {@link Fragment} subclass that shows a list of all shopping lists a user can see.
 * Use the {@link ShoppingListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListsFragment extends Fragment {
    private ListView mListView;
    //private TextView mTextViewListName; //v1
    //private TextView mTextViewListOwner; //v1
    //private TextView mTextViewListTimeStamp; //v1
    private ActiveListAdapter mActiveListAdapter;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    
    //v1
    //@Override
    //public void onActivityCreated(Bundle savedInstanceState) {
    //    super.onActivityCreated(savedInstanceState);
    //}

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);

        //Firebase listNameRef = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_ACTIVE_LISTS);
        Firebase activeListsRef = new Firebase(Constants.FIREBASE_URL_ACTIVE_LISTS);

        //add value event listeners to firebase reference
        //to control get data and control behavior and visibility of elements

        mActiveListAdapter = new ActiveListAdapter(getActivity(), ShoppingList.class,
                R.layout.single_active_list, activeListsRef);

        //set adapter to the mListView
        mListView.setAdapter(mActiveListAdapter);

        /*
        listNameRef.addValueEventListener(new ValueEventListener() {
            //data will change when app is launched and everytime listName changes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //datasnapshot is the snapshot of the firebase state when data gets changed
                //Log.e("ShoppingListFragment", dataSnapshot.toString());
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    ShoppingList shoppingList = ds.getValue(ShoppingList.class);

                    if (shoppingList != null) {
                        mTextViewListName.setText(shoppingList.getListName()); //v1
                        mTextViewListOwner.setText(shoppingList.getOwner()); //v1
                        mTextViewListTimeStamp.setText(Utils.SIMPLE_DATE_FORMAT.format(shoppingList.getTimestampLastChangedLong())); //v1

                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        */
        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingList selectedList = mActiveListAdapter.getItem(position);
                if(selectedList != null) {
                    Intent intent = new Intent(getActivity(), ActiveListDetailsActivity.class);
                    //Get the list id using the adapters get ref method to get the Firebase
                    //ref and then grab the key
                    String listId = mActiveListAdapter.getRef(position).getKey();
                    intent.putExtra(Constants.KEY_LIST_ID, listId);
                    //Starts an active showing the details for the selected list
                    startActivity(intent);
                }
            }
        });

        //mTextViewListName.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Intent myIntent = new Intent(getActivity(), ActiveListDetailsActivity.class);
        //        startActivity(myIntent);
        //    }
        //});

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActiveListAdapter.cleanup();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
        //mTextViewListName = (TextView) rootView.findViewById(R.id.text_view_list_name);
        //mTextViewListOwner = (TextView) rootView.findViewById(R.id.text_view_created_by_user);
        //mTextViewListTimeStamp = (TextView) rootView.findViewById((R.id.text_view_edit_time));
    }
}
