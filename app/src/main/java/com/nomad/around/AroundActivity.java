package com.nomad.around;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.nomad.adapter.AroundAdapter;
import com.nomad.path.PathActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.ProgressUnity;
import com.nomad.unity.SearchUnity;
import com.nomad.unity.ShareUnity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AroundActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int RESULT_AROUND_OK = 4;
    private Handler handler;
    private SearchUnity searchUnity;
    private ArrayList<MarkerOptions> markerOptions;
    private ListView listView;
    private Button btMoreScene;
    private Button btLessScene;

    private double latitude;
    private double longitude;//定位的的经纬度 （路径规划起点）
    private String poiCode;

    private String cityCode;
    private PopupMenu popupMenu; //长按文本菜单
    private int currentPage = 0; //记录查询第几页

//    private SimpleAdapter simpleAdapter;
    private AroundAdapter adapter;
    List<Map<String, Object>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around);
        listView = (ListView) findViewById(R.id.lv_show_scene);
        listView.setEmptyView(findViewById(R.id.empty_list_comment));
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        btMoreScene = (Button) findViewById(R.id.bt_more_scene);
        btLessScene = (Button) findViewById(R.id.bt_less_scene);
        btMoreScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProgressUnity(AroundActivity.this).showProgressDiaglog();
                searchUnity.searchPoi(latitude, longitude, 10000, currentPage, true); //周边搜索半径为10000m 查找第....页
            }
        });
        btLessScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProgressUnity(AroundActivity.this).showProgressDiaglog();
                //避免越界  当前页面>1 才有上一页
                if (currentPage > 1)
                    searchUnity.searchPoi(latitude, longitude, 10000, currentPage, false); //周边搜索半径为10000m 查找第....页
                else{
                    new ProgressUnity(AroundActivity.this).dissmissProgressDialog();
                    Toast.makeText(AroundActivity.this, "没有数据~", Toast.LENGTH_SHORT).show();
                }

            }
        });
        new ProgressUnity(this).showProgressDiaglog();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                new ProgressUnity(AroundActivity.this).dissmissProgressDialog();
                switch (msg.what) {
                    case 0 : //周边搜索完成  并且有结果
                        if (msg.getData().getBoolean("flag")) {
                            currentPage++;
                        } else {
                            currentPage--;
                        }
                        Map<String, Object> map = searchUnity.getMap();
                        list = new ArrayList<>();
                        list = (ArrayList<Map<String, Object>>) map.get("arrayList");
                        markerOptions = (ArrayList<MarkerOptions>) map.get("markerOptions");
                        //显示到listview
                        /*simpleAdapter = new SimpleAdapter(AroundActivity.this
                                , arrayList
                                , R.layout.item_around
                                , new String[]{"city", "address"} //"bitmap",
                                , new int[]{R.id.tv_around_city, R.id.tv_around_address}); //R.id.image_around,*/
                        adapter = new AroundAdapter(AroundActivity.this, list);
                        listView.setAdapter(adapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        poiCode = intent.getStringExtra("poiCode");
        cityCode = intent.getStringExtra("cityCode");
        searchUnity = new SearchUnity(poiCode, cityCode, this, handler);
        searchUnity.setLatitude(latitude);
        searchUnity.setLongitude(longitude);
        searchUnity.searchPoi(latitude, longitude, 10000, currentPage, true); //周边搜索半径为10000m
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //地图显示当前页所有的景点  city address
        @SuppressWarnings("unchecked")
        HashMap<String, Object> hashMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
        if (hashMap != null) {
            double latitude = (double) hashMap.get("latitude");
            double longitude = (double) hashMap.get("longitude");
            String address = (String) hashMap.get("address");
            String city = (String) hashMap.get("city");
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putString("address", address);
            bundle.putString("city", city);
            bundle.putSerializable("markerOptions", markerOptions);
            intent.putExtra("bundle", bundle);
            this.setResult(RESULT_AROUND_OK, intent);
            this.finish();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> hashMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
        if (hashMap != null) {
            final double latitude = (double) hashMap.get("latitude");
            final double longitude = (double) hashMap.get("longitude");
            final String city = (String) hashMap.get("city");
            final String address = (String) hashMap.get("address");
            popupMenu = new PopupMenu(this, view);
            getMenuInflater().inflate(R.menu.popup_menu_around, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_go:
                            popupMenu.dismiss();
                            //1.当前位置 2.目标位置（点击位置）3.城市编码通过Intent传过去
                            Intent intent = new Intent(AroundActivity.this, PathActivity.class);
                            intent.putExtra("latitude", AroundActivity.this.latitude);
                            intent.putExtra("longitude", AroundActivity.this.longitude);
                            intent.putExtra("latitudeto", latitude);
                            intent.putExtra("longitudeto", longitude);
                            intent.putExtra("cityCode", cityCode);
                            startActivity(intent);
                            break;
                        case R.id.action_comment:
                            popupMenu.dismiss();
                            //周边评论窗口
                            Intent intent1 = new Intent(AroundActivity.this, CommentActivity.class);
                            intent1.putExtra("latitude", latitude);
                            intent1.putExtra("longitude", longitude);
                            intent1.putExtra("city", city);
                            intent1.putExtra("address", address);
                            startActivity(intent1);
                            break;
                        case R.id.action_share:
                            popupMenu.dismiss();
                            //分享窗口 一键分享sdk
                            Map<String, Object> map = new HashMap<>();
                            map.put("longitude", longitude);
                            map.put("latitude", latitude);
                            map.put("city", city);
                            map.put("address", address);
//                            String text = "经度：" + longitude + "\n纬度：" + latitude + "\n城市：" + city + "\n地址：" + address;
                            ShareUnity.share(AroundActivity.this, map);
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        return true;
    }
}
