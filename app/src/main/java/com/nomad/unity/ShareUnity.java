package com.nomad.unity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nomad.travellmap.R;
import com.nomad.web.HttpJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by nomad on 18-4-20.
 * 一键分享
 */

public class ShareUnity {
    public static void share(Context context, Map<String, Object> map){
        OnekeyShare oks = new OnekeyShare();
        String text = "经度：" + map.get("longitude") + "\n纬度：" + map.get("latitude") + "\n城市：" + map.get("city") + "\n地址：" + map.get("address");
        oks.setText(text);
        oks.setLatitude(((Double) map.get("latitude")).floatValue());
        oks.setLongitude(((Double) map.get("longitude")).floatValue());
        oks.setVenueName((String) map.get("address"));
        oks.setVenueDescription((String) map.get("city"));

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->oncomplete()-->name:" + name + "分享成功~");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->onError()===:name" + name + "分享失败~" + throwable.getStackTrace().toString());
                Log.d("Amap", "shareunity->onError()===:name" + name + "分享失败~" + throwable.getMessage());
                throwable.getMessage();
                throwable.printStackTrace();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->onCancel()===:name" + name + "分享取消~");
            }
        });

        //显示gui
        oks.show(context);
    }


    public static void share(Context context, Map<String, Object> map, final int position, int id, final int share, final Handler handler) {
        OnekeyShare oks = new OnekeyShare();
        String text = "用户名：" + map.get("username") + "\n签到日期：" + map.get("time1") + "\n说说：" + map.get("description") + "\n地址：" + map.get("address");
        oks.setText(text);
        oks.setVenueName((String) map.get("address"));
        String protocol = context.getResources().getString(R.string.url_protocol);
        String host = context.getResources().getString(R.string.url_host);
        String port = context.getResources().getString(R.string.url_port);
        String path = context.getResources().getString(R.string.url_path_checkin);
        final String url = protocol + "://" + host + ":" + port + "/" + path
                + "?" + "id=" + id + "&share=" + share + "&method=updateShare";

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->oncomplete()-->name:" + name + "分享成功~");
                //服务器端更新share数量
                //后台修改数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Amap", "shareunity->oncomplete()-->run()===updateshare===url:" + url);
                        HttpJson httpJson = new HttpJson(url);
                        try {
                            String stringJson = httpJson.doHttpGetJson();
                            if (httpJson.getResultCode() != 200) {
                                handler.sendEmptyMessage(1);
                                Log.d("Amap", "shareunity->oncomplete()===resultcode:" + httpJson.getResultCode());
                            } else {
                                JSONObject jsonObject = new JSONObject(stringJson);
                                if (jsonObject.getInt("message") == 0) { //0:分享更新成功
                                    Message msg = new Message();
                                    msg.what = 6;
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("share", share);
                                    bundle.putInt("position", position);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
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

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->onError()===:name" + name + "分享失败~" + throwable.getStackTrace().toString());
                Log.d("Amap", "shareunity->onError()===:name" + name + "分享失败~" + throwable.getMessage());
                throwable.getMessage();
                throwable.printStackTrace();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                String name = platform.getName();
                Log.d("Amap", "shareunity->onCancel()===:name" + name + "分享取消~");
            }
        });

        //显示gui
        oks.show(context);
    }
}
