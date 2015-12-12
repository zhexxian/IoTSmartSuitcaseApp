package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MinhBreaker on 12/12/15.
 */
public class GridviewAdapter extends BaseAdapter
{
    private ArrayList<String> listInfo;
    private ArrayList<Integer> listIcon;
    private Activity activity;

    public GridviewAdapter(Activity activity,ArrayList<String> listInfo, ArrayList<Integer> listIcon) {
        super();
        this.listInfo = listInfo;
        this.listIcon = listIcon;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listInfo.size();
    }

    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        return listInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static class ViewHolder
    {
        public ImageView imgViewFlag;
        public TextView txtViewTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        LayoutInflater inflator = activity.getLayoutInflater();

        if(convertView==null)
        {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.gridview_row, null);

            view.txtViewTitle = (TextView) convertView.findViewById(R.id.textView1);
            view.imgViewFlag = (ImageView) convertView.findViewById(R.id.imageView1);

            convertView.setTag(view);
        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }

        view.txtViewTitle.setText(listInfo.get(position));
        view.imgViewFlag.setImageResource(listIcon.get(position));

        return convertView;
    }
}
