package com.polito.mad17.madmax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.ChooseGroupActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpenseDetailActivity;
import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.activities.login.LoginSignUpActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.activities.users.FriendsFragment;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.friends;

//import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.groups;

public class MainActivity extends BasicActivity implements OnItemClickInterface, OnItemLongClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference, usersRef, groupRef;

    public static FirebaseAuth auth;
    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private MenuItem one, two, three;
    private ViewPager viewPager;
    private PagerAdapter adapter;

 //   private ActionBarDrawerToggle drawerToggle;

    private static final int REQUEST_INVITE = 0;

    private static User currentUser;
    private String currentUID, inviterUID, groupToBeAddedID;


    private HashMap<String, String> userFriends;
    private HashMap<String, String> userGroups;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        Log.i(TAG, "token: "+FirebaseInstanceId.getInstance().getToken());

        // waiting user data becomes available
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        getDatabase();
        databaseReference = firebaseDatabase.getReference();
        auth = FirebaseAuth.getInstance();

        userFriends = new HashMap<>();
        userGroups = new HashMap<>();

        usersRef = databaseReference.child("users");
        groupRef = databaseReference.child("groups");

        // insert tabs and current fragment in the main layout
        mainView.addView(getLayoutInflater().inflate(R.layout.skeleton_tab, null));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        //viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        viewPager.setCurrentItem(1);
