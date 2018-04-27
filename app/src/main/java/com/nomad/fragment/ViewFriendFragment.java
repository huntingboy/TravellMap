package com.nomad.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nomad on 18-4-7.
 * 1.显示所有的好友
 * 2.长俺好友删除
 * 3.点击好友，进入好友的profileActivity个人中心（显示好友的签到记录）
 */

public class ViewFriendFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    private Handler handler;
    private ListView listView;
    private String[] strs = {};  //存储好友名，用于填充Listview
    private ArrayList<String> arrayList; //根据arrayList转换得到，用于填充Listview
    private PopupMenu popupMenu;
    private int position;
    private ArrayAdapter arrayAdapter;
    private Context context;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_friend, container, false);
        listView = view.findViewById(R.id.lv_show_friends);
        listView.setEmptyView(((Activity)context).findViewById(R.id.empty_list_friend));
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        new ProgressUnity(context).showProgressDiaglog();
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
                new ProgressUnity(context).dissmissProgressDialog();
                switch (msg.what) {
                    case 1: //响应状态码！=200
                        //解决getActivity()为null: https://www.jianshu.com/p/9d75e328f1de
                        Toast.makeText(context, "查询/删除失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: //查询成功
                        arrayList = new ArrayList<>(Arrays.asList(strs));
                        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayList);
                        listView.setAdapter(arrayAdapter);
                        Toast.makeText(context, "查询成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //io异常
                        Toast.makeText(context, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show();
                        //更新Listview
                        //刷新lsitview界面
                        arrayList.remove(position);
                        arrayAdapter.notifyDataSetChanged();
                        break;
                    case 5:
                        Toast.makeText(context, "删除失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        return view;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        //1.长按弹出文本菜单 2.点击删除从数据库中删除  3.handler刷新listview
        final String username = (String) parent.getItemAtPosition(position);
        if (username != null) {
            popupMenu = new PopupMenu(context, view);
            ((Activity)context).getMenuInflater().inflate(R.menu.popup_menu_friend, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_del_friend:
                            popupMenu.dismiss();
                            String protocol = getResources().getString(R.string.url_protocol);
                            String host = getResources().getString(R.string.url_host);
                            String port = getResources().getString(R.string.url_port);
                            String path = getResources().getString(R.string.url_path_friend);
                            final String url = protocol + "://" + host + ":" + port + "/" + path
                                    + "?" + "username=" + MainActivity.username + "&friend=" + username + "&method=deleteFriend";
                            new ProgressUnity(context).dissmissProgressDialog();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    HttpJson httpJson = new HttpJson(url);
                                    try {
                                        String stringJson = httpJson.doHttpGetJson();
                                        if (httpJson.getResultCode() != 200) {
                                            Log.d("Amap", "viewfenceFragment->onclick->new runnable->run()=== resultcode:" + httpJson.getResultCode());
                                            handler.sendEmptyMessage(1);
                                        } else {
                                            JSONObject jsonObject = new JSONObject(stringJson);
                                            int message = jsonObject.getInt("message"); //0:成功删除
                                            if (message == 0) { //数据库删除成功
                                                ViewFriendFragment.this.position = position;
                                                handler.sendEmptyMessage(4);
                                            } else {
                                                handler.sendEmptyMessage(5);
                                            }
                                        }
                                    }  catch (IOException | JSONException e) {
                                        handler.sendEmptyMessage(3);
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String username = (String) parent.getItemAtPosition(position);
        if (username != null) {
            Intent intent = new Intent(context, ProfileActivity.class);
            //传入用户名 进入好友个人中心
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }
}
