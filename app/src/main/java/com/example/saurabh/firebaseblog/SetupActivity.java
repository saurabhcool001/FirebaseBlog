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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    Context context;

    private ImageButton mSetupProfileBtn;
    private EditText mSetupName;
    private Button mSubmitBtn;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private ProgressDialog mProgressDialog;

    FirebaseAuth mAuth;


    // Get a non-default Database bucket
    FirebaseDatabase Database = FirebaseDatabase.getInstance();

    // Create a database reference from our app
    DatabaseReference mDatabaseRef = Database.getReference().child("Users");

    // Get a non-default Storage bucket
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a storage reference from our app
    StorageReference mStorageRef = storage.getReference().child("Profile_Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);

        mSetupProfileBtn = (ImageButton) findViewById(R.id.setupProfileBtn);
        mSetupName = (EditText) findViewById(R.id.setupNameField);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);

        mSetupProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });


        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(SetupActivity.this, "submit btn", Toast.LENGTH_SHORT).show();
                startSetupActivity();
            }
        });
    }

    private void startSetupActivity() {

        final String name = mSetupName.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name) && mImageUri != null) {

            mProgressDialog.setMessage("Finishing setup...");
            mProgressDialog.show();

            StorageReference filePath = mStorageRef.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    mDatabaseRef.child(user_id).child("name").setValue(name);
                    mDatabaseRef.child(user_id).child("image").setValue(downloadUrl);
                    mProgressDialog.dismiss();

                    startActivity(new Intent(SetupActivity.this, MainActivity.class));

                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                mSetupProfileBtn.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
