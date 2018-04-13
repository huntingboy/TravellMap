/**导包重复，地图一片黑
package com.nomad.unity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by nomad on 18-4-12.
 * 测量地图起始点距离
 *//**


public class DistanceUnity implements DistanceSearch.OnDistanceSearchListener {
    private List distance;
    private Context context;
    private DistanceSearch distanceSearch;
    private DistanceSearch.DistanceQuery distanceQuery;
    public List getDistance() {
        return distance;
    }

    public DistanceUnity(Context context){
        this.context = context;
        distanceSearch = new DistanceSearch(context);
        distanceSearch.setDistanceSearchListener(this);
        distanceQuery = new DistanceSearch.DistanceQuery();

        //设置测量方式，支持直线和驾车
        distanceQuery.setType(DistanceSearch.TYPE_DRIVING_DISTANCE);
    }

    public void addStarts(List<PoiItem> poiItems){
        List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();
        for (int j = 0; j < poiItems.size(); j++) {
            double latitude = poiItems.get(j).getLatLonPoint().getLatitude();
            double longitude = poiItems.get(j).getLatLonPoint().getLongitude();
            LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
            latLonPoints.add(latLonPoint);
        }
        distanceQuery.setOrigins(latLonPoints);
    }
    public void addDest(double latitude, double longitude){
        LatLonPoint dest = new LatLonPoint(latitude, longitude);
        distanceQuery.setDestination(dest);
    }
    public void startCal(){
        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int i) {
        if (i == 1000) {
            distance = new ArrayList();
            List<DistanceItem> list = distanceResult.getDistanceResults();
            for (DistanceItem item:
                 list) {
                Log.d("Amap", "distanceunity==distance:" + item.getDistance());
                distance.add(item.getDistance());
            }
        } else {
            Toast.makeText(context, "计算距离失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
*/
