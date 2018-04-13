package com.nomad.unity;

/**
 * Created by nomad on 18-4-12.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 得到网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
 *
 * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
 * C.支持的图片格式 ,png, jpg,bmp,gif等等
 */

public class BitmapUnity {

    public static Bitmap getBitmap(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == 200){
            InputStream inputStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }
}
