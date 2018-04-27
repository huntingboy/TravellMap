package com.nomad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nomad.travellmap.R;

import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-25.
 * 留言的适配器  为了可以点击图像进入个人中心
 */

public class MessageAdapter extends BaseAdapter implements View.OnClickListener{

    private Context context;
    private List<Map<String, Object>> list;
    private InnerItemOnclickListener mListener;

    public MessageAdapter(List<Map<String, Object>> list, Context context) {
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
        public TextView tvUsername;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, null);
            holder.imageHeader = convertView.findViewById(R.id.image_message_header);
            holder.tvUsername = convertView.findViewById(R.id.tv_message_username);
            holder.tvTime = convertView.findViewById(R.id.tv_message_time);
            holder.tvDescription = convertView.findViewById(R.id.tv_message_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String time1 = (String) list.get(position).get("time");
        String desciption = (String) list.get(position).get("description");
        String username = (String) list.get(position).get("username");
        holder.tvTime.setText(time1);
        holder.tvUsername.setText(username);
        holder.tvDescription.setText(desciption);
        holder.imageHeader.setOnClickListener(this);
        holder.imageHeader.setTag(position);
        return convertView;
    }


    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }
}
