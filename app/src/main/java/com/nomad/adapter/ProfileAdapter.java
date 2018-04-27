package com.nomad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nomad.travellmap.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-22.
 * 个人简介的适配器，为了内部的textview可以点击
 */

public class ProfileAdapter extends BaseAdapter implements View.OnClickListener {
    private List<Map<String, Object>> list;
    private Context context;
    private InnerItemOnclickListener mListener;

    public ProfileAdapter(List<Map<String, Object>> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        public ImageView imageHeader;
        public TextView tvTime;
        public TextView tvDescription;
        public ImageView imageLocation;
        public TextView tvAddress;
        public TextView tvApprove;
        public TextView tvComment;
        public TextView tvShare;
    }
    public interface InnerItemOnclickListener {  //需要用户实现， 实质就是onclicklistener，只不过把内部控件的position传过去了，用户可以根据list<map<>>和position拿到点击项的数据
        void itemClick(View v);
    }
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_profile, null);
            holder.imageHeader = convertView.findViewById(R.id.image_profile_header);
            holder.tvTime = convertView.findViewById(R.id.tv_profile_time);
            holder.tvDescription = convertView.findViewById(R.id.tv_profile_description);
            holder.imageLocation = convertView.findViewById(R.id.image_profile_location);
            holder.tvAddress = convertView.findViewById(R.id.tv_profile_address);
            holder.tvApprove = convertView.findViewById(R.id.tv_profile_approve);
            holder.tvComment = convertView.findViewById(R.id.tv_profile_comment);
            holder.tvShare = convertView.findViewById(R.id.tv_profile_share);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String time1 = (String) list.get(position).get("dateTime");
        String desciption = (String) list.get(position).get("description");
        String tvAddress = (String) list.get(position).get("address");
        int approve = (int) list.get(position).get("approve");
        int comment = (int) list.get(position).get("comment");
        int share = (int) list.get(position).get("share");
        holder.tvTime.setText(time1);
        holder.tvDescription.setText(desciption);
        holder.tvAddress.setText(tvAddress);
        holder.tvApprove.setText(String.valueOf(approve));
        holder.tvComment.setText(String.valueOf(comment));
        holder.tvShare.setText(String.valueOf(share));

        holder.tvApprove.setOnClickListener(this);
        holder.tvComment.setOnClickListener(this);
        holder.tvShare.setOnClickListener(this);

        holder.tvApprove.setTag(position);
        holder.tvComment.setTag(position);
        holder.tvShare.setTag(position);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        mListener.itemClick(v);  //把item的position通过setTag(positon)传给用户，用户通过Positoin拿到数据实现该方法
    }

}
