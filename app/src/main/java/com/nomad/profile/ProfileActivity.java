package com.nomad.profile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.web.HttpJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
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
public class ProfileActivity extends AppCompatActivity {

    private TextView tvName;
    private ListView listView;
    private Handler handler;
    private List<Map<String, Object>> list;
    //todo 评论功能
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        tvName = (TextView) findViewById(R.id.tv_profile_name);
        listView = (ListView) findViewById(R.id.lv_show_profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        tvName.setText(username);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: //resultcode != 200
                        Toast.makeText(ProfileActivity.this, "查询失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: //io错误  json错误
                        Toast.makeText(ProfileActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //成功
                        //给listview加simpleadapter
                        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileActivity.this
                                , list
                                , R.layout.item_profile
                                , new String[]{"dateTime", "description", "address"}
                                , new int[]{R.id.tv_checkin_time, R.id.tv_checkin_description, R.id.tv_checkin_address}
                        );
                        listView.setAdapter(simpleAdapter);
                        Toast.makeText(ProfileActivity.this, "显示成功！", Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
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
                        Log.d("Amap", "viewfencefragment->oncreateview()->run()==jsonArray:" + jsonArray);
                        list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<>();
                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            //Date dateTime = sdf.parse((String) map.get("dateTime"));
                            String description = (String) jsonObject.get("description");
                            map.put("address", jsonObject.get("address"));
                            map.put("dateTime", jsonObject.get("dateTime"));
                            map.put("description", description);
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
}
