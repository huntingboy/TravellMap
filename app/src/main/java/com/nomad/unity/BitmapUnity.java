package com.nomad.unity;

/**
 * Created by nomad on 18-4-12.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
            conn.disconnect();
            if (inputStream != null) inputStream.close();
            return bitmap;
        }
        return null;
    }

    public static void download2File(File fileDir, String name, String url1){
        FileOutputStream fos = null;
        InputStream in = null;

        // 创建文件
        File file = new File(fileDir, name);

        try {

            fos = new FileOutputStream(file);

            URL url = new URL(url1);

            in = url.openStream();

            int len = -1;
            byte[] b = new byte[1024];
            while ((len = in.read(b)) != -1) {
                fos.write(b, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
            Log.e("Amap", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

}
