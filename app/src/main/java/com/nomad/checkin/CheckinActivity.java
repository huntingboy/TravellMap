package com.nomad.checkin;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.nomad.geocode.GeoCode;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.web.HttpJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckinActivity extends Activity implements View.OnClickListener {

    private static final int RESULT_CHECKIN_OK = 3;
    private EditText editCheckin;
    private TextView tvCheckinAddress;
    private Button btAck;
    private Button btCan;
    private Handler handler;

    private double latitude;
    private double longitude;
    private GeoCode geoCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        editCheckin = findViewById(R.id.edit_checkin);
        tvCheckinAddress = findViewById(R.id.tv_checkin_address);
        btAck = findViewById(R.id.bt_ack_checkin);
        btCan = findViewById(R.id.bt_cancel_checkin);
        btAck.setOnClickListener(this);
        btCan.setOnClickListener(this);
        //1.intent解析经纬度  2.逆地理编码出地址
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(CheckinActivity.this, "签到出错！", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        CheckinActivity.this.setResult(RESULT_CHECKIN_OK);
                        finish();
                        break;
                    case 2:
                        Toast.makeText(CheckinActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //把逆地理编码解析的地址address显示到tvCheckinAddress
                        Log.d("Amap", "checkinactivity->handler===address:" + geoCode.getAddress());
                        tvCheckinAddress.setText(geoCode.getAddress());
                        break;
                }
                super.handleMessage(msg);
            }
        };
        geoCode = new GeoCode(this, new LatLng(latitude, longitude), handler);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("Amap", "checkinactivity->onresume()===address:" + geoCode.getAddress()); //address=null
//        tvCheckinAddress.setText(geoCode.getAddress());   //来不及显示解析出address
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_ack_checkin:
                //1.获取text,latitude,longitude,address  2.新开线程http get方式传送json数据 3.根据返回的结果执行相应操作
                final String text = URLEncoder.encode(editCheckin.getText().toString().trim());
                Log.d("Amap", "checkinactivity->onclick()===text:" + text);
                final String address = URLEncoder.encode(geoCode.getAddress());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String protocol = getResources().getString(R.string.url_protocol);
                        String host = getResources().getString(R.string.url_host);
                        String port = getResources().getString(R.string.url_port);
                        String path = getResources().getString(R.string.url_path_checkin);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                        String dateTime1 = sdf.format(new Date());
                        String dateTime = URLEncoder.encode(dateTime1);
                        //get方式提交
                        String url = protocol + "://" + host + ":" + port + "/" + path
                                + "?" + "username=" + MainActivity.username + "&latitude=" + latitude
                        + "&longitude=" + longitude + "&address=" + address + "&dateTime=" + dateTime
                                + "&description=" + text + "&method=checkin";
                        Log.d("Amap", "checkinactivity->onclick()->run()===url:" + url);
                        Log.d("Amap", "checkinactivity->onclick()->run()===datetime:" + dateTime1);

                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                Log.d("Amap", "====checkinactivity->onclick()->run() 签到resultcode:" + httpJson.getResultCode());
                                handler.sendEmptyMessage(0);
                            } else {
                                /*Looper.prepare(); //不加会报错，子线程不会自动创建looper,但是toast依赖handler,handler依赖looper
                                Toast.makeText(getActivity(), "获取验证码失败！", Toast.LENGTH_SHORT).show();
                                Looper.loop();*/
                                JSONObject jsonObject = new JSONObject(stringJson);
                                if (jsonObject.getInt("message") == 0) { //0:成功
                                    handler.sendEmptyMessage(1);
                                } else {
                                    handler.sendEmptyMessage(2);
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            /*Looper.prepare(); //不加会报错，子线程不会自动创建looper,但是toast依赖handler,handler依赖looper
                            Toast.makeText(getActivity(), "网络错误！", Toast.LENGTH_SHORT).show();
                            Looper.loop();*/
                            handler.sendEmptyMessage(2);
                        }
                    }
                }).start();
                break;
            case R.id.bt_cancel_checkin:
                finish();
                break;
        }
    }
}
