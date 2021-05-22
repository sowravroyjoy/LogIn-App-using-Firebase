package com.example.loginappusingdatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class RegistrationActivity extends AppCompatActivity {

    private EditText rUsername;
    private EditText rEmail;
    private EditText rPassword;
    private Button rSignUp;
    private TextView rLogIn;
    private ImageView rImage;
    private EditText rAge;
    private FirebaseAuth firebaseAuth;
    private String rname, rage, remail, rpassword;
    private ProgressDialog progressDialog;
    private static int PICK_IMAGE = 123;
    Uri imagePath;

    private FirebaseStorage firebaseStorage;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null){
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
                rImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        rUsername = findViewById(R.id.etUsernameR);
        rEmail = findViewById(R.id.etEmailR);
        rPassword = findViewById(R.id.etPasswordR);
        rSignUp = findViewById(R.id.btnSignUpR);
        rLogIn = findViewById(R.id.tvLogInR);
        rImage = findViewById(R.id.ivImageR);
        rAge = findViewById(R.id.etAgeR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();


        progressDialog = new ProgressDialog(this);

        rImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("Image/*"); //Application/* //Application/doc
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE);
            }
        });



        rSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("loading...");
                progressDialog.show();
                if(validate()){
                    //upload into Database
                    String userEmail = rEmail.getText().toString().trim();
                    String userPassword = rPassword.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendEmailVerification();

                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

            }
        });

        rLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    private Boolean validate(){
        Boolean result = false;

        rname = rUsername.getText().toString();
        remail = rEmail.getText().toString();
        rpassword = rPassword.getText().toString();
        rage = rAge.getText().toString();

        if(rname.isEmpty()|| rage.isEmpty() || remail.isEmpty() || rpassword.isEmpty() || imagePath == null){
            Toast.makeText(this, "input error", Toast.LENGTH_SHORT).show();

        }else{
            result = true;
        }
        return result;
    }

    private void sendEmailVerification(){
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendUserData();
                        firebaseAuth.signOut();
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this, "successfully registered and email verification mail sent", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this, "verification mail has not been sent!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserData(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = firebaseDatabase.getReference(firebaseAuth.getUid());

        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid()).child("Images").child("Profile Picture"); //user_id/Images/Profile_Picture.png
        UploadTask uploadTask = imageReference.putFile(imagePath);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegistrationActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegistrationActivity.this, "Image Upload Complete!", Toast.LENGTH_SHORT).show();

            }
        });

        UserProfile userProfile = new UserProfile(rname,rage,remail);
        myRef.setValue(userProfile);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}