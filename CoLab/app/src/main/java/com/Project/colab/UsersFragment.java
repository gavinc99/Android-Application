package com.Project.colab;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import adapters.AdapterUser;
import models.Modeluser;
import androidx.appcompat.widget.SearchView;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser.AdapterUsers adapterUsers;
    List<Modeluser.ModelUser> userList;

    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        //init recyclerview
        recyclerView=view.findViewById(R.id.users_recyclerView);

        //set its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        userList = new ArrayList<>();


        //getAll users
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Modeluser.ModelUser modelUser = ds.getValue(Modeluser.ModelUser.class);

                    //get all users except currently signed in user
                    if(!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUsers = new AdapterUser.AdapterUsers(getActivity(), userList);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query){
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Modeluser.ModelUser modelUser = ds.getValue(Modeluser.ModelUser.class);

                    /*conditions to fulfil search:
                    *1user not current user
                    * 2the user name or email contains text entered in searchview (case insensitive) */



                    //get all searched users except currently signed in user
                    if(!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                 modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }

                    }

                    //adapter
                    adapterUsers = new AdapterUser.AdapterUsers(getActivity(), userList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //use not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)   {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }



    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //searchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text, search it
                    searchUsers(s);
                }
                else{
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text, search it
                    searchUsers(s);
                }
                else{
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }


    //handle menu item clicks
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);

    }
}

