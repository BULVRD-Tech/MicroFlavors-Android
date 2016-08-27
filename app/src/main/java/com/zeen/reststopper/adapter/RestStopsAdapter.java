package com.zeen.reststopper.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeen.reststopper.R;
import com.zeen.reststopper.models.RestStop;
import com.zeen.reststopper.utils.Constants;

import java.util.List;

/**
 * Created by davidhodge on 8/9/15.
 */
public class RestStopsAdapter extends BaseAdapter {
    private List<RestStop> items;
    private LayoutInflater inflater;
    private Context mContext;
    private boolean useMetric;
    private String PACKAGE_NAME;

    public RestStopsAdapter(Context context, List<RestStop> items, boolean useMetric, String PACKAGE_NAME) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        mContext = context;
        this.useMetric = useMetric;
        this.PACKAGE_NAME = PACKAGE_NAME;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_rest_stop, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.item_loc);
            holder.distance = (TextView) convertView.findViewById(R.id.item_distance);
            holder.image = (ImageView) convertView.findViewById(R.id.item_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (useMetric) {
            holder.distance.setText(new StringBuilder().append(Math.round(items.get(position).distance * 0.001)).append(" km away"));
        } else {
            holder.distance.setText(new StringBuilder().append(Math.round(items.get(position).distance * 0.000621371)).append(" mi away"));
        }

        if(items.get(position).info.contains("RR")){
            holder.image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_restroom));
        }else{
            holder.image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_restroom_no));
        }

        if (TextUtils.equals(PACKAGE_NAME, Constants.HISTORY_PACKAGE)) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(items.get(position).info);
        } else if (TextUtils.equals(PACKAGE_NAME, Constants.THEME_PARK_PACKAGE)) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(items.get(position).info);
        } else if (TextUtils.equals(PACKAGE_NAME, Constants.BREWS_PACKAGE)) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(items.get(position).info);
        } else if (TextUtils.equals(PACKAGE_NAME, Constants.MURICA_PACKAGE)) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(items.get(position).info);
        } else if (TextUtils.equals(PACKAGE_NAME, Constants.CASTLE_PACKAGE)) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(items.get(position).info);
        }else if (TextUtils.equals(PACKAGE_NAME, Constants.WINE_PACKAGE)) {
            if(!TextUtils.equals("Winery", items.get(position).info)) {
                holder.text.setVisibility(View.VISIBLE);
                String value = items.get(position).info;
                value = value.replace(".", "");
                holder.text.setText(value);
            }else{
                holder.text.setVisibility(View.GONE);
            }
        } else {
            holder.text.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        TextView text;
        TextView distance;
        ImageView image;
    }
}
