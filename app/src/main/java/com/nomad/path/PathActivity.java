package com.nomad.path;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.Railway;
import com.amap.api.services.route.RailwayStationItem;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteRailwayItem;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.nomad.travellmap.R;
import com.nomad.unity.MarkerUnity;
import com.nomad.unity.PathUnity;
import com.nomad.unity.ProgressUnity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PathActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btCar, btWalk, btBus, btCycle;
    private AMap map;
    private MapView mapView;

    private int id; //记录当前哪个button被激活

    private double latitude;
    private double longitude;//地图中心点移动到当前位置  intent传过来
    private LatLonPoint from;
    private LatLonPoint to;
    
    private Handler handler; //处理算路回调，获取路径信息
    private PathUnity pathUnity;
    private String cityCode;
    private TextView textView;
    private Button btNext;
    private Button btPrevious;
    private int i = 0;
    private int length;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        new ProgressUnity(this).showProgressDiaglog();
        btCar = (Button) findViewById(R.id.bt_path_car);
        btWalk = (Button) findViewById(R.id.bt_path_walk);
        btBus = (Button) findViewById(R.id.bt_path_bus);
        btCycle = (Button) findViewById(R.id.bt_path_cycle);
        textView = (TextView) findViewById(R.id.tv_path);
        btNext = (Button) findViewById(R.id.bt_next);
        btPrevious = (Button) findViewById(R.id.bt_previous);

        btCar.setOnClickListener(this);
        btWalk.setOnClickListener(this);
        btBus.setOnClickListener(this);
        btCycle.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPrevious.setOnClickListener(this);

        mapView = (MapView) findViewById(R.id.map_path);
        mapView.onCreate(savedInstanceState);
        if (map == null) {
            map = mapView.getMap();
        }
        map.setTrafficEnabled(true);
        map.showBuildings(true);
        map.showIndoorMap(true);
        map.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            View infoWindow = null;
            @Override
            public View getInfoWindow(Marker marker) {
                if (infoWindow == null) {
                    infoWindow = LayoutInflater.from(PathActivity.this).inflate(R.layout.custom_info_window1, null);
                }
                render(marker, infoWindow);
                return infoWindow;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        from = new LatLonPoint(latitude, longitude);
        to = new LatLonPoint(intent.getDoubleExtra("latitudeto", 0), intent.getDoubleExtra("longitudeto", 0));
        cityCode = intent.getStringExtra("cityCode");
        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(latitude, longitude), 18, 30, 0)));
        map.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                List<MarkerOptions> markerOptionsList;
                List<LatLng> pointList1;
                float totalDistance, totalSeconds;
                int hour, min;
                new ProgressUnity(PathActivity.this).dissmissProgressDialog();
                switch (msg.what) {
                    case 0:
                        Map<String, Object> hashMap0 = pathUnity.getMap();
                        float cost = (float) hashMap0.get("cost");
                        List<DrivePath> drivePathList = (List<DrivePath>) hashMap0.get("pathList");
                        length = drivePathList.size();
                        index = 0;
                        DrivePath drivePath = drivePathList.get(i); //根据用户点击pre , next ,在sendemptymessage（）
                        int trafficLights = drivePath.getTotalTrafficlights();
                        totalDistance = drivePath.getDistance();
                        totalSeconds = drivePath.getDuration();
                        Log.d("Amap", "pathactivity->handler====trafficlights:" + trafficLights + ", totalDistance:" + totalDistance + "m, totalSeconds" + totalSeconds + ", totalCost:" + cost);
                        List<DriveStep> stepList = drivePath.getSteps();
                        markerOptionsList = new ArrayList<>();
                        pointList1 = new ArrayList<>();
                        pointList1.add(new LatLng(from.getLatitude(), from.getLongitude())); //起点
                        for (DriveStep driveStep :
                                stepList) {
                            float distance = driveStep.getDistance();
                            float seconds = driveStep.getDuration();
                            String road = driveStep.getRoad();
                            Log.d("Amap", "pathactivity->handler====road:" + road + ", distance:" + distance + "m, time:" + seconds + "s");
                            List<LatLonPoint> pointList = driveStep.getPolyline();
                            int temp = 0;
                            for (LatLonPoint point :
                                    pointList) {
                                pointList1.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                if (temp == 0) {
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                            .title(driveStep.getRoad())
                                            .snippet(driveStep.getInstruction());
                                    markerOptionsList.add(markerOptions);
                                }
                                temp++;
                            }
                            /*LatLonPoint start = pointList.get(0);
                            MarkerOptions markerOptions1 = new MarkerOptions()
                                    .position(new LatLng(start.getLatitude(), start.getLongitude()))
                                    .title(driveStep.getRoad())
                                    .snippet(driveStep.getInstruction());
                            pointList1.add(new LatLng(start.getLatitude(), start.getLongitude()));
                            markerOptionsList.add(markerOptions1);
                            LatLonPoint end = pointList.get(pointList.size() - 1);
                            MarkerOptions markerOptions2 = new MarkerOptions()
                                    .position(new LatLng(end.getLatitude(), end.getLongitude()))
                                    .title(driveStep.getRoad())
                                    .snippet(driveStep.getInstruction());
                            pointList1.add(new LatLng(end.getLatitude(), end.getLongitude()));
                            markerOptionsList.add(markerOptions2);*/
                            //获取搜索返回的路径规划交通拥堵信息。
                            //List<TMC> tmcList = driveStep.getTMCs();
                        }
                        pointList1.add(new LatLng(to.getLatitude(), to.getLongitude())); //终点
                        hour = (int)totalSeconds / 60 / 60;
                        min = (int)totalSeconds / 60 % 60;
                        textView.setText("花费:" + cost + "元, 红绿灯:" + trafficLights + "个\n总距离:" + totalDistance + "米\n历时:" + hour + "小时" + min + "分钟");
                        map.clear(true);
                        map.addPolyline(new PolylineOptions().addAll(pointList1).width(5).color(Color.argb(150, 1, 1, 1)));
                        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location)).zIndex(-1));
                        map.addMarker(new MarkerOptions().position(new LatLng(to.getLatitude(), to.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dest)).zIndex(-1));
                        new MarkerUnity(map, markerOptionsList).showMarkers();
                        break;
                    case 1:
                        Map<String, Object> hashMap1 = pathUnity.getMap();
                        List<WalkPath> walkPathList = (List<WalkPath>) hashMap1.get("pathList");
                        length = walkPathList.size();
                        index = 1;
                        WalkPath walkPath = walkPathList.get(i);
                        totalDistance = walkPath.getDistance();
                        totalSeconds = walkPath.getDuration();
                        Log.d("Amap", "pathactivity->handler====totalDistance:" + totalDistance + ", totalSeconds" + totalSeconds);
                        List<WalkStep> walkStepList = walkPath.getSteps();
                        markerOptionsList = new ArrayList<>();
                        pointList1 = new ArrayList<>();
                        pointList1.add(new LatLng(from.getLatitude(), from.getLongitude())); //起点
                        for (WalkStep walkStep :
                                walkStepList) {
                            float distance = walkStep.getDistance();
                            float seconds = walkStep.getDuration();
                            String road = walkStep.getRoad();
                            Log.d("Amap", "pathactivity->handler====distance:" + distance + ", seconds:" + seconds + ", road:" + road);
                            List<LatLonPoint> pointList = walkStep.getPolyline();
                            int temp = 0;
                            for (LatLonPoint point :
                                    pointList) {
                                pointList1.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                if (temp == 0) {
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                            .title(walkStep.getRoad())
                                            .snippet(walkStep.getInstruction());
                                    markerOptionsList.add(markerOptions);
                                }
                                temp++;
                            }
                            /*LatLonPoint start = pointList.get(0);
                            MarkerOptions markerOptions1 = new MarkerOptions()
                                    .position(new LatLng(start.getLatitude(), start.getLongitude()))
                                    .title(walkStep.getRoad())
                                    .snippet(walkStep.getInstruction());
                            pointList1.add(new LatLng(start.getLatitude(), start.getLongitude()));
                            markerOptionsList.add(markerOptions1);
                            LatLonPoint end = pointList.get(pointList.size() - 1);
                            MarkerOptions markerOptions2 = new MarkerOptions()
                                    .position(new LatLng(end.getLatitude(), end.getLongitude()))
                                    .title(walkStep.getRoad())
                                    .snippet(walkStep.getInstruction());
                            pointList1.add(new LatLng(end.getLatitude(), end.getLongitude()));
                            markerOptionsList.add(markerOptions2);*/
                        }
                        pointList1.add(new LatLng(to.getLatitude(), to.getLongitude())); //终点
                        map.clear(true);
                        hour = (int)totalSeconds / 60 / 60;
                        min = (int)totalSeconds / 60 % 60;
                        textView.setText("总距离:" + totalDistance + "米\n历时:" + hour + "小时" + min + "分钟");
                        map.addPolyline(new PolylineOptions().addAll(pointList1).width(5).color(Color.argb(150, 1, 1, 1)));
                        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location)).zIndex(-1));
                        map.addMarker(new MarkerOptions().position(new LatLng(to.getLatitude(), to.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dest)).zIndex(-1));
                        new MarkerUnity(map, markerOptionsList).showMarkers();
                        break;
                    case 2:
                        map.clear(true);
                        Map<String, Object> hashMap2 = pathUnity.getMap();
                        List<BusPath> busPathList = (List<BusPath>) hashMap2.get("pathList");
                        length = busPathList.size();
                        index = 2;
                        BusPath busPath = busPathList.get(i);
                        totalSeconds = busPath.getDuration();
                        totalDistance = busPath.getDistance();
                        Log.d("Amap", "pathactivity->handler====totalDistance:" + totalDistance + ", totalSeconds" + totalSeconds);
                        List<BusStep> busStepList = busPath.getSteps();
                        markerOptionsList = new ArrayList<>();
                        List<LatLng> pointList2, pointList3; //存放步行段、铁路段的坐标点集合
                        for (BusStep busStep :
                                busStepList) {
                            //步行段
                            pointList2 = new ArrayList<>();
                            RouteBusWalkItem routeBusWalkItem = busStep.getWalk();
                            if (routeBusWalkItem != null && routeBusWalkItem.getSteps().size() > 0) {
                                pointList2.add(new LatLng(routeBusWalkItem.getOrigin().getLatitude(), routeBusWalkItem.getOrigin().getLongitude()));
                                List<WalkStep> walkSteps = routeBusWalkItem.getSteps();
                                for (WalkStep step :
                                        walkSteps) {
                                    List<LatLonPoint> lonPoints = step.getPolyline();
                                    int temp = 0;
                                    for (LatLonPoint point :
                                            lonPoints) {
                                        pointList2.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                        if (temp == 0) {
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                                    .title(step.getRoad())
                                                    .snippet(step.getInstruction());
                                            markerOptionsList.add(markerOptions);
                                        }
                                        temp++;
                                    }
                                }
                                MarkerOptions markerOptions = new MarkerOptions() //步行段最后一个点
                                        .position(new LatLng(routeBusWalkItem.getDestination().getLatitude(), routeBusWalkItem.getDestination().getLongitude()))
                                        .title("走完了～")
                                        .snippet("步行距离：" + routeBusWalkItem.getDistance() + "米, 步行时间：" + routeBusWalkItem.getDuration() + "秒");
                                markerOptionsList.add(markerOptions);
                                pointList2.add(new LatLng(routeBusWalkItem.getDestination().getLatitude(), routeBusWalkItem.getDestination().getLongitude()));
                                map.addPolyline(new PolylineOptions().addAll(pointList2).width(10).color(Color.argb(150, 255, 0, 0))).setDottedLine(true);
                            }

                            //公交段
                            List<RouteBusLineItem> busLineItems = busStep.getBusLines();
                            for (RouteBusLineItem busLineItem:
                                    busLineItems) {    //共同的一段起始点可能会有多种公交线路
                                pointList1 = new ArrayList<>();  //公交段的坐标点集合
                                float seconds = busLineItem.getDuration();
                                float distance = busLineItem.getDistance();
                                Log.d("Amap", "pathactivity->oncreate()->handler()====seconds:" + seconds + ", distance:" + distance);
                                List<LatLonPoint> pointList = busLineItem.getPolyline();
                                List<BusStationItem> stationItems = busLineItem.getPassStations();
                                String stationName;
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                //地铁时间为空，所以这里判断一下
                                Date time1, time2;
                                String start = "", end = "";
                                time1 = busLineItem.getFirstBusTime();
                                time2 = busLineItem.getLastBusTime();
                                if (time1 != null && time2 != null) {
                                    start = sdf.format(time1.getTime());
                                    end = sdf.format(time2.getTime());
                                }
                                int i = 0; //记录经过的bus站点数目
                                BusStationItem stationItem = busLineItem.getDepartureBusStation();
                                MarkerOptions markerOptions = new MarkerOptions()  //起始站
                                        .position(new LatLng(stationItem.getLatLonPoint().getLatitude(), stationItem.getLatLonPoint().getLongitude()))
                                        .title(busLineItem.getBusLineName() + "  此站：" + stationItem.getBusStationName() + "  还要坐" + (busLineItem.getPassStationNum() + 1 - i++) + "站")
                                        .snippet(start + "~" + end + "  " + busLineItem.getTotalPrice() + "元");
                                markerOptionsList.add(markerOptions);
                                for (BusStationItem item :  //经过的站点名  不包括起始\终止点
                                        stationItems) {
                                    stationName = item.getBusStationName();
                                    markerOptions = new MarkerOptions()
                                            .position(new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude()))
                                            .title(busLineItem.getBusLineName() + "  此站：" + stationName + "  还要坐" + (busLineItem.getPassStationNum() + 1 - i++) + "站")
                                            .snippet(start + "~" + end + "  " + busLineItem.getTotalPrice() + "元");
                                    markerOptionsList.add(markerOptions);
                                }
                                BusStationItem stationItem1 = busLineItem.getArrivalBusStation();
                                markerOptions = new MarkerOptions()  //终点站
                                        .position(new LatLng(stationItem1.getLatLonPoint().getLatitude(), stationItem1.getLatLonPoint().getLongitude()))
                                        .title(busLineItem.getBusLineName() + "  此站：" + stationItem1.getBusStationName() + "，要下车了～")
                                        .snippet(start + "~" + end + "  " + busLineItem.getTotalPrice() + "元");
                                markerOptionsList.add(markerOptions);

                                for (LatLonPoint point :
                                        pointList) {
                                    pointList1.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                }
                                map.addPolyline(new PolylineOptions().addAll(pointList1).width(5).color(Color.argb(150, 1, 1, 1)));
                            }

                            //铁路段
                            RouteRailwayItem routeRailwayItem = busStep.getRailway();
                            if (routeRailwayItem != null) {
                                pointList3 = new ArrayList<>();
                                List<Railway> railways = routeRailwayItem.getAlters();
                                for (Railway railway :
                                        railways) {
                                    Log.d("Amap", "铁路备案：" + railway.getID() + "," + railway.getName());
                                }
                                String trip = routeRailwayItem.getTrip();
//                                String type = routeRailwayItem.getType();
                                String startStation = routeRailwayItem.getDeparturestop().getName();
                                String startTime = routeRailwayItem.getDeparturestop().getTime();
                                String endStation = routeRailwayItem.getArrivalstop().getName();
                                String endTime = routeRailwayItem.getArrivalstop().getTime();
                                String title = trip + "  " + startStation + "-" + endStation + "(" + startTime + "~" + endTime + ")"; //线路车次号 + 开始站-结束站（发车时-到车时）
                                Log.d("Amap", "pathactivity->handler case 2->bybus->railway===火车到站信息" + endStation + ", " + endTime +
                                        "; 火车发站信息:" + routeRailwayItem.getDeparturestop().getName() + ", " + routeRailwayItem.getDeparturestop().getTime());
                                int time = Integer.parseInt(routeRailwayItem.getTime()); //该线路车段耗时 s
                                hour = time / 60 / 60;
                                min = time / 60 % 60;
                                String address = "此站：" + startStation + ",该上车了~,车到时间：" + startTime + ", 历时：" + hour + "小时" + min + "分钟";
                                //上车起点
                                LatLng latLng = new LatLng(routeRailwayItem.getDeparturestop().getLocation().getLatitude(), routeRailwayItem.getDeparturestop().getLocation().getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .zIndex(-1)
                                        .title(title)
                                        .snippet(address);
                                markerOptionsList.add(markerOptions);
                                pointList3.add(latLng);
                                List<RailwayStationItem> stationItems = routeRailwayItem.getViastops(); //经过的车站(不包括起点)
                                for (RailwayStationItem item :
                                        stationItems) {
                                    String stationName = item.getName();
                                    String arrivalTime = item.getTime(); //车到站时间
                                    latLng = new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude());
                                    pointList3.add(latLng);
                                    address = "此站：" + stationName + ", 车到时间：" + arrivalTime;
                                    markerOptions = new MarkerOptions()
                                            .position(latLng)
                                            .zIndex(-1)
                                            .title(title)
                                            .snippet(address);
                                    markerOptionsList.add(markerOptions);
                                }
                                //下车终点
                                address = "此站：" + endStation + ",该下车了~,车到时间：" + endTime + ", 历时：" + hour + "小时" + min + "分钟";
                                latLng = new LatLng(routeRailwayItem.getArrivalstop().getLocation().getLatitude(), routeRailwayItem.getArrivalstop().getLocation().getLongitude());
                                markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .zIndex(-1)
                                        .title(title)
                                        .snippet(address);
                                markerOptionsList.add(markerOptions);
                                pointList3.add(latLng);
                                map.addPolyline(new PolylineOptions().addAll(pointList3).width(5).color(Color.argb(150, 1, 1, 1)));
                            }
                        }

                        hour = (int)totalSeconds / 60 / 60;
                        min = (int)totalSeconds / 60 % 60;
                        textView.setText("总距离:" + totalDistance + "米\n历时:" + hour + "小时" + min + "分钟");
                        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location)).zIndex(-1));
                        map.addMarker(new MarkerOptions().position(new LatLng(to.getLatitude(), to.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dest)).zIndex(-1));
                        new MarkerUnity(map, markerOptionsList).showMarkers();
                        break;
                    case 3:
                        Map<String, Object> hashMap3 = pathUnity.getMap();
                        List<RidePath> ridePathList = (List<RidePath>) hashMap3.get("pathList");
                        length = ridePathList.size();
                        index = 3;
                        RidePath ridePath = ridePathList.get(i);
                        totalSeconds = ridePath.getDuration();
                        totalDistance = ridePath.getDistance();
                        Log.d("Amap", "pathactivity->handler====totalDistance:" + totalDistance + ", totalSeconds" + totalSeconds);
                        List<RideStep> rideSteps = ridePath.getSteps();
                        markerOptionsList = new ArrayList<>();
                        pointList1 = new ArrayList<>();
                        pointList1.add(new LatLng(from.getLatitude(), from.getLongitude())); //起点
                        for (RideStep step :
                                rideSteps) {
                            float distance = step.getDistance();
                            float seconds = step.getDuration();
                            String road = step.getRoad();
                            Log.d("Amap", "pathactivity->handler====distance:" + distance + ", seconds" + seconds + ", road:" + road);
                            List<LatLonPoint> pointList = step.getPolyline();
                            int temp = 0;
                            for (LatLonPoint point :
                                    pointList) {
                                pointList1.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                if (temp == 0) {
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                            .title(step.getRoad())
                                            .snippet(step.getInstruction());
                                    markerOptionsList.add(markerOptions);
                                }
                                temp++;
                            }
                        }
                        pointList1.add(new LatLng(to.getLatitude(), to.getLongitude())); //终点
                        hour = (int)totalSeconds / 60 / 60;
                        min = (int)totalSeconds / 60 % 60;
                        textView.setText("总距离:" + totalDistance + "米\n历时:" + hour + "小时" + min + "分钟");
                        map.clear(true);
                        map.addPolyline(new PolylineOptions().addAll(pointList1).width(5).color(Color.argb(150, 1, 1, 1)));
                        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location)).zIndex(-1));
                        map.addMarker(new MarkerOptions().position(new LatLng(to.getLatitude(), to.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dest)).zIndex(-1));
                        new MarkerUnity(map, markerOptionsList).showMarkers();
                        break;
                    case 4:
                        Toast.makeText(PathActivity.this, "没有找到线路", Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };
        //开始 驾车路径规划
        btCar.setEnabled(false);
        id = R.id.bt_path_car;
        pathUnity = new PathUnity(this, handler);
        pathUnity.byCar(from, to);
    }
    protected void render(final Marker marker, View view) {
        TextView tvCity = view.findViewById(R.id.tv_marker_road);
        tvCity.setText(marker.getTitle());
        tvCity.setTextColor(getResources().getColor(R.color.colorMarkerCity));
        TextView tvAddress = view.findViewById(R.id.tv_marker_guide);
        tvAddress.setText(marker.getSnippet());
        tvAddress.setTextColor(getResources().getColor(R.color.colorMarkerAddress));
    }

    //todo 把handler里面相同的代码抽出来  反射
    protected void drawPath(List pathList) {

    }

    @Override
    public void onClick(View v) {
        new ProgressUnity(this).showProgressDiaglog();
        switch (v.getId()) {
            case R.id.bt_path_car:
                if (id != R.id.bt_path_car) {
                    i = 0;
                    findViewById(id).setEnabled(true);
                    btCar.setEnabled(false);
                    id = R.id.bt_path_car;

                    pathUnity = new PathUnity(this, handler);
                    pathUnity.byCar(from, to);
                }
                break;
            case R.id.bt_path_walk:
                if (id != R.id.bt_path_walk) {
                    i = 0;
                    findViewById(id).setEnabled(true);
                    btWalk.setEnabled(false);
                    id = R.id.bt_path_walk;

                    pathUnity = new PathUnity(this, handler);
                    pathUnity.byWalk(from, to);
                }
                break;
            case R.id.bt_path_bus:
                if (id != R.id.bt_path_bus) {
                    i = 0;
                    findViewById(id).setEnabled(true);
                    btBus.setEnabled(false);
                    id = R.id.bt_path_bus;

                    pathUnity = new PathUnity(this, handler);
                    pathUnity.byBus(from, to, cityCode);
                }
                break;
            case R.id.bt_path_cycle:
                if (id != R.id.bt_path_cycle) {
                    i = 0;
                    findViewById(id).setEnabled(true);
                    btCycle.setEnabled(false);
                    id = R.id.bt_path_cycle;

                    pathUnity = new PathUnity(this, handler);
                    pathUnity.byRide(from, to);
                }
                break;
            case R.id.bt_next:
                //todo 路径规划文本表示
//                startActivity(new Intent(PathActivity.this, PathDetailActivity.class));
                if (i + 1 < length) {
                    i++;
                    handler.sendEmptyMessage(index);
                } else {
                    new ProgressUnity(this).dissmissProgressDialog();
                    Toast.makeText(this, "已经是最后一种方案～", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_previous:
                if (i - 1 >= 0) {
                    i--;
                    handler.sendEmptyMessage(index);
                } else {
                    new ProgressUnity(this).dissmissProgressDialog();
                    Toast.makeText(this, "已经是第一种方案～", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

}
