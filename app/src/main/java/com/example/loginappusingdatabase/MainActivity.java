package com.example.loginappusingdatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText eUsername;
    private EditText ePassword;
    private Button eLogIn;
    private TextView eAttempts;
    private TextView eForgetPassword;
    private int counter = 5;

    private TextView eSignUp;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eUsername = findViewById(R.id.etUsernameR);
        ePassword = findViewById(R.id.etPasswordR);
        eLogIn = findViewById(R.id.btnLogin);
        eAttempts = findViewById(R.id.tvAttempts);
        eSignUp = findViewById(R.id.tvSignUp);
        eForgetPassword = findViewById(R.id.tvForgetPassword);

        eAttempts.setText("Number of remaining attempts: 5" );


        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            finish();
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }

        
        eLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(eUsername.getText().toString().trim(), ePassword.getText().toString());
            }
        });

        eSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        eForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    private void validate( String username, String password){
        progressDialog.setMessage("loading...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    checkEmailVerification();
                }else{

                    Toast.makeText(MainActivity.this, "login unsuccessful!", Toast.LENGTH_SHORT).show();
                    counter--;
                    progressDialog.dismiss();
                    if(counter == 0){
                        eLogIn.setEnabled(false);
                    }
                    eAttempts.setText("Number of remaining attempts: " + counter);
                }
            }
        });

    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Boolean emailFlag = firebaseUser.isEmailVerified();
        if(emailFlag){
            finish();
            Toast.makeText(MainActivity.this, "login successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }
        else{
            Toast.makeText(this, "verify your email", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }
}