package com.nomad.geocode;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.nomad.travellmap.MainActivity;
import com.nomad.unity.MarkerUnity;

/**
 * Created by nomad on 18-4-4.
 * 地理编码
 * 逆地理编码
 */

public class GeoCode implements GeocodeSearch.OnGeocodeSearchListener{

    private GeocodeSearch geocodeSearch;
    private LatLonPoint latLonPoint;
    private String address;
    private String city;
    private AMap map; //为了在map上显示marker, showinfowindow()
    private Handler handler;

    public String getAddress() {
        return address;
    }
    public String getCity() {
        return city;
    }

    /**
     * 地理编码构造函数
     * @param name
     *
     */
    public GeoCode(Context context, String name, AMap map) throws AMapException {
        this.map = map;
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);
        GeocodeQuery geocodeQuery = new GeocodeQuery(name, null);
        geocodeSearch.getFromLocationNameAsyn(geocodeQuery);
    }

    /**
     * 逆地理编码
     * @param latLng
     */
    public GeoCode(Context context, LatLng latLng, AMap map) {
        this.map = map;
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);
        latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);
    }
    public GeoCode(Context context, LatLng latLng, Handler handler) {
        this.handler = handler;
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);
        latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);
    }

    /**
     * 逆地理编码回调，坐标转地址
     * @param regeocodeResult
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                city = regeocodeResult.getRegeocodeAddress().getCity();
                address = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                //PoiItem poiItem = regeocodeResult.getRegeocodeAddress().getPois().get(0);
                //String name = poiItem.getAdName();  //name = null
                //address = address + "  " + name
                Log.d("Amap", "GeoCode->onRegeocodeSearched()==>" + city + "," + address);
                if (map != null) {
                    new MarkerUnity(map, new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude())
                            , city, address).showMarker();
                } else if (handler != null) {
                    handler.sendEmptyMessage(3);
                }
            }
        }
    }

    /**
     * 地理编码回调，地址转坐标
     * @param geocodeResult
     * @param i
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
