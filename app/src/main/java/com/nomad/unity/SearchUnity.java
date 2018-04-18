package com.nomad.unity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.Photo;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nomad on 18-4-12.
 * poi搜索工具类 ,关键字搜索，周边（位置，半径）搜索
 */

public class SearchUnity implements PoiSearch.OnPoiSearchListener{

    private Map<String, Object> map = new HashMap<>();

    private PoiSearch poiSearch = null;
    private int currentPage = 0;
    private Context context;
    private Handler handler;

    private double latitude;
    private double longitude;
    Bitmap bitmap ;

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private PoiSearch.Query  query = null;
    public Map<String, Object> getMap() {
        return map;
    }


    //keyWord表示搜索字符串
    //POI类别code
    //city code，""代表全国
    public SearchUnity(Context context, String keyword, String cityCode, Handler handler) {
        query = new PoiSearch.Query(keyword, cityCode);
        query.setPageSize(15);// todo 设置每页最多返回多少条poiitem  设置15  单击item直接退出了app
        this.context = context;
        this.handler = handler;
    }

    //周边搜索
    public SearchUnity(String poiCode, String cityCode, Context context, Handler handler) {
        query = new PoiSearch.Query("", poiCode, cityCode);
        query.setPageSize(15);// 设置每页最多返回多少条poiitem
        this.context = context;
        this.handler = handler;
    }

    public void searchAll(){
        //new ProgressUnity(context).showProgressDiaglog();
        query.setPageNum(currentPage++);//设置查询页码
        poiSearch = new PoiSearch(context, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    //搜索景点  餐馆
    public void searchPoi(double latitude, double longitude, int radius){
        query.setPageNum(currentPage++);//设置查询页码
        poiSearch = new PoiSearch(context, query);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,
                longitude), radius));//设置周边搜索的中心点以及半径
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
        //todo 第二次点击周边崩溃  is activity running?  progressdialog.show()
        /*if (!((Activity)context).isFinishing()) {
            new ProgressUnity(context).showProgressDiaglog();
        }*/
    }

    //异步回调的结果
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        new ProgressUnity(context).dissmissProgressDialog();
        if (i == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {// 是否是同一条
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();; //POI的list集合
                    ArrayList<MarkerOptions> markerOptions = new ArrayList<>(); //POI对应的标记点选项的list集合

                    if (poiItems != null && poiItems.size() > 0) {
                        //将PoiItems的标题和内容以列表的形式填到适配器，然后给listview显示。
                        for (int j = 0; j < poiItems.size(); j++) {
                            double latitude = poiItems.get(j).getLatLonPoint().getLatitude();
                            double longitude = poiItems.get(j).getLatLonPoint().getLongitude();
                            String city = poiItems.get(j).getCityName();
                            String address = poiItems.get(j).getSnippet();
                            String name = poiItems.get(j).getTitle();
                            address = address + "  " + name;
                            int distance = poiItems.get(j).getDistance();  //不是周边搜索就返回-1
                            //List<Photo> list = poiItems.get(j).getPhotos();
                            //Photo photo = list.get(0);
                            Log.d("Amap", "SearchUnity->onpoisearched()===name:" + name + ", distance:" + distance /*+ ", url:" + photo.getUrl()*/);
                            /**一直在转圈
                             * todo 新开线程+thread.join(5000)阻塞主线程;
                             * try {
                                bitmap = BitmapUnity.getBitmap(photo.getUrl());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                            if (distance != -1) {
                                city = city + "   距离中心点：" + distance + "米";
                            }
                            Log.d("Amap", "SearchActivity->onPoiSearched()=====>" + latitude + " ," + longitude + " ," + city + " ," + address);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            MarkerOptions markerOption = new MarkerOptions();
                            hashMap.put("latitude", latitude);
                            hashMap.put("longitude", longitude);
                            hashMap.put("city", city);
                            hashMap.put("address", address);
                            //hashMap.put("distance", distance);
                            //hashMap.put("bitmap", bitmap);
                            arrayList.add(hashMap);
                            markerOption.position(new LatLng(latitude, longitude))
                                    .title(city)
                                    .snippet(address)
                                    .draggable(false)
                                    .setFlat(false)
                                    .visible(true)
                                    .icon(BitmapDescriptorFactory.defaultMarker());
                            //      .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                            markerOptions.add(markerOption);
                        }
                        map.put("arrayList", arrayList);
                        map.put("markerOptions", markerOptions);
                        //handler处理异步搜索的结果
                        handler.sendEmptyMessage(0);
                    } else if (suggestionCities != null && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(context, "未找到结果", Toast.LENGTH_SHORT).show();
                    }
                }
            }else
                Toast.makeText(context, "未找到结果", Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(context, "错误代码：" + i, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
    /**
     * 自定义函数，当搜索没有结果时候被调用
     */
    private void showSuggestCity(List<SuggestionCity> suggestionCities) {
        String information = "推荐城市\n";
        for (int i = 0; i < suggestionCities.size(); i++) {
            information += "城市名称:" + suggestionCities.get(i).getCityName() + "\n";
        }
        Toast.makeText(context, information, Toast.LENGTH_SHORT).show();
    }
}
