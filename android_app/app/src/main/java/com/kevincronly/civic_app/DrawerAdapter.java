package com.kevincronly.civic_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by kevin.cronly on 6/10/15.
 */
public class DrawerAdapter extends ArrayAdapter<String>
{
    private static final String TAG = "DrawerAdapter";

    public DrawerAdapter(Context context, String[] objects)
    {
        super(context, 0, objects);
        Log.i(TAG, "DrawerAdapter constructor");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String temp = getItem(position);
        Log.i(TAG, "get View got " + temp);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
        }


        TextView drawerItem = (TextView) convertView.findViewById(R.id.drawer_item);
        drawerItem.setText(temp);

        return convertView;
    }

}
