package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.DetailFragment;
import com.polito.mad17.madmax.activities.SettingsFragment;
import com.polito.mad17.madmax.entities.CropCircleTransformation;
import com.polito.mad17.madmax.entities.Group;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList mData;
    private static final String TAG = GroupsViewAdapter.class.getSimpleName();

    // OnClick handler to help the Activity easier to interface with RecyclerView
    final private ListItemClickListener itemClickListener;
    private ListItemLongClickListener itemLongClickListener = null;
    private Context context;

    private String callingActivity;

    private LayoutInflater layoutInflater;
    DecimalFormat df = new DecimalFormat("#.##");
    private HashMap<String, Double> totBalances = new HashMap<>();

    Map.Entry<String, Group> nullEntry = new Map.Entry<String, Group>() {
        @Override
        public String getKey() {
            return "0";
        }

        @Override
        public Group getValue() {
            return null;
        }

        @Override
        public Group setValue(Group value) {
            return null;
        }
    };

    // The interface that receives the onClick messages
    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }
    //The interface that receives the onLongClick messages
    public interface ListItemLongClickListener {
        boolean onListItemLongClick(String clickedItemIndex, View v);
    }

    public GroupsViewAdapter(Context context, ListItemClickListener listener, Map<String, Group> map, String callingActivity) {
        this.context = context;
        itemClickListener = listener;
        this.callingActivity = callingActivity;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add(nullEntry);
    }

    public GroupsViewAdapter(Context context, ListItemClickListener listener, ListItemLongClickListener longListener, Map<String, Group> map,
                             String callingActivity) {
        this.context = context;
        itemClickListener = listener;
        itemLongClickListener = longListener;
        this.callingActivity = callingActivity;
        mData = new ArrayList();
        mData.addAll(map.entrySet());
        mData.add(nullEntry);
    }

    public void update(Map<String, Group> map) {
        mData.clear();
        mData.addAll(map.entrySet());
        mData.add(nullEntry);
    }

    class ItemGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imageView;
        private TextView nameTextView;
        private TextView balanceTextTextView;
        private TextView balanceTextView;

        public ItemGroupViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_sender);
            balanceTextTextView = (TextView) itemView.findViewById(R.id.tv_balance_text);
            balanceTextView = (TextView) itemView.findViewById(R.id.tv_balance);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            Map.Entry<String, Group> itemClicked = getItem(clickedPosition);

            Log.d(TAG, "clickedGroup " + itemClicked.getKey());

            if(!itemClicked.getKey().equals("0")) {
                itemClickListener.onListItemClick(itemClicked.getKey());
            }
        }

        @Override
        public boolean onLongClick (View v) {
            int clickedPosition = getAdapterPosition();
            Map.Entry<String, Group> itemClicked = getItem(clickedPosition);
            Log.d(TAG, "longClickedGroup " + itemClicked.getKey());

            if(!itemClicked.getKey().equals("0")) {
                itemLongClickListener.onListItemLongClick(itemClicked.getKey(), v);
            }
            return true;

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        return new ItemGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final ItemGroupViewHolder groupViewHolder = (ItemGroupViewHolder) holder;

        Map.Entry<String, Group> item = getItem(position);

        if(position == (mData.size() - 1))
        {
            Log.d(TAG, "item.getKey().equals(\"nullGroup\")");
            groupViewHolder.imageView.setVisibility(View.GONE);
            groupViewHolder.nameTextView.setText("");
            groupViewHolder.balanceTextTextView.setText("");
            groupViewHolder.balanceTextView.setText("");
        }
        else {

            Log.d(TAG, "item ID " + item.getKey() + " item name " + item.getValue().getName());

            //String p = groups.get(String.valueOf(position)).getImage();
            groupViewHolder.imageView.setVisibility(View.VISIBLE);
            String p = item.getValue().getImage();
            if (p != null && !p.equals("noImage"))
            {
                Log.d(TAG, "Image not null " + p);
                Glide.with(layoutInflater.getContext()).load(p)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(groupViewHolder.imageView);
            } else {

                Glide.with(layoutInflater.getContext()).load(R.drawable.group_default)
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(layoutInflater.getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(groupViewHolder.imageView);
            }

            groupViewHolder.nameTextView.setText(item.getValue().getName());

            //mydebt = mio debito con il gruppo
            String groupname = item.getValue().getName();

            totBalances = item.getValue().getCurrencyBalances();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultCurrency = sharedPref.getString(SettingsFragment.DEFAULT_CURRENCY, "");

            Boolean multipleCurrencies = false;
            Double shownBal;
            String shownCurr;

            if (groupname.equals("fff") || groupname.equals("Regalo"))
            {
                Log.d(TAG, "sii");
            }

            Log.d(TAG, "callingActivity " + callingActivity);



            if(!callingActivity.equals("FriendDetailActivity") && !callingActivity.equals("ChooseGroupActivity")) {
                if (totBalances != null) {

                    groupViewHolder.balanceTextTextView.setVisibility(View.VISIBLE);
                    groupViewHolder.balanceTextView.setVisibility(View.VISIBLE);
                    for (Map.Entry<String, Double> entry : totBalances.entrySet()) {
                        Log.d(TAG, "Bilancio in " + groupname + " : " + entry.getValue() + " " + entry.getKey());
                    }

                    if (!totBalances.isEmpty()) {
                        //If there is more than one currency
                        if (totBalances.size() > 1) {
                            multipleCurrencies = true;

                        }
                        //If there is just one currency
                        else {
                            multipleCurrencies = false;
                        }

                        if (totBalances.containsKey(defaultCurrency)) {
                            shownBal = totBalances.get(defaultCurrency);
                            shownCurr = defaultCurrency;
                        } else {
                            shownCurr = (String) totBalances.keySet().toArray()[0];
                            shownBal = totBalances.get(shownCurr);
                        }

                        //Print balance
                        if (shownBal > 0) {
                            groupViewHolder.balanceTextTextView.setText(R.string.you_should_receive);
                            groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));


                            if (multipleCurrencies)
                                groupViewHolder.balanceTextView.setText(df.format(shownBal) + " " + shownCurr + "*");
                            else
                                groupViewHolder.balanceTextView.setText(df.format(shownBal) + " " + shownCurr);

                            groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                        } else if (shownBal < 0) {
                            groupViewHolder.balanceTextTextView.setText(R.string.you_owe);
                            groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));


                            if (multipleCurrencies)
                                groupViewHolder.balanceTextView.setText(df.format(Math.abs(shownBal)) + " " + shownCurr + "*");
                            else
                                groupViewHolder.balanceTextView.setText(df.format(Math.abs(shownBal)) + " " + shownCurr);

                            groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

                        } else if (shownBal == 0) {
                            groupViewHolder.balanceTextTextView.setText(R.string.no_debts);
                            groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                            groupViewHolder.balanceTextView.setText("0 " + defaultCurrency);
                            groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                        }

                    }
                    groupViewHolder.balanceTextTextView.setVisibility(View.VISIBLE);
                    groupViewHolder.balanceTextView.setVisibility(View.VISIBLE);
                }
                //If there are no balances in the map
                else {
                    groupViewHolder.balanceTextTextView.setText(R.string.no_debts);
                    groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    groupViewHolder.balanceTextView.setText("0 " + defaultCurrency);
                    groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    groupViewHolder.balanceTextTextView.setVisibility(View.VISIBLE);
                    groupViewHolder.balanceTextView.setVisibility(View.VISIBLE);
                }
            }
            if (callingActivity.equals(DetailFragment.class.getSimpleName()))
            {
                groupViewHolder.balanceTextTextView.setVisibility(View.GONE);
                groupViewHolder.balanceTextView.setVisibility(View.GONE);
            }
            /*
            else
            {
                groupViewHolder.balanceTextTextView.setVisibility(View.GONE);
                groupViewHolder.balanceTextView.setVisibility(View.GONE);
            }
            */



            // check if there are expenses with other currencies than the default
            /*Boolean defaultCurrencyExpense = false;
            Boolean otherCurrenciesPresent = false;
            Double groupBalanceDefaultCurrency = 0.0d;
            HashMap<String, Double> otherCurrenciesBalance = new HashMap<>();

            HashMap<String, Expense> groupExpenses = item.getValue().getExpenses();

            if(groupExpenses != null) {
                for (Map.Entry<String, Expense> expenseEntry : groupExpenses.entrySet()) {
                    String currentCurrency = expenseEntry.getValue().getCurrency();

                    if (currentCurrency.equals(defaultCurrency)) {
                        groupBalanceDefaultCurrency += expenseEntry.getValue().getAmount();
                        defaultCurrencyExpense = true;
                    } else {
                        otherCurrenciesPresent = true;
                        if (otherCurrenciesBalance.containsKey(currentCurrency)) {
                            Double currentBalance = otherCurrenciesBalance.get(currentCurrency);
                            currentBalance += otherCurrenciesBalance.get(currentCurrency);
                            otherCurrenciesBalance.put(currentCurrency, currentBalance);
                        } else {
                            otherCurrenciesBalance.put(currentCurrency, expenseEntry.getValue().getAmount());
                        }
                    }
                }

                if (defaultCurrencyExpense) {
                    if (groupBalanceDefaultCurrency == null) {
                        groupViewHolder.balanceTextTextView.setText("");
                        groupViewHolder.balanceTextView.setText("");
                        return;
                    }

                    if (groupBalanceDefaultCurrency > 0) {
                        groupViewHolder.balanceTextTextView.setText(R.string.credit_of);
                        groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

                        String balance = df.format(Math.abs(groupBalanceDefaultCurrency)) + " " + defaultCurrency;
                        if (otherCurrenciesPresent) {
                            balance += " *";
                        }

                        groupViewHolder.balanceTextView.setText(balance);
                        groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                    } else {
                        if (groupBalanceDefaultCurrency < 0) {
                            groupViewHolder.balanceTextTextView.setText(R.string.debt_of);
                            groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                            String balance = df.format(Math.abs(groupBalanceDefaultCurrency)) + " " + defaultCurrency;
                            if (otherCurrenciesPresent) {
                                balance += " *";
                            }

                            groupViewHolder.balanceTextView.setText(balance);
                            groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                        } else if (!otherCurrenciesPresent) {
                            groupViewHolder.balanceTextTextView.setText(R.string.no_debts);
                            groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                            groupViewHolder.balanceTextView.setText("0.0 " + defaultCurrency);
                            groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                        } else {
                            Map.Entry<String, Double> entry = otherCurrenciesBalance.entrySet().iterator().next();

                            if (entry.getValue() > 0) {
                                groupViewHolder.balanceTextTextView.setText(R.string.credit_of);
                                groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

                                String balance = df.format(Math.abs(entry.getValue())) + " " + entry.getKey();

                                groupViewHolder.balanceTextView.setText(balance);
                                groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                            } else {
                                groupViewHolder.balanceTextTextView.setText(R.string.debt_of);
                                groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                                String balance = df.format(Math.abs(entry.getValue())) + " " + entry.getKey();

                                groupViewHolder.balanceTextView.setText(balance);
                                groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                            }
                        }
                    }
                }
            }
            else
            {
                groupViewHolder.balanceTextTextView.setText(R.string.no_debts);
                groupViewHolder.balanceTextTextView.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                groupViewHolder.balanceTextView.setText("0.0 " + defaultCurrency);
                groupViewHolder.balanceTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            }*/
        }
    }


    public Map.Entry<String, Group> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
