package com.example.saurabh.firebaseblog;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    Context context;

    private ImageView mSingleBlogImage;
    private TextView mSingleBlogTitle, mSingleBlogDesc;
    private Button mSingleRemoveBtn;
    FirebaseAuth mAuth;

    // Get a non-default Database bucket
    FirebaseDatabase Database = FirebaseDatabase.getInstance();

    // Create a database reference from our app
    DatabaseReference mDatabaseRef = Database.getReference().child("Blog");
    public String mblog_intent;
    //final String mblog_intent = getIntent().getExtras().getString("blog_intent");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mSingleBlogImage = (ImageView) findViewById(R.id.single_blog_image);
        mSingleBlogTitle = (TextView) findViewById(R.id.single_blog_title);
        mSingleBlogDesc = (TextView) findViewById(R.id.single_blog_desc);
        mSingleRemoveBtn = (Button) findViewById(R.id.single_remove_btn);

        final String mPost_key = getIntent().getExtras().getString("blog_id");
        mblog_intent = getIntent().getExtras().getString("blog_intent");
        //Toast.makeText(this, "post_key : " + post_key, Toast.LENGTH_SHORT).show();

        mDatabaseRef.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                mSingleBlogTitle.setText(post_title);
                mSingleBlogDesc.setText(post_desc);

                Picasso.with(BlogSingleActivity.this).load(post_image).into(mSingleBlogImage);

                if (mAuth.getCurrentUser().getUid().equals(post_uid)) {
                    //Toast.makeText(BlogSingleActivity.this, "post_uid : " + post_uid, Toast.LENGTH_SHORT).show();
                    mSingleRemoveBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSingleRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabaseRef.child(mPost_key).removeValue();

                Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        //finish();
        if (mblog_intent != null) {
            startActivity(new Intent(BlogSingleActivity.this, ProfileActivity.class));
        }
        return true;
    }

    //   @Override
//    public void onBackPressed()
//    {
//        if (mblog_intent != null) {
//            Intent intent = new Intent(BlogSingleActivity.this, ProfileActivity.class);
//            startActivity(intent);
//        }
//    }
}
