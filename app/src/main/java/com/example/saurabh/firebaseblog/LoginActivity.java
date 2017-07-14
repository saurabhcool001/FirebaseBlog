package com.example.saurabh.firebaseblog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    Context context;

    private EditText mLoginEmail, mLoginPassword;
    private Button mSignIn, mSignUp;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgessDialog;

    // Get a non-default Database bucket
    FirebaseDatabase Database = FirebaseDatabase.getInstance();

    // Create a database reference from our app
    DatabaseReference mDatabaseRef = Database.getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mLoginEmail = (EditText) findViewById(R.id.loginEmail);
        mLoginPassword = (EditText) findViewById(R.id.loginPassword);
        mSignIn = (Button) findViewById(R.id.loginBtn);
        mSignUp = (Button) findViewById(R.id.signUp);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkLogin();

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signUp = new Intent(LoginActivity.this, RegisterActivity.class);
                signUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signUp);

            }
        });


    }

    private void checkLogin() {

        String email = mLoginEmail.getText().toString().trim();
        String password = mLoginPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        checkUserExits();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }

    @Override
    public void onStart() {
        super.onStart();
       // mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
       //     mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void checkUserExits() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)) {

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }else {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
