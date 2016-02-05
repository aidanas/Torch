package com.aidanas.torch.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aidanas.torch.R;

import java.util.List;

/**
 * Created by Aidanas Tamasauskas
 * Created on 14/01/2016.
 *
 * Custom ListVew ArrayAdapter. Must be used with: layout/navdraw_list_item_layout.xml
 */
public class NavDrawLsAdapter extends BaseAdapter {

    private Context context;
    private List<String> navDrawTitles;

    public NavDrawLsAdapter(Context context, List<String> navDrawTitles){
        this.context = context;
        this.navDrawTitles = navDrawTitles;
    }

    @Override
    public int getCount() {
        return navDrawTitles.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawTitles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.navdraw_list_item_layout, null);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.nav_draw_item_title_tw);

        txtTitle.setText(navDrawTitles.get(position));

        return convertView;
    }

}