package com.nomad.around;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nomad.adapter.CommentAroundAdapter;
import com.nomad.profile.ProfileActivity;
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
import java.util.Objects;
import java.util.Vector;

/**
 * Created by nomad on 18-4-12.
 * 周边评论表：id 经纬度 city address username description time approve disapprove
 */

public class CommentActivity extends AppCompatActivity implements View.OnClickListener, CommentAroundAdapter.InnerItemOnclickListener{

    private ImageView imageLocation;  //todo 换为景点的图片
    private TextView tvCity, tvAddress;
    private ListView listView;
    private EditText editText;
    private Button btComment;
    private Handler handler;
    private List<Map<String, Object>> list;
    /**
     * listview item内部事件的点击 1.可以通过重写baseadapter ，写一个内部监听接口 实现  2.设置内部控件focus=false,listview设置before
     */
  //  private SimpleAdapter simpleAdapter; 需要使用自定义的adapter，实现listview内部Item点击事件的监听   否则要么拿不到点击item的数据，要么无法响应Item事件监听，二者不可兼得
    private CommentAroundAdapter mAdapter;
    private double latitude;
    private double longitude;
    private String city;
    private String address;
    private int firstId; //记录用户临时添加的评论的id，等于数据库中最后一条记录id+1

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        tvCity = (TextView) findViewById(R.id.tv_comment_city);
        tvAddress = (TextView) findViewById(R.id.tv_comment_address);
        listView = (ListView) findViewById(R.id.lv_show_comment);
        listView.setEmptyView(findViewById(R.id.empty_list_comment));
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //需要listview控件设置android:descendantFocusability="×××Descendants"和item控件设置focusable=false(button)，才可以响应该事件。
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Amap", "listview->onitemclick()");
            }
        });*/
        editText = (EditText) findViewById(R.id.edit_comment);
        btComment = (Button) findViewById(R.id.bt_comment);
        btComment.setOnClickListener(this);
        new ProgressUnity(this).showProgressDiaglog();
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        city = intent.getStringExtra("city");
        address = intent.getStringExtra("address");
        tvCity.setText(city);
        tvAddress.setText(address);

        /**
         * 1.根据经纬度从数据库拿到用户留言信息(新开线程http get方式)
         * 2.给listview添加用户留言数据
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                String protocol = getResources().getString(R.string.url_protocol);
                String host = getResources().getString(R.string.url_host);
                String port = getResources().getString(R.string.url_port);
                String path = getResources().getString(R.string.url_path_comment);
                //get方式提交
                String url = protocol + "://" + host + ":" + port + "/" + path
                        + "?" + "latitude=" + latitude + "&longitude=" + longitude + "&method=getComments";
                HttpJson httpJson = new HttpJson(url);
                try {
                    String stringJson = httpJson.doHttpGetJson();
                    if (httpJson.getResultCode() != 200) {
                        handler.sendEmptyMessage(1);
                        Log.d("Amap", "commentactivity->oncreate()===resultcode:" + httpJson.getResultCode());
                    } else {
                        JSONArray jsonArray = new JSONArray(stringJson);
                        list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<>();
                            map.put("username", jsonObject.get("username"));
                            map.put("time", jsonObject.get("time"));
                            map.put("description", "\t\t" + jsonObject.get("description"));
                            map.put("approve", jsonObject.get("approve"));
                            map.put("disapprove", jsonObject.get("disapprove"));
                            map.put("id", jsonObject.get("id")); //用于根据id更新approve和disapprove
                            if (i == 0) {
                                firstId = (int) jsonObject.get("id") + 1;
                            }
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
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                new ProgressUnity(CommentActivity.this).dissmissProgressDialog();
                switch (msg.what) {
                    case 1: // resultcode != 200
                        Toast.makeText(CommentActivity.this, "查询出错", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: // io错误  Json解析错误
                        Toast.makeText(CommentActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: // 查询成功
                        /*simpleAdapter = new SimpleAdapter(CommentActivity.this,
                                list,
                                R.layout.item_comment,
                                new String[]{"username", "time", "description", "approve", "disapprove"},
                                new int[]{R.id.tv_comment_username, R.id.tv_comment_time, R.id.tv_comment_description, R.id.tv_comment_approve, R.id.tv_comment_disapprove});*/
                        mAdapter = new CommentAroundAdapter(CommentActivity.this, list);
                        mAdapter.setOnInnerItemOnClickListener(CommentActivity.this);
                        listView.setAdapter(mAdapter);
                        break;
                    case 4: //评论失败
                        Toast.makeText(CommentActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 5: //评论成功
                        //edittext清空
                        editText.setText("");
                        //更新Listview
                        mAdapter.notifyDataSetChanged();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.bt_comment:
                //1.把评论信息提交到服务器 2.刷新listview
                if (MainActivity.username == null) {
                    Toast.makeText(this, "你还没有登录", Toast.LENGTH_SHORT).show();
                }else {
                    final String description = editText.getText().toString().trim();
                    if (description.length() > 0) {
                        new ProgressUnity(this).showProgressDiaglog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String protocol = getResources().getString(R.string.url_protocol);
                                String host = getResources().getString(R.string.url_host);
                                String port = getResources().getString(R.string.url_port);
                                String path = getResources().getString(R.string.url_path_comment);
                                String username = MainActivity.username;
                                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                String dateTime = URLEncoder.encode(time);
                                //get方式提交
                                String url = protocol + "://" + host + ":" + port + "/" + path
                                        + "?" + "latitude=" + latitude + "&longitude=" + longitude +
                                        "&city=" + URLEncoder.encode(city) + "&address=" + URLEncoder.encode(address) +
                                        "&username=" + username + "&time=" + dateTime + "&description=" + URLEncoder.encode(description) + "&method=addComment";
                                HttpJson httpJson = new HttpJson(url);
                                try {
                                    String stringJson = httpJson.doHttpGetJson();
                                    if (httpJson.getResultCode() != 200) {
                                        handler.sendEmptyMessage(1);
                                        Log.d("Amap", "commentactivity->onclick()===resultcode:" + httpJson.getResultCode());
                                    } else {
                                        JSONObject jsonObject = new JSONObject(stringJson);
                                        if (jsonObject.getInt("message") == 0) { //0:评论成功
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("username", username);
                                            map.put("time", time);
                                            map.put("description", "\t\t" + description);
                                            map.put("approve", 0);
                                            map.put("disapprove", 0);
                                            map.put("id", firstId);
                                            list.add(0, map); //改变List，用于后面adapter通知listview更新
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
                    }
                }
                break;
        }
    }

    /**
     * 自己在适配器中定义的回调接口
     * @param v
     */
    @Override
    public void itemClick(View v) {
        final int position;
        position = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.image_comment_header:
                Log.e("内部item-->", position + "");
                //跳转到用户主页profileactivity
                Intent intent = new Intent(CommentActivity.this, ProfileActivity.class);
                //传入用户名
                String username = (String) list.get(position).get("username");
                intent.putExtra("username", username);
                startActivity(intent);
                break;
            case R.id.tv_comment_approve:
                //后台修改数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String protocol = getResources().getString(R.string.url_protocol);
                        String host = getResources().getString(R.string.url_host);
                        String port = getResources().getString(R.string.url_port);
                        String path = getResources().getString(R.string.url_path_comment);
                        int id = (int) list.get(position).get("id");
                        int approve = (int) list.get(position).get("approve");
                        String url = protocol + "://" + host + ":" + port + "/" + path
                                + "?" + "id=" + id + "&approve=" + (approve+1) + "&method=updateApprove";
                        Log.d("Amap", "commentactivity->itemclick->run()===updateapprove===url:" + url);
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                handler.sendEmptyMessage(1);
                                Log.d("Amap", "commentactivity->onclick()===resultcode:" + httpJson.getResultCode());
                            } else {
                                JSONObject jsonObject = new JSONObject(stringJson);
                                if (jsonObject.getInt("message") == 0) { //0:评论更新成功
                                    approve++;
                                    list.get(position).put("approve", approve); //hashmap值覆盖修改disapprove
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
            case R.id.tv_comment_disapprove:
                //后台修改数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String protocol = getResources().getString(R.string.url_protocol);
                        String host = getResources().getString(R.string.url_host);
                        String port = getResources().getString(R.string.url_port);
                        String path = getResources().getString(R.string.url_path_comment);
                        int id = (int) list.get(position).get("id");
                        int disapprove = (int) list.get(position).get("disapprove");
                        String url = protocol + "://" + host + ":" + port + "/" + path
                                + "?" + "id=" + id + "&disapprove=" + (disapprove+1) + "&method=updateDisapprove";
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                handler.sendEmptyMessage(1);
                                Log.d("Amap", "commentactivity->onclick()===resultcode:" + httpJson.getResultCode());
                            } else {
                                JSONObject jsonObject = new JSONObject(stringJson);
                                if (jsonObject.getInt("message") == 0) { //0:评论更新成功
                                    disapprove++;
                                    list.get(position).put("disapprove", disapprove); //hashmap值覆盖修改disapprove
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
        }
    }
}
