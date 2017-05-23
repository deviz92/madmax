/*
package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.users.NewMemberActivity;
import com.polito.mad17.madmax.entities.User;

public class GroupInfoActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    String groupID;
    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView lv;

        //setContentView(R.layout.activity_group_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        caller = intent.getStringExtra("caller");

        //Button to add a new member
        Button newMemberButton = (Button) findViewById(R.id.addmember);
        newMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = GroupInfoActivity.this;
                Class destinationActivity = NewMemberActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
            }
        });

        lv = (ListView) findViewById(R.id.members);



        FirebaseListAdapter<User> firebaseListAdapter = new FirebaseListAdapter<User>(
                this,   //activity contentente la ListView
                User.class,   //classe in cui viene messo il dato letto (?)
                R.layout.list_item,   //layout del singolo item
                databaseReference.child("groups").child(groupID).child("members")  //nodo del db da cui leggo
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                Log.d("DEBUG", model.toString());
                TextView nametext = (TextView) v.findViewById(R.id.name);
                nametext.setText(model.getName() + " " + model.getSurname());

            }
        };

        lv.setAdapter(firebaseListAdapter);
    }
}*/
