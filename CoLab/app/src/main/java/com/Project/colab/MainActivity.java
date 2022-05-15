package com.Project.colab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.Project.colab.R;
import com.Project.colab.RegisterActivity;

public class MainActivity extends AppCompatActivity {
    private Button button;

    Button mRegisterBtn, mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=(Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenMap();
            }
        });

        mRegisterBtn = findViewById(R.id.register_btn);
        mLoginBtn = findViewById(R.id.login_btn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
    public  void OpenMap(){
        Intent intent=new Intent(this, Map.class);
        startActivity(intent);
    }
}
