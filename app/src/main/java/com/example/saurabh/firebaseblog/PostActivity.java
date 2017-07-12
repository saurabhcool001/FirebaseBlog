package com.example.saurabh.firebaseblog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
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
        mProgressDialog.show();

        final String title = mPostTitle.getText().toString().trim();
        final String description = mPostDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && mImageUri != null) {
            StorageReference filePath = mStorageRef.child("BlogImages").child(mImageUri.getLastPathSegment());

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = mDatabaseRef.push();

                    newPost.child("title").setValue(title);
                    newPost.child("description").setValue(description);
                    newPost.child("image").setValue(downloadUrl.toString());
//                    newPost.child("uId").setValue(FirebaseAuth.getCurrentuID);

                    mProgressDialog.dismiss();

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
