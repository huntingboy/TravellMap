package com.nomad.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.nomad.geocode.GeoCode;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.web.HttpJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nomad on 18-4-8.
 * 添加围栏信息  当前用户名， 中心点（经度，维度）， 半径， 地址
 */
@SuppressLint("ValidFragment")
public class AddFenceFragment extends Fragment implements View.OnClickListener, AMap.OnMapClickListener {

    private Handler handler;
    private double latitude;
    private double longitude;
    private int radius;

    private EditText editLatitude;
    private EditText editLongitude;
    private EditText editRadius;
    private GeoCode geoCode; //为了获取逆地理编码后的地址

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";

    public AddFenceFragment(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private AMap aMap1;
    private MapView mapView1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_fence, container, false);
        editLatitude = view.findViewById(R.id.edit_latitude);
        editLongitude = view.findViewById(R.id.edit_longitude);
        editRadius = view.findViewById(R.id.edit_radius);
        Button btAck = view.findViewById(R.id.bt_ack_addfence);
        Button btCan = view.findViewById(R.id.bt_cancel_addfence);
        btAck.setOnClickListener(this);
        btCan.setOnClickListener(this);

        mapView1 = view.findViewById(R.id.map_fence);
        mapView1.onCreate(savedInstanceState);
        if (aMap1 == null) {
            aMap1 = mapView1.getMap();
        }
        aMap1.setTrafficEnabled(true);
        aMap1.showBuildings(true);
        aMap1.showIndoorMap(true);
        UiSettings uiSettings = aMap1.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setScaleControlsEnabled(true);
        aMap1.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(latitude, longitude), 18, 30, 0)));
        aMap1.setOnMapClickListener(this);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:  //resultcode!=200
                        Toast.makeText(getActivity(), "添加出错！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:  //io错误 json转换出错
                        Toast.makeText(getActivity(), "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(getActivity(), "添加失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        //把添加成功的围栏显示到地图，仅仅显示，并不是真正的创建了一个围栏，下面的线程才是
                        aMap1.addCircle(new CircleOptions()
                                .center(new LatLng(latitude, longitude))
                                .radius(radius)
                                .fillColor(Color.argb(100, 1, 1, 1))
                                .strokeColor(Color.argb(100, 1, 1, 1))
                                .strokeWidth(15));
                        Toast.makeText(getActivity(), "添加成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(getActivity(), "创建围栏失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
                new ProgressUnity(getActivity()).dissmissProgressDialog();
                super.handleMessage(msg);
            }
        };
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_ack_addfence:
                //todo   is activity running报错
                //new ProgressUnity(getActivity()).showProgressDiaglog();
                //判断半径输入框是不是数字
                Pattern p = Pattern.compile("[0-9]*");
                String textRadius = editRadius.getText().toString();
                Matcher m = p.matcher(textRadius);

                if ("".equals(editLatitude.getText().toString().trim())) {
                    new ProgressUnity(getActivity()).dissmissProgressDialog();
                    editLatitude.setError("输入有误！");
                    editLatitude.requestFocus();
                } else if ("".equals(editLongitude.getText().toString().trim())) {
                    new ProgressUnity(getActivity()).dissmissProgressDialog();
                    editLongitude.setError("输入有误！");
                    editLongitude.requestFocus();
                } else if (!m.matches() || "".equals(textRadius)) {
                    new ProgressUnity(getActivity()).dissmissProgressDialog();
                    editRadius.setError("输入有误！");
                    editRadius.requestFocus();
                } else {
                    radius = Integer.parseInt(editRadius.getText().toString());
                    String address;
                    try {
                        address = URLEncoder.encode(geoCode.getAddress(), "UTF-8");  //android不像浏览器，需要手动编码url中的中文参数，不然会乱码
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("Amap", "AddFenceFragment->onclickack===latitude:" + latitude + ", longitude:" + longitude + ", radius:" + radius + ", address:" + address);
                    //url  新开线程httpjson resultcode stringjson  解析stringjson里的message(0:成功)  给handler发送信息 创建围栏
                    //todo 此处应该为事务 1. 插入数据库  2.客户端创建围栏
                    String protocol = getResources().getString(R.string.url_protocol);
                    String host = getResources().getString(R.string.url_host);
                    String port = getResources().getString(R.string.url_port);
                    String path = getResources().getString(R.string.url_path_fence);
                    final String url = protocol + "://" + host + ":" + port + "/" + path
                            + "?" + "username=" + MainActivity.username + "&latitude=" + latitude + "&longitude=" + longitude
                            + "&radius=" + radius + "&address=" + address + "&method=addFence";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            HttpJson httpJson = new HttpJson(url);
                            try {
                                String stringJson = httpJson.doHttpGetJson();
                                if (httpJson.getResultCode() != 200) {
                                    Log.d("Amap", "AddfenceFragment->onclick->new runnable->run()=== resultcode:" + httpJson.getResultCode());
                                    handler.sendEmptyMessage(1);
                                } else {
                                    JSONObject jsonObject = new JSONObject(stringJson);
                                    int message = jsonObject.getInt("message");
                                    if (message != 0) { //0：添加成功 1:添加失败（e.g 围栏已经存在）
                                        handler.sendEmptyMessage(3);
                                    } else {   //数据库插入成功

                                        //创建自定义围栏  圆形围栏
                                        MainActivity.mGeoFenceClient.addGeoFence(new DPoint(latitude, longitude), radius, "我的围栏");
                                        GeoFenceListener fenceListenter = new GeoFenceListener() {

                                            @Override
                                            public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {
                                                if(i == GeoFence.ADDGEOFENCE_SUCCESS){//判断围栏是否创建成功
                                                    //list就是已经添加的围栏列表，可据此查看创建的围栏
                                                    handler.sendEmptyMessage(4);  //成功
                                                } else {
                                                    //list就是已经添加的围栏列表
                                                    //todo 从数据库删除刚才添加成功的围栏
                                                    handler.sendEmptyMessage(5);
                                                }
                                            }
                                        };
                                        //设置回调监听
                                        MainActivity.mGeoFenceClient.setGeoFenceListener(fenceListenter);
                                    }
                                }
                            } catch (IOException | JSONException e) {
                                handler.sendEmptyMessage(2);
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                break;
            case R.id.bt_cancel_addfence:
                new ProgressUnity(getActivity()).dissmissProgressDialog();
                //清空地图，清空文本框
                aMap1.clear(true);
                editLongitude.setText("");
                editLatitude.setText("");
                editRadius.setText("");
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        aMap1.clear(true);
        geoCode = new GeoCode(getActivity(), latLng, aMap1);
        editLatitude.setText(String.valueOf(latLng.latitude));
        editLongitude.setText(String.valueOf(latLng.longitude));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView1.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView1.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView1.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView1.onSaveInstanceState(outState);
    }

}
