package com.nomad.profile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nomad.adapter.ProfileAdapter;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.unity.ShareUnity;
import com.nomad.web.HttpJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.头像
 * 2.用户名
 * 3.textview + fragment 或者 直接显示用户的签到记录(地址 时间 评论)
 */
public class ProfileActivity extends AppCompatActivity implements ProfileAdapter.InnerItemOnclickListener {

    private TextView tvName;
    private ListView listView;
    private Handler handler;
    private List<Map<String, Object>> list;
    private ProfileAdapter adapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        tvName = (TextView) findViewById(R.id.tv_profile_name);
        listView = (ListView) findViewById(R.id.lv_show_profile);
        listView.setEmptyView(findViewById(R.id.empty_list_checkin));

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        tvName.setText(username);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                new ProgressUnity(ProfileActivity.this).dissmissProgressDialog();
                switch (msg.what) {
                    case 1: //resultcode != 200
                        Toast.makeText(ProfileActivity.this, "查询失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: //io错误  json错误
                        Toast.makeText(ProfileActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //查询成功
                        //给listview加simpleadapter
                       /* SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileActivity.this
                                , list
                                , R.layout.item_profile
                                , new String[]{"dateTime", "description", "address"}
                                , new int[]{R.id.tv_checkin_time, R.id.tv_checkin_description, R.id.tv_checkin_address}
                        );*/
                        adapter = new ProfileAdapter(list, ProfileActivity.this);
                        adapter.setOnInnerItemOnClickListener(ProfileActivity.this);
                        listView.setAdapter(adapter);
                        Toast.makeText(ProfileActivity.this, "显示成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 4://点赞更新失败
                        Toast.makeText(ProfileActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 5: //点赞更新成功
                        //更新Listview
                        adapter.notifyDataSetChanged();
                        break;
                    case 6: //分享更新成功 （单独处理share的更新）
                        Bundle bundle = msg.getData();
                        int position = bundle.getInt("position");
                        int share = bundle.getInt("share");
                        list.get(position).put("share", share);
                        //更新Listview
                        adapter.notifyDataSetChanged();
                        break;

                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void onResume() {  //每次从别的界面进来，都要查询一次签到表
        super.onResume();
        new ProgressUnity(this).showProgressDiaglog();
        //根据用户名拿到签到信息
        String protocol = getResources().getString(R.string.url_protocol);
        String host = getResources().getString(R.string.url_host);
        String port = getResources().getString(R.string.url_port);
        String path = getResources().getString(R.string.url_path_checkin);
        //get方式提交
        final String url = protocol + "://" + host + ":" + port + "/" + path
                + "?" + "username=" + username + "&method=getCheckins";
        Log.d("Amap", "profileactivity->oncreate()===url:" + url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpJson httpJson = new HttpJson(url);
                try {
                    String stringJson = httpJson.doHttpGetJson();
                    if (httpJson.getResultCode() != 200) {
                        Log.d("Amap", "profileactivity->oncreate()->run()==resultcode:" + httpJson.getResultCode());
                        handler.sendEmptyMessage(1);
                    } else {
                        //1.解析jsonarray数据 2.填充到listview  simpleadapter
                        JSONArray jsonArray = new JSONArray(stringJson);
                        Log.d("Amap", "profileactivity->oncreate()->run()==jsonArray:" + jsonArray);
                        list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<>();
                            map.put("dateTime", jsonObject.get("dateTime"));
                            map.put("description", jsonObject.get("description"));
                            map.put("address", jsonObject.get("address"));
                            map.put("approve", jsonObject.get("approve"));
                            map.put("comment", jsonObject.get("comments"));
                            map.put("share", jsonObject.get("share"));
                            map.put("id", jsonObject.get("id")); //用于根据id更新approve和comment share
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
    public void itemClick(View v) {
        final int position;
        position = (Integer) v.getTag();
        final int id = (int) list.get(position).get("id");
        switch (v.getId()) {
            case R.id.tv_profile_approve:
                //后台修改数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String protocol = getResources().getString(R.string.url_protocol);
                        String host = getResources().getString(R.string.url_host);
                        String port = getResources().getString(R.string.url_port);
                        String path = getResources().getString(R.string.url_path_checkin);

                        int approve = (int) list.get(position).get("approve");  //todo approve应该放在服务器端的全局变量上，多用户并发访问
                        String url = protocol + "://" + host + ":" + port + "/" + path
                                + "?" + "id=" + id + "&approve=" + (approve+1) + "&method=updateApprove";
                        Log.d("Amap", "profileactivity->itemclick->run()===updateapprove===url:" + url);
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                handler.sendEmptyMessage(1);
                                Log.d("Amap", "profileactivity->itemclick()===resultcode:" + httpJson.getResultCode());
                            } else {
                                JSONObject jsonObject = new JSONObject(stringJson);
                                if (jsonObject.getInt("message") == 0) { //0:赞更新成功
                                    approve++;
                                    list.get(position).put("approve", approve); //hashmap值覆盖修改approve
                                    handler.sendEmptyMessage(5);
                                } else {
                                    handler.sendEmptyMessage(4);
                                }
                            }
                        } catch (IOException | JSONException e) {
                            handler.sendEmptyMessage(2);
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.tv_profile_comment:
                //1.跳转到显示用户签到记录的留言模块
                //2.用户可以留言  用户留言更新留言数目
                Intent intent = new Intent(this, MessageActivity.class);
                int comment = (int) list.get(position).get("comment");
                intent.putExtra("cId", id);
                intent.putExtra("comment", comment);
                startActivity(intent);
                break;
            case R.id.tv_profile_share:
                //调用新浪分享  分享成功更新分享数目
                int share = (int) list.get(position).get("share");
                //Date time = (Date) list.get(position).get("dateTime");
                //String time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
                String time1 = (String) list.get(position).get("dateTime");
                String address = (String) list.get(position).get("address");
                String description = (String) list.get(position).get("description");
//                String text = "用户名：" + username + "\n签到日期：" + time1 + "\n签到地址：" + address + "\n说说：" + description;
                Map<String, Object> map = new HashMap<>();
                map.put("address", address);
                map.put("username", username);
                map.put("time1", time1);
                map.put("description", description);

                ShareUnity.share(this, map, position, id, ++share, handler);
                break;
        }
    }
}
