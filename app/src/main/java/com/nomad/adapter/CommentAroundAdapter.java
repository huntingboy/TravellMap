package com.nomad.adapter;

import android.content.Context;
import android.util.Log;
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
 * Created by nomad on 18-4-21.
 * 周边评论的listview适配器， 为了Listview内部点击事件有效
 */

public class CommentAroundAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context;
    private List<Map<String, Object>> list;
    private InnerItemOnclickListener mListener;

    public CommentAroundAdapter(final Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    public class ViewHolder{
        public ImageView imageView;
        public TextView username;
        public TextView time;
        public TextView description;
        public TextView approve;
        public TextView disapprove;
    }
    public interface InnerItemOnclickListener {  //需要用户实现， 实质就是onclicklistener，只不过把内部控件的position传过去了，用户可以根据list<map<>>和position拿到点击项的数据
        void itemClick(View v);
    }
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, null);
            holder.imageView = convertView.findViewById(R.id.image_comment_header);
            holder.username = convertView.findViewById(R.id.tv_comment_username);
            holder.time = convertView.findViewById(R.id.tv_comment_time);
            holder.description = convertView.findViewById(R.id.tv_comment_description);
            holder.approve = convertView.findViewById(R.id.tv_comment_approve);
            holder.disapprove = convertView.findViewById(R.id.tv_comment_disapprove);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String username = (String) list.get(position).get("username");
        Log.d("Amap", "commentaroundadapter点击第" + position + "项");
        String description = (String) list.get(position).get("description");
        String time = (String) list.get(position).get("time");
        int approve = (int) list.get(position).get("approve");
        int disapprove = (int) list.get(position).get("disapprove");
        holder.username.setText(username);
        holder.time.setText(time);
        holder.description.setText(description);
        holder.approve.setText(String.valueOf(approve));
        holder.disapprove.setText(String.valueOf(disapprove));

        holder.imageView.setOnClickListener(this);
        holder.approve.setOnClickListener(this);
        holder.disapprove.setOnClickListener(this);
        holder.imageView.setTag(position);
        holder.approve.setTag(position);
        holder.disapprove.setTag(position);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        mListener.itemClick(v);  //把item的position通过setTag(positon)传给用户，用户通过Positoin拿到数据实现该方法
    }
}
