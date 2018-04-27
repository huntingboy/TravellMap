package com.nomad.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nomad.travellmap.R;
import com.nomad.unity.BitmapUnity;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-26.
 */

public class AroundAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> list;
    ViewHolder holder;
    //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/travellmap/";// 文件目录
    File fileDir;
    File file;
    String path = "/sdcard/travellmap/";

    public AroundAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
        /**
         * 文件目录如果不存在，则创建
         */
        fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
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
        public ImageView imageView;
        public TextView tvCity;
        public TextView tvAddress;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_around, null);
            holder.imageView = convertView.findViewById(R.id.image_around_picture);
            holder.tvCity = convertView.findViewById(R.id.tv_around_city);
            holder.tvAddress = convertView.findViewById(R.id.tv_around_address);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        String city = (String) list.get(position).get("city");
        String address = (String) list.get(position).get("address");
        //String photoName = (String) list.get(position).get("photoName");
        //photoName += "1.png";
        String photoPath = (String) list.get(position).get("photoPath"); //https:/png.icons8.com/material/2x/spinner-frame-4.png
        //得到正确的url地址
        int i = photoPath.indexOf('/');
        if (photoPath.charAt(i + 1) != '/') {
            String[] str = photoPath.split(":");
            photoPath = str[0] + ":/" + str[1];
        }
        holder.tvCity.setText(city);
        holder.tvAddress.setText(address);
        /**
         * 创建图片文件
         */
        //file = new File(fileDir, photoName);
        new DownloadShowImage(holder.imageView).execute(photoPath);
        /*if (!file.exists() || "1.png".equals(photoName)) {// 如果本地图片不存在则从网上下载
            downloadPic(photoName, photoPath);
        } else {// 图片存在则填充到listview上
            Bitmap bitmap = BitmapFactory
                    .decodeFile(file.getAbsolutePath());
            holder.imageView.setImageBitmap(bitmap);
        }*/
        return convertView;
    }

    public class DownloadShowImage extends AsyncTask<String, Void, Bitmap>{

        private ImageView imageView;

        public DownloadShowImage(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUnity.downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
                    imageView.setImageDrawable(placeholder);
                }
            }
        }


    }
}
