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

import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.nomad.path.PathActivity;
import com.nomad.travellmap.R;
import com.nomad.unity.SearchUnity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AroundActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int RESULT_AROUND_OK = 4;
    private Handler handler;
    private SearchUnity searchUnity;
    private ArrayList<MarkerOptions> markerOptions;
    private ListView listView;
    private Button btMoreScene;

    private double latitude;
    private double longitude;//定位的的经纬度 （路径规划起点）
    private String poiCode;
    private String cityCode;

    private PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around);
        listView = (ListView) findViewById(R.id.lv_show_scene);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        btMoreScene = (Button) findViewById(R.id.bt_more_scene);
        btMoreScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUnity.searchPoi(latitude, longitude, 10000); //周边搜索半径为10000m 查找第....页
            }
        });
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0 :
                        Map<String, Object> map = searchUnity.getMap();
                        ArrayList<HashMap<String, Object>> arrayList = (ArrayList<HashMap<String, Object>>) map.get("arrayList");
                        markerOptions = (ArrayList<MarkerOptions>) map.get("markerOptions");
                        //显示到listview
                        SimpleAdapter simpleAdapter = new SimpleAdapter(AroundActivity.this
                                , arrayList
                                , R.layout.item_around
                                , new String[]{"city", "address"} //"bitmap",
                                , new int[]{R.id.tv_around_city, R.id.tv_around_address}); //R.id.image_around,
                        listView.setAdapter(simpleAdapter);
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
        searchUnity.searchPoi(latitude, longitude, 10000); //周边搜索半径为10000m
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
                            //todo 周边评论窗口
                            Intent intent1 = new Intent(AroundActivity.this, CommentActivity.class);

                            startActivity(intent1);
                            break;
                        case R.id.action_share:
                            popupMenu.dismiss();
                            //todo 分享窗口 一键分享sdk
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
