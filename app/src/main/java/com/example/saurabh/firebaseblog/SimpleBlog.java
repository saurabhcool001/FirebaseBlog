package com.example.saurabh.firebaseblog;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by saurabh on 14-07-2017.
 */

public class SimpleBlog extends Application {

    Context context;

    // Get a non-default Database bucket
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mDatabase.setPersistenceEnabled(true);

    }
}
