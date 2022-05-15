package com.Project.colab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccountTv;
    Button mLoginBtn;

    private FirebaseAuth mAuth;

    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccountTv = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else{
                    loginUser(email, passw);
                }
            }
        });

        notHaveAccountTv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");
    }

    private void loginUser(String email, String passw) {
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //if user is signing in for the first time then get and show the user info from google account
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){

                                String email = user.getEmail();
                                String uid = user.getUid();

                                HashMap<Object, String> hashMap = new HashMap<>();

                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", ""); //will add later - edit profile
                                hashMap.put("phone", ""); //will add later
                                hashMap.put("image", ""); //will add later
                                hashMap.put("cover", ""); //will add later

                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                DatabaseReference reference = database.getReference("Users");

                                reference.child(uid).setValue(hashMap);

                            }



                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
