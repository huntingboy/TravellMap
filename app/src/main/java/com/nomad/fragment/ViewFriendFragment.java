package com.nomad.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nomad.profile.ProfileActivity;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.web.HttpJson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by nomad on 18-4-7.
 * 1.显示所有的好友
 * 2.长俺好友删除
 * 3.点击好友，进入好友的profileActivity个人中心（显示好友的签到记录）
 */

public class ViewFriendFragment extends Fragment {

    private Handler handler;
    private ListView listView;
    private String[] strs = {};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //new ProgressUnity(getActivity()).showProgressDiaglog();
        View view = inflater.inflate(R.layout.fragment_view_friend, container, false);
        listView = view.findViewById(R.id.lv_show_friends);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String username = (String) listView.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
        //根据username查询所有的好友名
        String protocol = getResources().getString(R.string.url_protocol);
        String host = getResources().getString(R.string.url_host);
        String port = getResources().getString(R.string.url_port);
        String path = getResources().getString(R.string.url_path_friend);
        //get方式提交
        final String url = protocol + "://" + host + ":" + port + "/" + path
                + "?" + "username=" + MainActivity.username + "&method=getFriends";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpJson httpJson = new HttpJson(url);
                try {
                    String stringJson = httpJson.doHttpGetJson();
                    if (httpJson.getResultCode() != 200) {
                        Log.d("Amap", "====ViewFrienndFragment->oncreateView()-> 查询所有好友resultcode:" + httpJson.getResultCode());
                        handler.sendEmptyMessage(1);
                    }else{
                        Log.d("Amap", "====ViewFrienndFragment->oncreateView()-> 查询所有好友stringJson:" + stringJson);

                            JSONArray jsonArray = new JSONArray(stringJson);
                            strs = new String[jsonArray.length()];
                            for (int i = 0; i < jsonArray.length(); i++) {
                                strs[i] = jsonArray.getString(i);
                            }

                        handler.sendEmptyMessage(2);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(3);
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: //响应状态码！=200
                        Toast.makeText(getActivity(), "查询失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: //查询成功
                        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, strs));
                        Toast.makeText(getActivity(), "查询成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //io异常
                        Toast.makeText(getActivity(), "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                }
                new ProgressUnity(getActivity()).dissmissProgressDialog();
                super.handleMessage(msg);
            }
        };

        return view;
    }

}
