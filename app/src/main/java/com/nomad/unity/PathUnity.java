package com.nomad.unity;

import android.content.Context;
import android.os.Handler;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.TMC;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-14.
 * 高德地图路径规划  驾车 步行 公交 自行车 货车
 */
//todo 所有的路径搜索模式都可以在设置里面由用户自行设置   此处为默认
public class PathUnity implements RouteSearch.OnRouteSearchListener {

    private RouteSearch routeSearch;
    private RouteSearch.FromAndTo fromAndTo;

    private Handler handler;

    private Map<String, Object> map;

    public PathUnity(Context context, Handler handler) {
        this.handler = handler;
        routeSearch = new RouteSearch(context);
        routeSearch.setRouteSearchListener(this);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    /**
     *驾车出行路线规划
     */
    public void byCar(LatLonPoint from, LatLonPoint to){
        // fromAndTo包含路径规划的起点和终点，drivingMode表示驾车模式
        // 第三个参数表示途经点（最多支持16个），第四个参数表示避让区域（最多支持32个），第五个参数表示避让道路
        fromAndTo = new RouteSearch.FromAndTo(from, to);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_MULTI_CHOICE_AVOID_CONGESTION, null, null, "");
        routeSearch.calculateDriveRouteAsyn(query);
    }
    /**
     *步行出行路线规划
     */
    public void byWalk(LatLonPoint from, LatLonPoint to){
        // fromAndTo包含路径规划的起点和终点
        //初始化query对象，fromAndTo是包含起终点信息，walkMode是步行路径规划的模式
        fromAndTo = new RouteSearch.FromAndTo(from, to);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_MULTI_PATH );
        routeSearch.calculateWalkRouteAsyn(query);//开始算路
    }
    /**
     *公交出行路线规划
     */
    public void byBus(LatLonPoint from, LatLonPoint to, String cityCode){
        // fromAndTo包含路径规划的起点和终点，RouteSearch.BusLeaseWalk表示公交查询模式
        // 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
        fromAndTo = new RouteSearch.FromAndTo(from, to);
        RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk, cityCode,0);
        //query.setCityd("027");//终点城市区号
        routeSearch.calculateBusRouteAsyn(query);//开始规划路径
    }
    /**
     *骑行出行路线规划
     */
    public void byRide(LatLonPoint from, LatLonPoint to){
        // fromAndTo包含路径规划的起点和终点
        fromAndTo = new RouteSearch.FromAndTo(from, to);
        RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo);
        //query.setCityd("027");//终点城市区号
        routeSearch.calculateRideRouteAsyn(query);
    }
    /**
     * todo 最新版搜索sdk才有 货车出行路线规划
     */
    /*public void byTruck(LatLonPoint from, LatLonPoint to){
        // fromAndTo包含路径规划的起点和终点
        fromAndTo = new RouteSearch.FromAndTo(from, to);
        //设置车牌
        fromAndTo.setPlateProvince("京");
        fromAndTo.setPlateNumber("A000XXX");
        RouteSearch.TruckRouteQuery query = new RouteSearch.TruckRouteQuery(fromAndTo,truckMode, null, RouteSearch.TRUCK_SIZE_LIGHT);
        //设置车辆信息
        query.setTruckAxis(6);
        query.setTruckHeight(3.9f);
        query.setTruckWidth(3);
        query.setTruckLoad(45);
        query.setTruckWeight(50);
    }*/


    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i == 1000) {
            map = new HashMap<>();
            float cost = driveRouteResult.getTaxiCost();
            map.put("cost", cost);
            List<DrivePath> pathList = driveRouteResult.getPaths();
            if (pathList.size() == 0) {
                handler.sendEmptyMessage(4);
            } else {
                map.put("pathList", pathList);
                handler.sendEmptyMessage(0);
            }
            /*for (DrivePath drivePath :
                    pathList) {
                int trafficLights = drivePath.getTotalTrafficlights();
                List<DriveStep> stepList = drivePath.getSteps();
                for (DriveStep driveStep :
                        stepList) {
                    float distance = driveStep.getDistance();

                    float seconds = driveStep.getDuration();
                    String road = driveStep.getRoad();
                    List<LatLonPoint> pointList = driveStep.getPolyline();
                    //获取搜索返回的路径规划交通拥堵信息。
                    //List<TMC> tmcList = driveStep.getTMCs();
                }
            }*/
        } else {
            handler.sendEmptyMessage(4);
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        //解析result获取算路结果
        if (i == 1000) {
            map = new HashMap<>();
            List<BusPath> pathList = busRouteResult.getPaths();
            if (pathList.size() == 0) {
                handler.sendEmptyMessage(4);
            } else {
                map.put("pathList", pathList);
                handler.sendEmptyMessage(2);
            }
        } else {
            handler.sendEmptyMessage(4);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        //解析result获取算路结果
        if (i == 1000) {
            map = new HashMap<>();
            List<WalkPath> pathList = walkRouteResult.getPaths();
            if (pathList.size() == 0) {
                handler.sendEmptyMessage(4);
            } else {
                map.put("pathList", pathList);
                handler.sendEmptyMessage(1);
            }
        } else {
            handler.sendEmptyMessage(4);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        //解析result获取算路结果
        if (i == 1000) {
            map = new HashMap<>();
            List<RidePath> pathList = rideRouteResult.getPaths();
            if (pathList.size() == 0) {
                handler.sendEmptyMessage(4);
            } else {
                map.put("pathList", pathList);
                handler.sendEmptyMessage(3);
            }
        } else {
            handler.sendEmptyMessage(4);
        }
    }
}
