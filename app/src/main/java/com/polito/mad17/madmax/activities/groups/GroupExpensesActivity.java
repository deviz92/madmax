package com.polito.mad17.madmax.activities.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.expenses.NewExpenseActivity;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;

import java.util.HashMap;

// todo controllare se viene usata

public class GroupExpensesActivity extends AppCompatActivity {
    public static HashMap<String, Expense> expenses = new HashMap<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_group_expenses);

        final String IDGroup;

        Intent intent = getIntent();
        // if starting activity from itself (adding a new expense)
        if (intent.getBooleanExtra("addExpenseToGroup", true)) {
            IDGroup = intent.getStringExtra("IDGroup");

            String description = intent.getStringExtra("description");
            String amount = intent.getStringExtra("amount");
            String currency = intent.getStringExtra("currency");
//            expenses.put(String.valueOf(i), new Expense(String.valueOf(i), description + " " + currency + " " + amount, (double) i+1));

            Group group = MainActivity.getCurrentUser().getUserGroups().get(IDGroup);
            expenses = group.getExpenses();

            //todo a cosa serve questa expense?? quale utente la fa?
            /*Expense e = new Expense(
                    String.valueOf(expenses.size()), description, null, Double.valueOf(amount),
                    currency, String.valueOf(R.drawable.expense6), "urlBill", false, group.getID(), null, false
            );*/

            // save the new expense
            //expenses.put(String.valueOf(expenses.size()), e);

            //GroupsActivity.users.get(0).addExpense(e);
            //GroupsActivity.myself.addExpense(e);
            //todo mettere addExpenseFirebase
        }
        // if starting activity from GroupActivity (tapping on a group for showing details)
        else {
            IDGroup = intent.getStringExtra("IDGroup");

            Group group = MainActivity.getCurrentUser().getUserGroups().get(IDGroup);
            expenses = group.getExpenses();
        }

        // then show group expenses list

        // todo getting IDGroup from the intent from GroupsActivity

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupExpensesActivity.this, NewExpenseActivity.class);
                intent.putExtra("IDGroup", IDGroup);
                intent.putExtra("callingActivity", "GroupExpensesActivity");
                intent.putExtra("newPending", false);
                GroupExpensesActivity.this.startActivity(intent);

                finish();
                GroupExpensesActivity.this.startActivity(intent);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

/*        //Button to go to group info
        Button groupInfoButton = (Button) findViewById(R.id.infobutton);
        groupInfoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Context context = GroupExpensesActivity.this;
                Class destinationActivity = GroupInfoActivity.class;
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtra("groupID", IDGroup);
                intent.putExtra("caller", "GroupExpensesActivity");
                startActivity(intent);

            }
        });*/

        // todo when we'll use Firebase -> retrieve expenses HashMap with IDGroup received from GroupsActivity

        ListView lv = (ListView) findViewById(R.id.expenses);

        // for putting data inside the list view I need an adapter
        BaseAdapter a = new BaseAdapter() {

            @Override
            public int getCount() {
                return expenses.size();
            }

            @Override
            public Object getItem(int position) {
                return expenses.get(String.valueOf(position));
            }

            @Override // not changed
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // if it's the first time I create the view
                if (convertView == null) {
                    //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LayoutInflater inflater = LayoutInflater.from(GroupExpensesActivity.this);

                    // last parameter is very important: for now we don't want to attach our view to the parent view
                    convertView = inflater.inflate(R.layout.list_item, parent, false);
                }

                Expense expense = expenses.get(String.valueOf(position));

                ImageView photo = (ImageView) convertView.findViewById(R.id.img_photo);
                TextView description = (TextView) convertView.findViewById(R.id.tv_sender);
                TextView amount = (TextView) convertView.findViewById(R.id.tv_balance);

                if (expense.getExpensePhoto() != null)
                    photo.setImageResource(Integer.parseInt(expense.getExpensePhoto()));
                description.setText(expense.getDescription());
                String amountString = expense.getAmount() + " " + expense.getCurrency();
                amount.setText(amountString);

                return convertView;
            }
        };

        lv.setAdapter(a);
    }


}
