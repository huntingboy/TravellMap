package com.nomad.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.web.HttpJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-8.
 * 显示围栏信息  中心点（经度，维度）， 半径， 地址
 */

public class ViewFenceFragment extends Fragment {

    private Handler handler;
    private ListView listView;
    private List<Map<String, Object>> list;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //new ProgressUnity(getActivity()).showProgressDiaglog();
        View view = inflater.inflate(R.layout.fragment_view_fence, container, false);
        listView = view.findViewById(R.id.lv_show_fence);
        //根据username查询所有的围栏信息
        String protocol = getResources().getString(R.string.url_protocol);
        String host = getResources().getString(R.string.url_host);
        String port = getResources().getString(R.string.url_port);
        String path = getResources().getString(R.string.url_path_fence);
        //get方式提交
        final String url = protocol + "://" + host + ":" + port + "/" + path
                + "?" + "username=" + MainActivity.username + "&method=getFences";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpJson httpJson = new HttpJson(url);
                try {
                    String stringJson = httpJson.doHttpGetJson();
                    if (httpJson.getResultCode() != 200) {
                        Log.d("Amap", "viewfencefragment->oncreateview()->run()==resultcode:" + httpJson.getResultCode());
                        handler.sendEmptyMessage(1);
                    } else {
                        //1.解析jsonarray数据 2.填充到listview  simpleadapter
                        JSONArray jsonArray = new JSONArray(stringJson);
                        Log.d("Amap", "viewfencefragment->oncreateview()->run()==jsonArray:" + jsonArray);
                        list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<>();
                            map.put("latitude", jsonObject.get("latitude"));
                            map.put("longitude", jsonObject.get("longitude"));
                            map.put("radius", jsonObject.get("radius"));
                            map.put("address", jsonObject.get("address"));
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

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://resultcode != 200
                        Toast.makeText(getActivity(), "查询失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2://io异常  json转换错误
                        Toast.makeText(getActivity(), "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3://成功
                        listView.setAdapter(new SimpleAdapter(getActivity()
                                , list
                                , R.layout.item_fence
                                , new String[]{"latitude", "longitude", "radius", "address"}
                                , new int[]{R.id.tv_fence_latitude, R.id.tv_fence_longitude, R.id.tv_fence_radius, R.id.tv_fence_address}));
                        Toast.makeText(getActivity(), "查询成功！", Toast.LENGTH_SHORT).show();

                }
                new ProgressUnity(getActivity()).dissmissProgressDialog();

                super.handleMessage(msg);
            }
        };

        return view;
    }

}
