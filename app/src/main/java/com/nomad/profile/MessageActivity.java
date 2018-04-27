package com.nomad.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nomad.adapter.MessageAdapter;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.web.HttpJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-23.
 * 个人中心的留言模块   image  username time description
 */

public class MessageActivity extends AppCompatActivity implements View.OnClickListener, MessageAdapter.InnerItemOnclickListener {

    private ListView listView;
    private EditText editText;
    private Button button;
    private Handler handler;
    private List<Map<String, Object>> list;
    private MessageAdapter adapter;
//    private SimpleAdapter simpleAdapter;
    private int cId; //哪一条签到记录的留言
    private int comment; //用于跟新留言数量

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        listView = (ListView) findViewById(R.id.lv_show_message);
        listView.setEmptyView(findViewById(R.id.empty_list_message));
        editText = (EditText) findViewById(R.id.edit_message);
        button = (Button) findViewById(R.id.bt_message);
        button.setOnClickListener(this);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                new ProgressUnity(MessageActivity.this).dissmissProgressDialog();
                switch (msg.what) {
                    case 1: // resultcode != 200
                        Toast.makeText(MessageActivity.this, "查询出错", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: // io错误  Json解析错误
                        Toast.makeText(MessageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: // 查询成功
                        //填充到Listview
                        /*simpleAdapter = new SimpleAdapter(MessageActivity.this,
                                list,
                                R.layout.item_message,
                                new String[]{"username", "time", "description"},
                                new int[]{R.id.tv_message_username, R.id.tv_message_time, R.id.tv_message_description});*/
                        adapter = new MessageAdapter(list, MessageActivity.this);
                        adapter.setOnInnerItemOnClickListener(MessageActivity.this);
                        listView.setAdapter(adapter);
                        break;
                    case 4:  //评论失败
                        Toast.makeText(MessageActivity.this, "留言失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 5: //留言成功
                        editText.setText("");
                        //simpleAdapter.notifyDataSetChanged();  //更新Listview
                        adapter.notifyDataSetChanged();
                        break;

                }
                super.handleMessage(msg);
            }
        };

        new ProgressUnity(this).showProgressDiaglog();
        Intent intent = getIntent();
        cId = intent.getIntExtra("cId", 0);
        comment = intent.getIntExtra("comment", 0);
        //服务器获取留言数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                String protocol = getResources().getString(R.string.url_protocol);
                String host = getResources().getString(R.string.url_host);
                String port = getResources().getString(R.string.url_port);
                String path = getResources().getString(R.string.url_path_message);
                String url = protocol + "://" + host + ":" + port + "/" + path
                        + "?" + "cId=" + cId  + "&method=getMessages";
                HttpJson httpJson = new HttpJson(url);
                Log.d("Amap", "messageactivity->oncreate()->run()===url:" + url);
                try {
                    String stringJson = httpJson.doHttpGetJson();
                    if (httpJson.getResultCode() != 200) {
                        Log.d("Amap", "messageactivity->oncreate()->run()==resultcode:" + httpJson.getResultCode());
                        handler.sendEmptyMessage(1);
                    } else {
                        JSONArray jsonArray = new JSONArray(stringJson);
                        list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<>();
                            map.put("username", jsonObject.get("username"));
                            map.put("time", jsonObject.get("time"));
                            map.put("description", "\t\t" + jsonObject.get("description"));
                            list.add(map);
                        }
                        handler.sendEmptyMessage(3);
                    }
                } catch (IOException | JSONException e) {
                    handler.sendEmptyMessage(2);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        //用户点击留言  1.获取留言信息 2.让服务器更新数据库 3.刷新Listview(包括个人中心界面的评论数量+1)
        if (MainActivity.username == null) {
            Toast.makeText(this, "你还没有登录~", Toast.LENGTH_SHORT).show();
        } else {
            final String message = editText.getText().toString().trim();
            if ("".equals(message)) {
                Toast.makeText(this, "请输入留言~", Toast.LENGTH_SHORT).show();
            } else {
                new ProgressUnity(this).showProgressDiaglog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String protocol = getResources().getString(R.string.url_protocol);
                        String host = getResources().getString(R.string.url_host);
                        String port = getResources().getString(R.string.url_port);
                        String path = getResources().getString(R.string.url_path_message);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateTime = sdf.format(new Date());
                        String url = protocol + "://" + host + ":" + port + "/" + path
                                + "?" + "cId=" + cId  + "&username=" + MainActivity.username +
                                "&dateTime=" + URLEncoder.encode(dateTime) + "&description=" + URLEncoder.encode(message) +
                                "&comment=" + (comment + 1) + "&method=addMessage";
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                Log.d("Amap", "messageactivity->onclick()->run()==resultcode:" + httpJson.getResultCode());
                                handler.sendEmptyMessage(1);
                            } else {
                                int msg = new JSONObject(stringJson).getInt("message");
                                if (msg != 0) { //留言失败
                                    handler.sendEmptyMessage(4);
                                } else { //留言成功
                                    comment++;  //记住comment+1  用户连续评论
                                    //更新adapter和List
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("username", MainActivity.username);
                                    map.put("time", dateTime);
                                    map.put("description", "\t\t" + message);
                                    list.add(0, map);
                                    handler.sendEmptyMessage(5);
                                }
                            }
                        } catch (IOException | JSONException e) {
                            handler.sendEmptyMessage(2);
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

    }

    @Override
    public void itemClick(View v) {
        final int position;
        position = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.image_message_header:
                //跳转到用户主页profileactivity
                Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
                //传入用户名
                String username = (String) list.get(position).get("username");
                intent.putExtra("username", username);
                startActivity(intent);
                break;
        }
    }
}
