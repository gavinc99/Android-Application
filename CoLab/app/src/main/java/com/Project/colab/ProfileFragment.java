package com.Project.colab;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Img/";

    //views from xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, collegeTv;
    FloatingActionButton fab;

    //progress dialog
    ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA__CODE = 400;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of chosen image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference=getInstance().getReference(); //firebase storage reference

        //init arrays of permissions
        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        avatarIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        collegeTv = view.findViewById(R.id.collegeTv);
        fab = view.findViewById(R.id.fab);

        //init progress dialog
        pd=new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String college = "" + ds.child("college").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    collegeTv.setText(college);

                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception then set to default
                        Picasso.get().load(R.drawable.ic_person).into(avatarIv);
                    }

                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception then set to default

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });




        return view;
    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==((PackageManager.PERMISSION_GRANTED));
        return result;
    }
    private void requestStoragePermission(){
        //request runtime  storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==((PackageManager.PERMISSION_GRANTED));

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==((PackageManager.PERMISSION_GRANTED));
        return result && result1;
    }
    private void requestCameraPermission(){
        //request runtime  storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        //options to show in dialog
        String options[] = {"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit College"};

        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("What would you like to edit?");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //handle dialog item clicks
                if (which==0){
                //Edit profile picture clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto="cover";//i.e. changing profile picture, make sure to assign the same value
                    showImagePicDialog();
                }
                else if (which==1){
                //Edit Cover photo clicked
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto="image";//i.e. changing cover photo, make sure to assign the same value
                    showImagePicDialog();
                }
                else if (which==2){
                    //Edit Name clicked
                    pd.setMessage("Updating Name");
                    //calling method and pass key "name" as parameter to update it's value in database
                    showNameCollegeUpdateDialog("name");
                }
                else if (which==3){
                    //Edit College clicked
                    pd.setMessage("Updating College");
                    showNameCollegeUpdateDialog("college");
                }

            }
        });

        //create and show dialog
        builder.create().show();

    }

    private void showNameCollegeUpdateDialog(final String key) {//using keys to determine whether a user wants to change their college or their name
        //custom dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key);//update name or college
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //add edit text
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+key);//hint
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog to update and cancel

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result=new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //updated, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress, get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                    Toast.makeText(getActivity(), "Please enter your "+ key + "", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //create and show dialog
        builder.create().show();



    }

    private void showImagePicDialog() {

        String options[] = {"Camera","Gallery"};

        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which==0) {
                    //Camera clicked

                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    //Gallery clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }


    });

        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){

            case CAMERA_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //permissions enabled
                        pickFromCamera();
                    }
                    else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;

            case STORAGE_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //permissions enabled
                        pickFromGallery();
                    }
                    else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            break;
        }
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //image is chosen from gallery, get uri of image
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }

            if(requestCode==IMAGE_PICK_CAMERA__CODE){
                //image is chosen from camera, get uri of image

                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        pd.show();
        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto + "_"+ user.getUid();

        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri=uriTask.getResult();

                //check if image is uploaded or not and url is received
                if (uriTask.isSuccessful()){
                    //image upload is successful
                    //add/update url in users database
                    HashMap<String, Object> results=new HashMap<>();

                    results.put(profileOrCoverPhoto, downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //url added to database successfully
                                    pd.dismiss();//dismiss progress bar
                                    Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //url not added to database
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image", Toast.LENGTH_SHORT).show();

                                }
                            });
                }
                else{
                    //error
                    pd.dismiss();
                    Toast.makeText(getActivity(), "An error has occurred", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
;    }

    private void pickFromCamera(){
        //picking image from device
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA__CODE);
    }

    private void pickFromGallery() {
        //choose from gallery
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

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

