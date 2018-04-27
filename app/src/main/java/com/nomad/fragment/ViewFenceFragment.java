package com.nomad.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
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

public class ViewFenceFragment extends Fragment implements AdapterView.OnItemLongClickListener{

    private Handler handler;
    private ListView listView;
    private List<Map<String, Object>> list;
    private PopupMenu popupMenu;
    private int position;  //记录点击的（要删除的）是第几个围栏
    private SimpleAdapter simpleAdapter;
    private Context context;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_fence, container, false);
        listView = view.findViewById(R.id.lv_show_fence);
        listView.setEmptyView(((Activity)context).findViewById(R.id.empty_list_fence));
        listView.setOnItemLongClickListener(this);
        new ProgressUnity(context).showProgressDiaglog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //根据username查询所有的围栏信息
                String protocol = getResources().getString(R.string.url_protocol);
                String host = getResources().getString(R.string.url_host);
                String port = getResources().getString(R.string.url_port);
                String path = getResources().getString(R.string.url_path_fence);
                //get方式提交
                String url = protocol + "://" + host + ":" + port + "/" + path
                        + "?" + "username=" + MainActivity.username + "&method=getFences";
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
                new ProgressUnity(context).dissmissProgressDialog();
                switch (msg.what) {
                    case 1://resultcode != 200
                        Toast.makeText(context, "查询/删除失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2://io异常  json转换错误
                        Toast.makeText(context, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3://查询成功
                        simpleAdapter = new SimpleAdapter(context
                                , list
                                , R.layout.item_fence
                                , new String[]{"latitude", "longitude", "radius", "address"}
                                , new int[]{R.id.tv_fence_latitude, R.id.tv_fence_longitude, R.id.tv_fence_radius, R.id.tv_fence_address});
                        listView.setAdapter(simpleAdapter);
                        Toast.makeText(context, "查询成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 4://删除成功
                        Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show();
                        //刷新lsitview界面
                        list.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                        break;
                    case 5://删除失败
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
        //1.弹出文本菜单  删除选项  2.点击删除，发送Http get请求删除fence 3.从MainActivity.mGeoFenceClient中删除围栏
        final HashMap<String, Object> hashMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
        if (hashMap != null) {
            final double latitude = (double) hashMap.get("latitude");
            final double longitude = (double) hashMap.get("longitude");
            final int radius = (int) hashMap.get("radius");
            popupMenu = new PopupMenu(context, view);
            ((Activity)context).getMenuInflater().inflate(R.menu.popup_menu_fence, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    new ProgressUnity(context).showProgressDiaglog();
                    switch (item.getItemId()) {
                        case R.id.action_del_fence:
                            popupMenu.dismiss();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String protocol = getResources().getString(R.string.url_protocol);
                                    String host = getResources().getString(R.string.url_host);
                                    String port = getResources().getString(R.string.url_port);
                                    String path = getResources().getString(R.string.url_path_fence);
                                    String url = protocol + "://" + host + ":" + port + "/" + path
                                            + "?" + "username=" + MainActivity.username + "&latitude=" + latitude + "&longitude=" + longitude
                                            + "&radius=" + radius + "&method=deleteFence";
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
                                                ViewFenceFragment.this.position = position;
                                                //从fenceclient删除围栏
                                                List<GeoFence> geoFences = MainActivity.mGeoFenceClient.getAllGeoFence();
                                                GeoFence geoFence = geoFences.get(position);
                                                Log.d("Amap", "要删除的围栏：" + geoFence.getCenter().toString() + ", " + geoFence.getRadius());
                                                MainActivity.mGeoFenceClient.removeGeoFence(geoFence);
                                                //从mainactivity中删除画出的该围栏
                                                MainActivity.circles.remove(position);
                                                handler.sendEmptyMessage(4);
                                            } else {
                                                handler.sendEmptyMessage(5);
                                            }
                                        }
                                    }  catch (IOException | JSONException e) {
                                        handler.sendEmptyMessage(2);
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