//        updateFab(1);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getPosition() + "");
                updateFab(tab.getPosition());
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // in the main we don't want an expansible bar
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(false);
        //todo: capire come bloccare la barra nel main
    }

    private void updateFab(int position){
        switch(position){
            case 0:
                // friends fragment
                Log.d(TAG, "fab 0");
                fab.setImageResource(R.drawable.person_add);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "my ID is " + MainActivity.getCurrentUser().getID());
                        String deepLink = getString(R.string.invitation_deep_link) + "?inviterUID=" + MainActivity.getCurrentUser().getID();

                        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                                .setDeepLink(Uri.parse(deepLink))
                                .setMessage(getString(R.string.invitation_message))
                                //                     .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                                .setCallToActionText(getString(R.string.invitation_cta))
                                .build();

                        startActivityForResult(intent, REQUEST_INVITE);
                    }
                });
                break;
            case 1:
                // groups fragment
                Log.d(TAG, "fab 1");
                fab.setImageResource(R.drawable.group_add);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, NewGroupActivity.class);
                        myIntent.putExtra("userAdded", currentUser);//("UID", currentUID);
                        //String tempGroupID = mDatabase.child("temporarygroups").push().getKey();
                        //inizialmente l'unico user è il creatore del gruppo stesso
                  //      User myself = new User(myselfID, "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null, "€");
                        //mDatabase.child("temporarygroups").child(tempGroupID).child("members").push();
                        //mDatabase.child("temporarygroups").child(tempGroupID).child("members").child(myself.getID()).setValue(myself);
                  //      NewGroupActivity.newmembers.put(currentUID, currentUser);  //inizialmente l'unico membro del nuovo gruppo sono io
                        //myIntent.putExtra("groupID", tempGroupID);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
                break;
            case 2:
                // pending fragment
                Log.d(TAG, "fab 2");
                fab.setImageResource(R.drawable.edit);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, ChooseGroupActivity.class);
                        myIntent.putExtra("userAdded", currentUser);//("UID", currentUID);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
                //fab.setClickable(false);
                break;
        }
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        int numberOfTabs;

        FriendsFragment friendsFragment = null;
        GroupsFragment groupsFragment = null;
        PendingExpensesFragment pendingExpensesFragment = null;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position) {
                case 0:
                    Log.i(TAG, "here in case 0: FriendsFragment");
                    friendsFragment = new FriendsFragment();
                    return friendsFragment;
                case 1:
                    Log.i(TAG, "here in case 1: GroupsFragment");
                    groupsFragment = new GroupsFragment();
                    return groupsFragment;
                case 2:
                    Log.i(TAG, "here in case 2: PendingExpensesFragment");
                    pendingExpensesFragment = new PendingExpensesFragment();
                    return pendingExpensesFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":
                intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra("friendID", itemID);
                intent.putExtra("userID", currentUser.getID());
                startActivity(intent);
                break;

            case "GroupsFragment":
                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", itemID);
                intent.putExtra("userID", currentUser.getID());
                startActivity(intent);
                break;

            case "PendingExpensesFragment":
                intent = new Intent(this, PendingExpenseDetailActivity.class);
                intent.putExtra("expenseID", itemID);
                intent.putExtra("userID", currentUser.getID());
                startActivity(intent);
                break;
        }

    }

    //Apro popup menu quando ho tenuto premuto un friend o gruppo per 1 secondo
    @Override
    public void itemLongClicked(String fragmentName, final String itemID, View v) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":

                PopupMenu popup = new PopupMenu(MainActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Remove Friend");
                popup.getMenu().findItem(R.id.two).setVisible(false);
                popup.getMenu().findItem(R.id.three).setVisible(false);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu


                break;

            case "GroupsFragment":

                popup = new PopupMenu(MainActivity.this, v, Gravity.RIGHT);

                popup.getMenuInflater().inflate(R.menu.longclick_popup_menu, popup.getMenu());
                one = popup.getMenu().findItem(R.id.one);
                one.setTitle("Leave this Group");
                two = popup.getMenu().findItem(R.id.two);
                two.setTitle("Remove this Group");
                popup.getMenu().findItem(R.id.three).setVisible(false);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();

                        switch ((String) item.getTitle()) {

                            case "Leave this Group":

                                leaveGroupFirebase(itemID);
                                break;

                            case "Remove this Group":
                                removeGroupFirebase (itemID);
                                break;
                        }

                        return true;
                    }
                });

                popup.show();//showing popup menu

                break;
        }
    }

    public String addExpenseFirebase(Expense expense) {

        //Aggiungo spesa a Firebase
        String eID = databaseReference.child("expenses").push().getKey();
        databaseReference.child("expenses").child(eID).setValue(expense);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        databaseReference.child("expenses").child(eID).child("timestamp").setValue(timeStamp);

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet())
        {
            //Se il participant corrente è il creatore della spesa
            if (participant.getKey().equals(expense.getCreatorID()))
            {
                //paga tutto lui
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(expense.getAmount());
            }
            else
            {
                //gli altri participant inizialmente non pagano niente
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(0);
            }

            //risetto fraction di spesa che deve pagare l'utente, visto che prima si sputtana
            databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("fraction").setValue(expense.getParticipants().get(participant.getKey()));



            //Aggiungo spesaID a elenco spese dello user
            //todo controllare se utile
            databaseReference.child("users").child(participant.getKey()).child("expenses").child(eID).setValue("true");
        }

        //Aggiungo spesa alla lista spese del gruppo
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue("true");

        return eID;

        //u.updateBalance(expense);
        //updateBalanceFirebase(u, expense);
    }

    public void joinGroupFirebase (final String userID, String groupID) {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Aggiungo gruppo alla lista gruppi dello user
        databaseReference.child("users").child(userID).child("groups").push();
        databaseReference.child("users").child(userID).child("groups").child(groupID).setValue("true");
        //Aggiungo user (con sottocampi admin e timestamp) alla lista membri del gruppo
        databaseReference.child("groups").child(groupID).child("members").push();
        databaseReference.child("groups").child(groupID).child("members").child(userID).push();
        databaseReference.child("groups").child(groupID).child("members").child(userID).child("admin").setValue("false");
        databaseReference.child("groups").child(groupID).child("members").child(userID).push();
        databaseReference.child("groups").child(groupID).child("members").child(userID).child("timestamp").setValue("time");

    }

    public void addFriendFirebase (final String user1ID, final String user2ID) {
        //Add u2 to friend list of u1
        databaseReference.child("users").child(user1ID).child("friends").push();
        databaseReference.child("users").child(user1ID).child("friends").child(user2ID).setValue("true");
        //Add u1 to friend list of u2
        databaseReference.child("users").child(user2ID).child("friends").push();
        databaseReference.child("users").child(user2ID).child("friends").child(user1ID).setValue("true");

        //Read groups u1 belongs to
        Query query = databaseReference.child("users").child(user1ID).child("groups");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final ArrayList<String> u1Groups = new ArrayList<String>();

                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    u1Groups.add(groupSnapshot.getKey());
                }

                Query query = databaseReference.child("users").child(user2ID).child("groups");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<String> sharedGroups = new ArrayList<String>();


                        for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                        {
                            if (u1Groups.contains(groupSnapshot.getKey()))
                                sharedGroups.add(groupSnapshot.getKey());
                        }

                        //ora in sharedGroups ci sono solo i gruppi di cui fanno parte entrambi gli utenti
                        for (String groupID : sharedGroups)
                        {
                            databaseReference.child("users").child(user1ID).child("friends").child(user2ID).child(groupID).setValue("true");
                            databaseReference.child("users").child(user2ID).child("friends").child(user1ID).child(groupID).setValue("true");
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //todo eliminare spese peding di cui faccio parte in questo gruppo
    public void leaveGroupFirebase (final String groupID) {
        Group g = GroupsFragment.groups.get(groupID);
        if (g != null)
        {
            Log.d (TAG, "Bilancio attuale col gruppo: " + g.getBalance());
            if (g.getBalance() > 0)
            {
                Log.d (TAG, "Hai un credito verso questo gruppo. Abbandonare comunque?");
                return;
            }
            else if (g.getBalance() < 0)
            {
                Log.d (TAG, "Hai un debito verso questo gruppo. Prima salda il debito poi puoi abbandonare, testa di cazzo");
                return;
            }
            else
            {
                Log.d (TAG, "Nessuno debito, abbandono in corso");
                //Elimino gruppo da lista gruppi dello user
                databaseReference.child("users").child(getCurrentUser().getID()).child("groups").child(groupID).setValue(false);
                //Elimino user dalla lista dei members del gruppo
                databaseReference.child("groups").child(groupID).child("members").child(getCurrentUser().getID()).child("deleted").setValue(true);

                //Elimino pending expenses di questo gruppo dallo user, in modo che lui non le veda più. Gli altri coninuano a vederle
                databaseReference.child("users").child(getCurrentUser().getID()).child("proposedExpenses").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Per ogni spesa proposta dello user
                        for (DataSnapshot proposedExpenseSnap : dataSnapshot.getChildren())
                        {
                            final String expenseID = proposedExpenseSnap.getKey();

                            databaseReference.child("proposedExpenses").child(expenseID).child("groupID").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot groupSnapshot) {

                                    //Se la spesa proposta è nel gruppo appena eliminato
                                    if (groupSnapshot.getValue(String.class).equals(groupID))
                                    {
                                        //Elimino la spesa proposta dall'elenco dell'utente
                                        databaseReference.child("users").child(getCurrentUser().getID()).child("proposedExpenses").child(expenseID).setValue(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //Elimino gruppo da cache
                GroupsFragment.groups.remove(groupID);

            }

        }
        else
        {
            Log.d (TAG, "Bilancio del gruppo: " + groupID + " non disponibile adesso. Riprovare.");
        }

        return ;

    }

    public void removeGroupFirebase (final String groupID) {

        databaseReference.child("groups").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Se io sono admin posso eliminare il gruppo, altrimenti no
                if (dataSnapshot.child("members").child(getCurrentUser().getID()).child("admin").getValue().equals("true"))
                {
                    Log.d (TAG, "Sono admin, posso eliminare il gruppo");

                    //todo controllare se almeno un membro ha debito/credito verso il gruppo. Se sì visualizzare messaggio per chiedere conferma dell'eliminazione gruppo

                    //For each member of the group
                    for (DataSnapshot memberSnapshot: dataSnapshot.child("members").getChildren())
                    {
                        String memberID = memberSnapshot.getKey();
                        //In user's groups, set this group to deleted
                        databaseReference.child("users").child(memberID).child("groups").child(groupID).setValue(false);
                    }

                    //For each pending expense in the group
                    for (DataSnapshot pendingExpenseSnap: dataSnapshot.child("proposedExpenses").getChildren())
                    {
                        //Delete pending expense
                        String expenseID = pendingExpenseSnap.getKey();
                        databaseReference.child("proposedExpenses").child(expenseID).child("deleted").setValue(true);
                    }


                    //todo aggiornare shared groups

                    //Segno il group come eliminato nei groups
                    databaseReference.child("groups").child(groupID).child("deleted").setValue(true);


                    Toast.makeText(MainActivity.this,"Group successfully removed",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Log.d (TAG, "Non sono admin, non posso eliminare il gruppo");
                    Toast.makeText(MainActivity.this,"You must be admin to remove this group",Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    // check if permissions on reading storage must be asked: true only if API >= 23
    public static boolean shouldAskPermission(){
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    // avoid to call setPersistenceEnabled on a database where it was already called ->
    // when an instance of FirebaseDatabase is needed call this method to retrieve it
    public static FirebaseDatabase getDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }

        return firebaseDatabase;
    }

    // return the instance of the current user logged into the app
    public static User getCurrentUser() {
        return currentUser;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                Toast.makeText(getApplicationContext(), "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* in this way calls to getIntent() will return the latest intent that was used to start this activity
     * rather than the first intent */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        // getting currentUID from Intent (from LoginSignUpActivity or EmailVerificationActivity)
        Intent i = getIntent();
        Bundle extras = i.getExtras();

        if(extras != null) {
            if (extras.containsKey("UID"))
                currentUID = extras.getString("UID");
            else if (currentUID == null) {
                auth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginSignUpActivity.class);
                startActivity(intent);
                finish();
            }

            //       currentUID = "-KjTCeDmpYY7gEOlYuSo"; // mario rossi, tenuto solo per debug, sostituire a riga precedente per vedere profilo con qualcosa
            Log.d(TAG, "currentID: " + currentUID);

            // getting invitation info if coming from LoginSignUpActivity after an Invitation
            if (extras.containsKey("inviterUID")) {
                inviterUID = extras.getString("inviterUID");
                Log.i(TAG, "present inviterUID: " + inviterUID);
            } else {
                inviterUID = null;
            }

            if (extras.containsKey("groupToBeAddedID")) {
                groupToBeAddedID = extras.getString("groupToBeAddedID");
                Log.i(TAG, "present groupToBeAddedID: " + groupToBeAddedID);
            } else {
                groupToBeAddedID = null;
            }
        }


        // getting reference to the user from db
        DatabaseReference currentUserRef = usersRef.child(currentUID);

        if (currentUserRef == null) {
            Log.e(TAG, "unable to retrieve logged user from db");

            Toast.makeText(MainActivity.this, "unable to retrieve logged user from db", Toast.LENGTH_LONG).show();

            // if the current user is not in the database do the logout and restart from login
            auth.signOut();
            currentUID = null;
            Intent intent = new Intent(getApplicationContext(), LoginSignUpActivity.class);
            if(progressDialog.isShowing())
                progressDialog.dismiss();
            startActivity(intent);
            finish();
        }


        // control if there is a request to join a group
        if (groupToBeAddedID != null) {
            if (inviterUID != null) {
                if (!currentUser.getUserGroups().containsKey(groupToBeAddedID)) {
                    currentUser.joinGroup(groupToBeAddedID, inviterUID);
                    Toast.makeText(MainActivity.this, "Now you are part of the group!", Toast.LENGTH_LONG).show();
                } else
                    Log.i(TAG,"You are already part of the group " + groupToBeAddedID);
            }
        }
        else
            // control if ther is a friend request
            if (inviterUID != null) {
                if(!currentUser.getUserFriends().containsKey(inviterUID)){
                    currentUser.addFriend(inviterUID);
                    Toast.makeText(MainActivity.this, "Now you have a new friend!", Toast.LENGTH_LONG).show();
                }
                else
                    Log.i(TAG, "You and "+inviterUID+" are already friends!");
            }

        // attach a listener on all the current user data
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");

                // in the main we don't want an expansible bar
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
                appBarLayout.setExpanded(false);
                //todo: capire come bloccare la barra nel main

                /*if(!progressDialog.isShowing())
                    progressDialog.show();*/

                currentUser = new User();
                currentUser.setID(currentUID);
                currentUser.setName(dataSnapshot.child("name").getValue(String.class));
                currentUser.setSurname(dataSnapshot.child("surname").getValue(String.class));
                currentUser.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                currentUser.setEmail(dataSnapshot.child("email").getValue(String.class));
                currentUser.setPassword(dataSnapshot.child("password").getValue(String.class));
                currentUser.setUsername(dataSnapshot.child("username").getValue(String.class));
                // get user friends's IDs
                for(DataSnapshot friend : dataSnapshot.child("friends").getChildren()){
                    currentUser.getUserFriends().put(friend.getKey(),null);
                }
                // get user groups's IDs
                for(DataSnapshot group : dataSnapshot.child("groups").getChildren()){
                    currentUser.getUserGroups().put(group.getKey(), null);
                }
                //todo mettere altri dati in myself?

                // load nav menu header data for the current user
                loadNavHeader();
                Log.d(TAG, "logged user name: "+currentUser.getName());
                Log.d(TAG, "logged user surname: "+currentUser.getSurname());

                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
                updateFab(1);

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: come gestire?
                Log.d(TAG, "getting current user failed");

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }
}