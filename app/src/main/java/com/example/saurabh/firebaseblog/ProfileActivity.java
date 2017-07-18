package com.example.saurabh.firebaseblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    Context context;

    private RecyclerView mBlogList;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthList;
    private boolean mProcessLike = false;
    private Query mQueryCurrentUser;

    // Get a non-default Database bucket
    FirebaseDatabase Database = FirebaseDatabase.getInstance();

    // Create a database reference from our app
    DatabaseReference mDatabaseRef = Database.getReference().child("Blog");
    DatabaseReference mDatabaseUsers = Database.getReference().child("Users");
    DatabaseReference mDatabaseLike = Database.getReference().child("Likes");
    DatabaseReference mDatabaseCurrentUsers = Database.getReference().child("Blog");
    String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
//
        currentUserId = mAuth.getCurrentUser().getUid();
        mQueryCurrentUser = mDatabaseCurrentUsers.orderByChild("uid").equalTo(currentUserId);
        //Toast.makeText(this, "CurrentUser : " + currentUserId, Toast.LENGTH_SHORT).show();
//
        //mDatabaseUsers.keepSynced(true);
        mDatabaseRef.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDatabaseCurrentUsers.keepSynced(true);
//
        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
//        //checkUserExits();
    }

    @Override
    public boolean onSupportNavigateUp(){
        //finish();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Toast.makeText(this, "user onStart : " + currentUserId, Toast.LENGTH_SHORT).show();
        //mAuth.addAuthStateListener(mAuthList);

        FirebaseRecyclerAdapter<Blog, ProfileActivity.BlogViewHolder1> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Blog, BlogViewHolder1>(
                        Blog.class,
                        R.layout.blog_row,
                        ProfileActivity.BlogViewHolder1.class,
                        mQueryCurrentUser
                ) {
                    @Override
                    protected void populateViewHolder(BlogViewHolder1 viewHolder, Blog model, int position) {

                        final String post_key = getRef(position).getKey();

                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDesc());
                        viewHolder.setImage(getApplicationContext(), model.getImage());
                        viewHolder.setUsername(model.getUsername());

                        viewHolder.setLikeBtn(post_key);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

//                                Toast.makeText(MainActivity.this, "You Clicked a View " + post_key, Toast.LENGTH_SHORT).show();
                                Intent singleBlogIntent = new Intent(ProfileActivity.this, BlogSingleActivity.class);
                                //singleBlogIntent.putExtra("blog_id", post_key);
                                Bundle extras = new Bundle();
                                extras.putString("blog_id", post_key);
                                extras.putString("blog_intent", "ProfileActivity");
                                singleBlogIntent.putExtras(extras);
                                Log.i("intent_value ", "onClick: " + extras);
                                startActivity(singleBlogIntent);

                            }
                        });

                        viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (mProcessLike) {

                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                                mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mProcessLike = false;
                                            } else {
                                                mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
//                                                mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).push().setValue("RandomValue");
                                                mProcessLike = false;
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                };

        mBlogList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class BlogViewHolder1 extends RecyclerView.ViewHolder {

        View mView;
        //TextView post_title;
        ImageButton mLikeBtn;
        FirebaseAuth mAuth;

        // Get a non-default Database bucket
        FirebaseDatabase Database = FirebaseDatabase.getInstance();

        // Create a database reference from our app
        DatabaseReference mDatabaseLike = Database.getReference().child("Likes");


        public BlogViewHolder1(View itemView) {
            super(itemView);

            mView = itemView;
            mLikeBtn = (ImageButton) mView.findViewById(R.id.like_btn);

            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);
        }

        public void setLikeBtn(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_red_24dp);
                    } else {
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);

        }

        public void setDesc(String desc) {

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);

        }


        public void setImage(Context context, String image) {

            ImageView post_image = (ImageView) mView.findViewById(R.id.post_Image);
            Picasso.with(context).load(image).into(post_image);

        }

        public void setUsername(String username) {

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }
    }
}
