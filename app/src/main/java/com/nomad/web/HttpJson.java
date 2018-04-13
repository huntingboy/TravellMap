package com.nomad.web;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nomad on 18-4-4.
 *
 * 和服务通过JSON交互数据，
 */

public class HttpJson {

    private String jsonString;
    private String url;
    private String result;
    public static String JSESSIONID; //与服务器的会话ID，持久化会话需要保存下来，在http头部的cookie中

    public int getResultCode() {
        return resultCode;
    }

    private int resultCode;

    public HttpJson(String url) {
        this.url = url;
    }
    public HttpJson(String jsonString, String url) {  //post方式使用的构造函数
        this.jsonString = jsonString;
        this.url = url;
    }

    //get方式提交到服务器
    public String doHttpGetJson() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        if (JSESSIONID != null) {
            get.setHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        }
        HttpResponse response = httpClient.execute(get);
        resultCode = response.getStatusLine().getStatusCode();
        if (resultCode == 200) {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, HTTP.UTF_8);
            //从cookie中拿到sessionid并且存下来
            //特别注意：b/s和c/s 会话方式不一样，android客户端需要手动给cookie添加sessoinId,以保存登录会话。
            CookieStore cookieStore = ((DefaultHttpClient)httpClient).getCookieStore();
            List<Cookie> cookieList = cookieStore.getCookies();
            for (Cookie cookie :
                    cookieList) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    JSESSIONID = cookie.getValue();
                    break;
                }
            }
        }
        return result;
    }

    //post方式提交到服务器
    public String doHttpPostJson() throws IOException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("data", jsonString));

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        if (JSESSIONID != null) {
            post.setHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        }
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpClient.execute(post);
        resultCode = response.getStatusLine().getStatusCode();
        if (resultCode == 200) {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, HTTP.UTF_8);
            //从cookie中拿到sessionid并且存下来
            CookieStore cookieStore = ((DefaultHttpClient)httpClient).getCookieStore();
            List<Cookie> cookieList = cookieStore.getCookies();
            for (Cookie cookie :
                    cookieList) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    JSESSIONID = cookie.getValue();
                    break;
                }
            }
        }
        return result;
    }
}
