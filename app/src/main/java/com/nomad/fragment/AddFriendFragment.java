package com.nomad.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.MarkerUnity;
import com.nomad.unity.ProgressUnity;
import com.nomad.web.HttpJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

/**
 * Created by nomad on 18-4-6.
 * 添加好友:
 * 1.填写已经注册该app的qq邮箱
 * 2.获取邮箱验证码，登录邮箱填写验证码
 * 3.http发送json数据给服务器，服务器比对验证码和用户名是否已经注册并作相应的数据库操作，返回结果数据
 * 4.解析获取的结果json数据，给用户提示
 */

public class AddFriendFragment extends Fragment {

    private Handler handler;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);
        final EditText editUsername = view.findViewById(R.id.edit_username);
        final EditText editAuthcode = view.findViewById(R.id.edit_authcode);
        final Button btCodeSend = view.findViewById(R.id.bt_code_send);
        Button btAck = view.findViewById(R.id.bt_ack_addfriend);
        Button btCan = view.findViewById(R.id.bt_cancel_addfriend);
        btCodeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toMail = editUsername.getText().toString();
                String protocol = getResources().getString(R.string.url_protocol);
                String host = getResources().getString(R.string.url_host);
                String port = getResources().getString(R.string.url_port);
                String path = getResources().getString(R.string.url_path_friend);

                if ("".equals(toMail.trim()) || !toMail.contains("@qq.com") || toMail.contains(" ")) {
                    editUsername.setError("qq邮箱格式错误！");
                    return ;
                }
                //get方式提交
                final String url = protocol + "://" + host + ":" + port + "/" + path
                        + "?" + "toMail=" + toMail + "&method=sendAuthcode";
                //新开线程执行网络相关操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() == 200) {
                                handler.sendEmptyMessage(0); //获取验证码成功
                            } else {
                                Log.d("Amap", "====AddFrienndFragment->oncreateView()->onclick() 获取验证码resultcode:" + httpJson.getResultCode());
                                /*Looper.prepare(); //不加会报错，子线程不会自动创建looper,但是toast依赖handler,handler依赖looper
                                Toast.makeText(context, "获取验证码失败！", Toast.LENGTH_SHORT).show();
                                Looper.loop();*/
                                handler.sendEmptyMessage(1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            /*Looper.prepare(); //不加会报错，子线程不会自动创建looper,但是toast依赖handler,handler依赖looper
                            Toast.makeText(context, "网络错误！", Toast.LENGTH_SHORT).show();
                            Looper.loop();*/
                            handler.sendEmptyMessage(2);
                        }
                    }
                }).start();
            }
        });
        btAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String toMail = editUsername.getText().toString();
                String code = editAuthcode.getText().toString();
                String protocol = getResources().getString(R.string.url_protocol);
                String host = getResources().getString(R.string.url_host);
                String port = getResources().getString(R.string.url_port);
                String path = getResources().getString(R.string.url_path_friend);
                if ("".equals(toMail.trim()) || toMail.contains(" ") || !toMail.contains("@qq.com") || toMail.equals(MainActivity.username)){
                    editUsername.setError("qq邮箱格式错误！");
                    return ;
                }else if ("".equals(code.trim()) || code.contains(" ") || code.length() != 4){
                    editAuthcode.setError("验证码格式错误！");
                    return ;
                }

                //get方式提交
                final String url = protocol + "://" + host + ":" + port + "/" + path
                        + "?" + "username=" + MainActivity.username + "&toMail=" + toMail + "&code=" + code + "&method=addFriend";
                new ProgressUnity(context).showProgressDiaglog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                Log.d("Amap", "====AddFrienndFragment->oncreateView()->onclick() 获取验证码resultcode:" + httpJson.getResultCode());
                                handler.sendEmptyMessage(3);
                            } else {

                                JSONObject jsonObject = new JSONObject(stringJson);
                                Log.d("Amap", "服务器加好友返回数据====》" + stringJson);
                                //todo 得到好友的位置信息和最后登录时间  marker显示到地图
                                int message = jsonObject.getInt("message");
                                if (message == 0) { //添加成功
                                    double latitude = jsonObject.getDouble("latitude");
                                    double longitude = jsonObject.getDouble("longitude");
                                    String dateTime1 = jsonObject.getString("dateTime");
                                    Message msg = new Message();
                                    msg.what = 4;
                                    Bundle bundle = new Bundle();
                                    bundle.putDouble("latitude", latitude);
                                    bundle.putDouble("longitude", longitude);
                                    bundle.putString("dateTime", dateTime1);
                                    bundle.putString("username", toMail);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                } else { //好友不存在或者好友已经添加
                                    handler.sendEmptyMessage(5);
                                }

                            }
                        } catch (IOException | JSONException e) {
                            handler.sendEmptyMessage(2);  //io错误，服务器没有数据返回，jsonObject == null
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
        btCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btCodeSend.setClickable(true);
                btCodeSend.setText(R.string.bt_sendcode);
                editAuthcode.setText("");
                editUsername.setText("");
                editUsername.requestFocus();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                new ProgressUnity(context).dissmissProgressDialog();
                switch (msg.what) {
                    case 0:    //获取验证码成功
                        btCodeSend.setText(R.string.bt_codesended);
                        btCodeSend.setClickable(false);
                        break;
                    case 1:  //发送验证码错误 响应码！= 200
                        Toast.makeText(context, "获取验证码失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:  //网络IO错误
                        Toast.makeText(context, "网络错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:  //添加好友失败 响应码 ！= 200
                        Toast.makeText(context, "添加好友失败！", Toast.LENGTH_SHORT).show();
                        new ProgressUnity(context).dissmissProgressDialog();
                        break;
                    case 4:  //好友添加成功
                        Toast.makeText(context, "添加好友成功！", Toast.LENGTH_SHORT).show();
                        new ProgressUnity(context).dissmissProgressDialog();
                        btCodeSend.setClickable(true);
                        btCodeSend.setText(R.string.bt_sendcode);
                        editAuthcode.setText("");
                        editUsername.setText("");
                        editUsername.requestFocus();
                        //把好友marker显示到地图
                        Bundle bundle = msg.getData();
                        double latitude = bundle.getDouble("latitude");
                        double longitude = bundle.getDouble("longitude");
                        String dateTime1 = bundle.getString("dateTime");
                        String username = bundle.getString("username");
                        new MarkerUnity(MainActivity.aMap,
                                new LatLng(latitude, longitude),
                                username, dateTime1,
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_map_friend)).showMarker();
                        break;
                    case 5:  //好友不存在（没有注册）或者已经添加
                        Toast.makeText(context, "此用户不存在 or 已经是好友！", Toast.LENGTH_SHORT).show();
                        new ProgressUnity(context).dissmissProgressDialog();
                        btCodeSend.setClickable(true);
                        btCodeSend.setText(R.string.bt_sendcode);
                        editAuthcode.setText("");
                        editUsername.setText("");
                        editUsername.requestFocus();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        return view;
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
