package com.example.saurabh.firebaseblog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    Context context;

    // Get a non-default Storage bucket
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a storage reference from our app
    StorageReference mStorageRef = storage.getReference();

    // Get a non-default Database bucket
    FirebaseDatabase Database = FirebaseDatabase.getInstance();

    // Create a database reference from our app
    DatabaseReference mDatabaseRef = Database.getReference().child("Blog");

    private ImageButton mImageSelectBtn;
    private EditText mPostTitle;
    private EditText mPostDescription;
    private Button mSubmitBtn;
    private Uri mImageUri = null;
    public static final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mImageSelectBtn = (ImageButton) findViewById(R.id.imageSelectBtn);
        mPostTitle = (EditText) findViewById(R.id.postTitle);
        mPostDescription = (EditText) findViewById(R.id.postDescription);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);
        mProgressDialog = new ProgressDialog(this);

        mImageSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();
            }
        });
    }

    private void startPosting() {
        mProgressDialog.setMessage("Post upload to blog");


        final String title = mPostTitle.getText().toString().trim();
        final String description = mPostDescription.getText().toString().trim();
        final String[] uid = new String[1];
        final String[] name = new String[1];
        final String[] email = new String[1];

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && mImageUri != null) {

            mProgressDialog.show();

            StorageReference filePath = mStorageRef.child("BlogImages").child(mImageUri.getLastPathSegment());

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = mDatabaseRef.push();


                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // Name, email address, and profile photo Url
                        name[0] = user.getDisplayName();
                        email[0] = user.getEmail();


                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getToken() instead.
                        uid[0] = user.getUid();

                        newPost.child("title").setValue(title);
                        newPost.child("desc").setValue(description);
                        newPost.child("image").setValue(downloadUrl.toString());
                        newPost.child("uid").setValue(uid[0]);
                        //newPost.child("username").setValue(name[0]);
                        newPost.child("username").setValue(email[0]);

                        Log.i("user_detail", "onSuccess: " + name[0] + "  " + uid[0]);
//                    newPost.child("uId").setValue(FirebaseAuth.getCurrentuID);

                        mProgressDialog.dismiss();
                    }
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();

            mImageSelectBtn.setImageURI(mImageUri);
        }
    }
}
