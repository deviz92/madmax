package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.InsetDivider;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class GroupsFragment extends Fragment implements GroupsViewAdapter.ListItemClickListener, GroupsViewAdapter.ListItemLongClickListener {

    private static final String TAG = GroupsFragment.class.getSimpleName();
    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = FirebaseUtils.getDatabaseReference();
    private OnItemClickInterface onClickGroupInterface;
    private OnItemLongClickInterface onLongClickGroupInterface;

    public static TreeMap<String, Group> groups = new TreeMap<>(Collections.reverseOrder());
    //private Double totBalance;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private GroupsViewAdapter groupsViewAdapter;

    private ValueEventListener groupListener;
    private ArrayList<String> listenedGroups = new ArrayList<>();

    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickGroupInterface = onItemClickInterface;
        onLongClickGroupInterface = onItemLongClickInterface;
    }

    public GroupsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (TAG, "OnCreate from " + getActivity().getLocalClassName());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        final View view = inflater.inflate(R.layout.skeleton_list, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        RecyclerView.ItemDecoration divider = new InsetDivider.Builder(getContext())
                .orientation(InsetDivider.VERTICAL_LIST)
                .dividerHeight(getResources().getDimensionPixelSize(R.dimen.divider_height))
                .color(ContextCompat.getColor(getContext(), R.color.colorDivider))
                .insets(getResources().getDimensionPixelSize(R.dimen.divider_inset), 0)
                .overlay(true)
                .build();

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);

        groups.clear();
        groupsViewAdapter = new GroupsViewAdapter(this.getContext(), this, this, groups, GroupsFragment.TAG);
        recyclerView.setAdapter(groupsViewAdapter);

        //Ascolto i gruppi dello user
        databaseReference.child("users").child(MainActivity.getCurrentUID()).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Per ogni gruppo dello user
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    //Se il gruppo è true, ossia è ancora tra quelli dello user
                    try{
                    Boolean trueGroup = (Boolean) groupSnapshot.getValue();
                    if (trueGroup)
                        getGroupAndBalance(MainActivity.getCurrentUID(), groupSnapshot.getKey());
                    else
                    {
                        //tolgo il gruppo da quelli che verranno stampati, così lo vedo sparire realtime
                        groups.remove(groupSnapshot.getKey());
                        groupsViewAdapter.update(groups);
                        groupsViewAdapter.notifyDataSetChanged();

                    }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Log.d(TAG, groupSnapshot.getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d (TAG, "OnStart from " + getActivity());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(String groupID) {
        Log.d(TAG, "clickedItemIndex " + groupID);
        onClickGroupInterface.itemClicked(getClass().getSimpleName(), groupID);
    }

    @Override
    public boolean onListItemLongClick (String friendID, View v) {
        Log.d(TAG, "longClickedItemIndex " + friendID);
        onLongClickGroupInterface.itemLongClicked(getClass().getSimpleName(), friendID, v);
        return true;
    }


/*    void getGroupAndBalance (final String userID, final String groupID)
    {

        //final HashMap <String, Double> totalBalance = new HashMap<>();
        final HashMap <String, Expense> groupExpenses = new HashMap<>();
        //totalBalance.put(userID,0d);
        //totBalance = 0d;
        final HashMap <String, Double> totBalances = new HashMap<>();
        totBalances.clear();

        groupListener = databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot groupDataSnapshot) {

                //totalBalance.put(userID,0d);
                totBalances.clear();
                if (!listenedGroups.contains(groupID))
                    listenedGroups.add(groupID);

                final Boolean deleted = groupDataSnapshot.child("deleted").getValue(Boolean.class);

                if (deleted != null)
                {
                    for (DataSnapshot groupExpenseSnapshot: groupDataSnapshot.child("expenses").getChildren())
                    {
                        //Contribuiscono al bilancio solo le spese non eliminate dal gruppo
                        if (groupExpenseSnapshot.getValue(Boolean.class) == true)
                        {
                            //Adesso sono sicuro che la spesa non è stata eliminata
                            //Ascolto la singola spesa del gruppo
                            String expenseID = groupExpenseSnapshot.getKey();
                            databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Boolean involved = false; //dice se user contribuisce o no a quella spesa

                                    for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                                    {
                                        //todo poi gestire caso in cui utente viene tolto dai participant alla spesa
                                        if (participantSnapshot.getKey().equals(userID))
                                            involved = true;
                                    }

                                    //se user ha partecipato alla spesa
                                    if (involved)
                                    {
                                        //alreadyPaid = soldi già messi dallo user per quella spesa
                                        //dueImport = quota che user deve mettere per quella spesa
                                        //balance = credito/debito dello user per quella spesa
                                        for (DataSnapshot d : dataSnapshot.child("participants").getChildren())
                                        {
                                            Log.d (TAG, "PartCampo " + d.getKey() + ": " + d.getValue() );
                                        }
                                        Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);

                                        Log.d (TAG, "Fraction: " + Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())));

                                        Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * dataSnapshot.child("amount").getValue(Double.class);
                                        if (alreadyPaid != null && dueImport != null)
                                        {
                                            Double balance = alreadyPaid - dueImport;
                                            String currency = dataSnapshot.child("currency").getValue(String.class);
                                            //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo

                                            //current balance for that currency
                                            Double temp = totBalances.get(currency);
                                            //update balance for that currency
                                            if (temp != null)
                                            {
                                                totBalances.put(currency, temp + balance);
                                             //   Log.d (TAG, "Actual debt for " + groupName + ": " + totBalances.get(currency) + " " + currency);
                                            }
                                            else
                                            {
                                                totBalances.put(currency, balance);
                                             //   Log.d (TAG, "Actual debt for " + groupName + ": " + totBalances.get(currency) + " " + currency);

                                            }
                                            //Double currentBalance = totalBalance.get(userID);
                                            //totalBalance.put(userID, currentBalance+balance);
                                            //totBalance += balance;

                                            Expense expense = new Expense();
                                            expense.setID(dataSnapshot.getKey());
                                            expense.setCurrency(dataSnapshot.child("currency").getValue(String.class));
                                            expense.setAmount(dataSnapshot.child("amount").getValue(Double.class));

                                            groupExpenses.put(dataSnapshot.getKey(), expense);

                                            Log.d(TAG, "added expense " + dataSnapshot.getKey());
                                        }
                                        else
                                        {
                                            Log.d (TAG, "Qui sarebbe crashato");
                                        }


                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    Group group = new Group();
                    group.setName(groupDataSnapshot.child("name").getValue(String.class));
                    group.setImage(groupDataSnapshot.child("image").getValue(String.class));
                    group.setDeleted(deleted);
                    group.setBalance(0d);
                    group.setCurrencyBalances(totBalances);
                    group.setExpenses(groupExpenses);

                    if (!deleted &&
                            groupDataSnapshot.child("members").hasChild(MainActivity.getCurrentUID()) &&
                            !groupDataSnapshot.child("members").child(MainActivity.getCurrentUID()).child("deleted").getValue(Boolean.class)) {
                        groups.put(groupID, group);
                    }
                    else
                    {
                        groups.remove(groupID);
                    }

//                    Group nullGroup = new Group();
//                    nullGroup.setID(groupID + 1);
//                    groups.put(nullGroup.getID(), nullGroup);

                    groupsViewAdapter.update(groups);
                    groupsViewAdapter.notifyDataSetChanged();
                    //totBalance = 0d;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        return ;
    }*/


    // versione di ale
    ///versione più complessa dell getGroup. Quest'ultima non verrà più usata.
    //oltre al nome gruppo, prende anche il bilancio dello user col gruppo
    void getGroupAndBalance (final String userID, final String groupID)
    {

        //final HashMap <String, Double> totalBalance = new HashMap<>();
        final HashMap <String, Expense> groupExpenses = new HashMap<>();
        //totalBalance.put(userID,0d);
        //totBalance = 0d;
        final HashMap <String, Double> totBalances = new HashMap<>();
        totBalances.clear();

        groupListener = databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot groupDataSnapshot) {

                //totalBalance.put(userID,0d);
                totBalances.clear();
                if (!listenedGroups.contains(groupID))
                    listenedGroups.add(groupID);

                final String groupName = groupDataSnapshot.child("name").getValue(String.class);
                final Boolean deleted = groupDataSnapshot.child("deleted").getValue(Boolean.class);

                if (deleted != null)
                {
                    for (DataSnapshot groupExpenseSnapshot: groupDataSnapshot.child("expenses").getChildren())
                    {
                        //Contribuiscono al bilancio solo le spese non eliminate dal gruppo
                        if (groupExpenseSnapshot.getValue(Boolean.class) == true)
                        {
                            //Adesso sono sicuro che la spesa non è stata eliminata
                            //Ascolto la singola spesa del gruppo
                            String expenseID = groupExpenseSnapshot.getKey();
                            databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Boolean involved = false; //dice se user contribuisce o no a quella spesa

                                    for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                                    {
                                        //todo poi gestire caso in cui utente viene tolto dai participant alla spesa
                                        if (participantSnapshot.getKey().equals(userID))
                                            involved = true;
                                    }

                                    //se user ha partecipato alla spesa
                                    if (involved)
                                    {
                                        //alreadyPaid = soldi già messi dallo user per quella spesa
                                        //dueImport = quota che user deve mettere per quella spesa
                                        //balance = credito/debito dello user per quella spesa
                                        for (DataSnapshot d : dataSnapshot.child("participants").getChildren())
                                        {
                                            Log.d (TAG, "PartCampo " + d.getKey() + ": " + d.getValue() );
                                        }
                                        Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);

                                        //If has been made to avoid crash
                                        if (dataSnapshot.child("participants").child(userID).hasChild("fraction") && dataSnapshot.hasChild("amount"))
                                        {
                                            Log.d (TAG, "Fraction: " + Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())));

                                            Double dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(userID).child("fraction").getValue())) * dataSnapshot.child("amount").getValue(Double.class);
                                            if (alreadyPaid != null && dueImport != null)
                                            {
                                                Double balance = alreadyPaid - dueImport;
                                                String currency = dataSnapshot.child("currency").getValue(String.class);
                                                //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo

                                                //current balance for that currency
                                                Double temp = totBalances.get(currency);
                                                //update balance for that currency
                                                if (temp != null)
                                                {
                                                    totBalances.put(currency, temp + balance);
                                                    Log.d (TAG, "Actual debt for " + groupName + ": " + totBalances.get(currency) + " " + currency);
                                                }
                                                else
                                                {
                                                    totBalances.put(currency, balance);
                                                    Log.d (TAG, "Actual debt for " + groupName + ": " + totBalances.get(currency) + " " + currency);

                                                }
                                                //Double currentBalance = totalBalance.get(userID);
                                                //totalBalance.put(userID, currentBalance+balance);
                                                //totBalance += balance;

                                                Expense expense = new Expense();
                                                expense.setID(dataSnapshot.getKey());
                                                expense.setCurrency(dataSnapshot.child("currency").getValue(String.class));
                                                expense.setAmount(dataSnapshot.child("amount").getValue(Double.class));

                                                groupExpenses.put(dataSnapshot.getKey(), expense);

                                                Log.d(TAG, "added expense " + dataSnapshot.getKey());
                                            }
                                            else
                                            {
                                                Log.d (TAG, "Qui sarebbe crashato");
                                            }
                                        }




                                    }

                                    Group g = new Group();
                                    g.setName(groupName);
                                    //g.setBalance(totalBalance.get(userID));
                                    g.setCurrencyBalances(totBalances);
                                    g.setExpenses(groupExpenses);
                                    g.setDeleted(deleted);
                                    g.setImage(groupDataSnapshot.child("image").getValue(String.class));
                                    //g.setBalance(totBalance);
                                    //se il gruppo non è deleted e io faccio ancora parte del gruppo
                                    if (!deleted &&
                                            groupDataSnapshot.child("members").hasChild(MainActivity.getCurrentUID()) &&
                                            !groupDataSnapshot.child("members").child(MainActivity.getCurrentUID()).child("deleted").getValue(Boolean.class)
                                            )
                                    {
                                        groups.put(groupID, g);

                                    }
                                    else
                                    {
                                        groups.remove(groupID);
                                    }

                                    groupsViewAdapter.update(groups);
                                    groupsViewAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    //Per gestire il caso di quando ho appena abbandonato o eliminato il gruppo
                    //if (dataSnapshot.child("members").hasChild(userID))
                    //{
                    Group g = new Group();
                    g.setName(groupName);
                    g.setImage(groupDataSnapshot.child("image").getValue(String.class));
                    g.setBalance(0d);
                    g.setDeleted(deleted);
                    if (!deleted &&
                            groupDataSnapshot.child("members").hasChild(MainActivity.getCurrentUID()) &&
                            !groupDataSnapshot.child("members").child(MainActivity.getCurrentUID()).child("deleted").getValue(Boolean.class)) {
                        groups.put(groupID, g);
                    }
                    else
                    {
                        groups.remove(groupID);
                    }

//                    Group nullGroup = new Group();
//                    nullGroup.setID(groupID + 1);
//                    groups.put(nullGroup.getID(), nullGroup);

                    groupsViewAdapter.update(groups);
                    groupsViewAdapter.notifyDataSetChanged();
                    //totBalance = 0d;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        return ;
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d (TAG, "OnStop from " + getActivity());
        //databaseReference.child("groups").child(groupID).removeEventListener(groupListener);
        //todo come gestire il fatto che ci sono più listener da eliminare?
        //Elimino una alla volta tutti i listener istanziati
        /*for (String groupID : listenedGroups)
        {
            databaseReference.child("groups").child(groupID).removeEventListener(groupListener);
            Log.d (TAG, "Detached listener on " + groupID);
        }*/

    }



}