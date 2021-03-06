package com.polito.mad17.madmax.activities.users;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.SimpleDateFormat;
import java.util.TreeMap;

public class NewMemberActivity extends AppCompatActivity {

    private static final String TAG = NewMemberActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private ListView addedFriendsListView;
    private ListView friendsListView;
    private TreeMap<String, User> friends = new TreeMap<>();
    //todo usare SharedPreferences invece della map globale alreadySelected
    public static TreeMap<String, User> alreadySelected = new TreeMap<>();
    private HashMapFriendsAdapter friendsAdapter;
    private HashMapFriendsAdapter addedAdapter;

    private Button buttonInvite;
    private String groupID, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate di NewMemeberAcitivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonInvite = (Button) findViewById(R.id.btn_new_friend);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        groupName = intent.getStringExtra("groupName");

        Log.d(TAG, groupID);

        friendsListView = (ListView) findViewById(R.id.lv_friends);
        friendsAdapter = new HashMapFriendsAdapter(friends);

        addedFriendsListView = (ListView) findViewById(R.id.lv_added_members);
        addedAdapter = new HashMapFriendsAdapter(alreadySelected);

        addedFriendsListView.setAdapter(addedAdapter);

        databaseReference.child("users").child(MainActivity.getCurrentUID()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean alreadyAdded = false;
                for (DataSnapshot friendSnapshot: dataSnapshot.getChildren()) {
                    if (friendSnapshot.hasChild("sharedGroups")) {
                        if (friendSnapshot.child("sharedGroups").hasChild(groupID)) {
                            alreadyAdded = friendSnapshot.child("sharedGroups").child(groupID).getValue(Boolean.class);
                        }
                    }

                    // se sono già nel gruppo => vengono inseriti nell'addedAdapter
                    if (alreadyAdded) {
                        FirebaseUtils.getInstance().getFriendInviteToGroup(friendSnapshot.getKey(), groupID, alreadySelected, addedAdapter);
                    }
                    // altrimenti vengono inseriti nella lista friendsAdapter degli amici disponibili
                    else {
                        FirebaseUtils.getInstance().getFriendInviteToGroup(friendSnapshot.getKey(), "", friends, friendsAdapter);
                    }
                }

                friendsListView.setAdapter(friendsAdapter);
                addedFriendsListView.setAdapter(addedAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });

        //When i click on one friend of the list
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User item = friendsAdapter.getItem(position).getValue();
                friends.remove(item.getID());
                friendsAdapter.update(friends);
                friendsAdapter.notifyDataSetChanged();

                alreadySelected.put(item.getID(), item);
                addedAdapter.update(alreadySelected);
                addedAdapter.notifyDataSetChanged();
            }
        });

        //When i click on one added friend of the list
        addedFriendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User item = addedAdapter.getItem(position).getValue();
                alreadySelected.remove(item.getID());
                addedAdapter.update(alreadySelected);
                addedAdapter.notifyDataSetChanged();

                friends.put(item.getID(), item);
                friendsAdapter.update(friends);
                friendsAdapter.notifyDataSetChanged();
            }
        });

        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button clicked");
                Log.d(TAG, "invite a member to join the group");
                //        String deepLink = getString(R.string.invitation_deep_link) + "?groupToBeAddedID=" + groupID+ "?inviterToGroupUID=" + MainActivity.getCurrentUID();

                Uri.Builder builder = Uri.parse(getString(R.string.invitation_deep_link)).buildUpon()
                    .appendQueryParameter("groupToBeAddedID", groupID)
                    .appendQueryParameter("inviterID", MainActivity.getCurrentUID());

                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setDeepLink(builder.build())
                        .setMessage(getString(R.string.invitationToGroup_message))//.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                        .setCallToActionText(getString(R.string.invitationToGroup)) //todo vedere perchè non mostra questo link
                        .build();

                startActivityForResult(intent, MainActivity.REQUEST_INVITE_GROUP);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == MainActivity.REQUEST_INVITE_GROUP){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);

                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }

                // add event for FRIEND_GROUP_INVITE
                User currentUser = MainActivity.getCurrentUser();
                Event event = new Event(
                        groupID,
                        Event.EventType.FRIEND_GROUP_INVITE,
                        currentUser.getName() + " " + currentUser.getSurname()
                );
                event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                FirebaseUtils.getInstance().addEvent(event);
            }
            else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                //Toast.makeText(this, "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }


    //When I click SAVE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.action_save)
        {
            for(User newMemeber : alreadySelected.values())
            {
                FirebaseUtils.getInstance().joinGroupFirebase(newMemeber.getID(), groupID);

                // add event for GROUP_MEMBER_ADD
                User currentUser = MainActivity.getCurrentUser();
                Event event = new Event(
                        groupID,
                        Event.EventType.GROUP_MEMBER_ADD,
                        currentUser.getName() + " " + currentUser.getSurname(),
                        newMemeber.getName() + " " + newMemeber.getSurname()
                );
                event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                FirebaseUtils.getInstance().addEvent(event);
            }

            for(User user : friends.values()) {
                if (user.getIsInGroupForInvite().equals(groupID)) {
                    FirebaseUtils.getInstance().removeMemberFirebase(user.getID(), groupID);

                    // add event for GROUP_MEMBER_REMOVE
                    User currentUser = MainActivity.getCurrentUser();
                    Event event = new Event(
                            groupID,
                            Event.EventType.GROUP_MEMBER_REMOVE,
                            currentUser.getName() + " " + currentUser.getSurname(),
                            user.getName() + " " + user.getSurname()
                    );
                    event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                    event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                    FirebaseUtils.getInstance().addEvent(event);
                }
            }

            friends.clear();
            alreadySelected.clear();

            Toast.makeText(getApplicationContext(), getString(R.string.member_add), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}