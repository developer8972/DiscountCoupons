package com.example.vpoorn001c.ble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
* Created by vpoorn001c on 5/3/15.
*/
public class CustomAdapter extends BaseAdapter {

    ArrayList<Data> dataList = new ArrayList<>();
    Context context;

    public CustomAdapter(Context context, ArrayList<Data> datas) {
        this.context = context;
        NotesDatabaseAdapter helper = new NotesDatabaseAdapter(context);
        dataList = datas;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Data getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.listview_item,null);
        TextView couponName = (TextView)view.findViewById(R.id.coupon_name);
        couponName.setText(dataList.get(position).getCouponName());
        TextView couponDesc = (TextView)view.findViewById(R.id.coupon_desc);
        couponDesc.setText(dataList.get(position).getCouponDesc());
        TextView couponExpiry = (TextView)view.findViewById(R.id.coupon_expiry);
        couponExpiry.setText(dataList.get(position).getCouponExpiry());


        return view;
    }
}
