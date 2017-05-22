package com.polito.mad17.madmax.activities.expenses;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.OnItemClickInterface;
import com.polito.mad17.madmax.activities.OnItemLongClickInterface;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

import static com.polito.mad17.madmax.activities.groups.GroupExpensesActivity.expenses;
import static com.polito.mad17.madmax.activities.groups.NewGroupActivity.groups;

public class ExpensesFragment extends Fragment implements ExpensesViewAdapter.ListItemClickListener, ExpensesViewAdapter.ListItemLongClickListener {

    private static final String TAG = ExpensesFragment.class.getSimpleName();

    private OnItemClickInterface onClickFriendInterface;
    private OnItemLongClickInterface onLongClickGroupInterface;


    public void setInterface(OnItemClickInterface onItemClickInterface, OnItemLongClickInterface onItemLongClickInterface) {
        onClickFriendInterface = onItemClickInterface;
        onLongClickGroupInterface = onItemLongClickInterface;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ExpensesViewAdapter expensesViewAdapter;

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private DatabaseReference groupRef;

    private String groupID = null;

    private HashMap<String, Expense> expensesMap = new HashMap<>();
    //String myselfID = "-KjTCeDmpYY7gEOlYuSo";


    public ExpensesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        String groupID;
        DatabaseReference groupRef;

        //firebaseDatabase = FirebaseDatabase.getInstance();
        //databaseReference = firebaseDatabase.getReference();


        setInterface((OnItemClickInterface) getActivity(), (OnItemLongClickInterface) getActivity());

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        Bundle bundle = getArguments();
        groupID = bundle.getString("groupID");

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        expensesViewAdapter = new ExpensesViewAdapter(this, this, expensesMap);
        recyclerView.setAdapter(expensesViewAdapter);

        groupRef = databaseReference.child("groups");

        Log.d(TAG, groupID);
        // retrieving group details for current group
        groupRef.child(groupID).child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot expensesSnapshot) {

                for(DataSnapshot expense : expensesSnapshot.getChildren())
                {
                    getExpense(expense.getKey());
                    Log.d(TAG, expense.getKey());
                }

                expensesViewAdapter.update(expensesMap);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        Log.d(TAG, "dopo setAdapter");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
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
    public void onListItemClick(String expenseID) {
        Log.d(TAG, "clickedItemIndex " + expenseID);
        onClickFriendInterface.itemClicked(getClass().getSimpleName(), expenseID);
    }

    @Override
    public boolean onListItemLongClick (String friendID, View v) {
        Log.d(TAG, "longClickedItemIndex " + friendID);
        onLongClickGroupInterface.itemLongClicked(getClass().getSimpleName(), friendID, v);
        return true;
    }

    public void getExpense(final String id) {
        databaseReference.child("expenses").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d (TAG, "Spesa: " + dataSnapshot.getKey());
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    Log.d (TAG, "Campo " + d.getKey() + ": " + d.getValue());
                }
                Log.d(TAG, " ");


                //Se io sono tra i participant e la spesa non è stata eliminata
                if (dataSnapshot.child("participants").hasChild(MainActivity.getCurrentUser().getID()) &&
                        dataSnapshot.hasChild("deleted") &&
                        !dataSnapshot.child("deleted").getValue(Boolean.class))
                {
                    Expense expense = new Expense();
                    expense.setDescription(dataSnapshot.child("description").getValue(String.class));
                    expense.setAmount(dataSnapshot.child("amount").getValue(Double.class));
                    expense.setCurrency(dataSnapshot.child("currency").getValue(String.class));
                    expensesMap.put(id, expense);
                    expensesViewAdapter.update(expensesMap);
                    expensesViewAdapter.notifyDataSetChanged();
                }
                //Se user non è più participant della spesa
                else
                {
                    expensesMap.remove(id);
                    expensesViewAdapter.update(expensesMap);
                    expensesViewAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
}