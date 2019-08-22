package com.montini.expensesapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DisplayItemsAdapter extends ArrayAdapter<HelperClass3items> {


    Activity context;
    List<HelperClass3items> listWithDisplayItems;

    public DisplayItemsAdapter(Activity context, List<HelperClass3items> listWithDisplayItems){
        super(context,R.layout.list_item_layout, listWithDisplayItems);
        this.context = context;
        this.listWithDisplayItems = listWithDisplayItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_item_layout, null, true);


        TextView textmainItem = listViewItem.findViewById(R.id.textmainItem);
        TextView textnextItem = listViewItem.findViewById(R.id.textnextItem);


        HelperClass3items ItemInQuestion = listWithDisplayItems.get(position);

        textmainItem.setText(ItemInQuestion.getMainItem());
        textnextItem.setText(ItemInQuestion.getNextItem());

        return listViewItem;
    }


}