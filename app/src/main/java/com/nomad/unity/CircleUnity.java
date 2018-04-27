package com.nomad.unity;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;

/**
 * Created by nomad on 18-4-19.
 */

public class CircleUnity {
    public static Circle drawCircle(AMap map, double latitude, double longitude, int radius){
        Circle circle = map.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(radius)
                .fillColor(Color.argb(100, 1, 1, 1))
                .strokeColor(Color.argb(100, 1, 1, 1))
                .strokeWidth(15));
        return circle;
    }
}
