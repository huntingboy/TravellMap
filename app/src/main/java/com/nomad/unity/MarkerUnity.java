package com.nomad.unity;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.nomad.travellmap.MainActivity;

import java.util.List;

/**
 * Created by nomad on 18-4-5.
 * 地图添加一个/多个marker，设置动画
 */

public class MarkerUnity {
    private LatLng latLng;
    private String city;
    private String address;
    private BitmapDescriptor icon;
    private List<MarkerOptions> markerOptionsList;
    private AMap map;

    public MarkerUnity(AMap map, LatLng latLng, String city, String address) {
        this.map = map;
        this.city = city;
        this.latLng = latLng;
        this.address = address;
    }

    public MarkerUnity(AMap map, LatLng latLng, String city, String address, BitmapDescriptor icon) {
        this.map = map;
        this.city = city;
        this.latLng = latLng;
        this.address = address;
        this.icon = icon;
    }

    public MarkerUnity(AMap map, List<MarkerOptions> markerOptionsList) {
        this.map = map;
        this.markerOptionsList = markerOptionsList;
    }

    public void showMarker(){
        //要先移动camera,然后添加marker,否则不显示marker
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 18, 30, 0)));
        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(city)
                .snippet(address)
                .draggable(false)
                .zIndex(-1)
                .setFlat(false));
        if (icon != null) {
            marker.setIcon(icon);  //假设 用户给了图片的marker都是不需要清除的
        } else {
            marker.setObject("记录删除"); //为了不删除定位蓝点，给所有不是定位蓝点的Marker setobject()
            if (map == MainActivity.aMap) {
                MainActivity.oldMarker = marker;
            }
        }

        Animation animation = new RotateAnimation(marker.getRotateAngle(),marker.getRotateAngle()+360,0,0,0);
        long duration = 1000L;
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());

        marker.setAnimation(animation);
        marker.startAnimation();
        marker.showInfoWindow();

    }

    public void showMarkers() {
        for (MarkerOptions markerOptions :
                markerOptionsList) {
            this.latLng = markerOptions.getPosition();
            this.city = markerOptions.getTitle();
            this.address = markerOptions.getSnippet();
            this.icon = markerOptions.getIcon();

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(city)
                    .snippet(address)
                    .draggable(false)
                    .zIndex(-1)
                    .setFlat(false));
            if (icon != null) {
                marker.setIcon(icon);  //假设 用户给了图片的marker都是不需要清除的
            } else {
                marker.setObject("记录删除");
            }

            Animation animation = new RotateAnimation(marker.getRotateAngle(),
                    marker.getRotateAngle()+360,0,0,0);
            long duration = 1000L;
            animation.setDuration(duration);
            animation.setInterpolator(new LinearInterpolator());

            marker.setAnimation(animation);
            marker.startAnimation();
        }
    }



    public static void clearMarkers(AMap map){
        //获取地图上所有Marker
        List<Marker> mapScreenMarkers = map.getMapScreenMarkers();
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            if (marker.getObject() instanceof String) {
                marker.remove();//移除当前Marker
            }
        }
    }
}
