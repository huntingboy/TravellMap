package com.nomad.around;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.maps.model.MarkerOptions;
import com.nomad.travellmap.R;
import com.nomad.unity.SearchUnity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AroundActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int RESULT_AROUND_OK = 4;
    private Handler handler;
    private SearchUnity searchUnity;
    private ArrayList<MarkerOptions> markerOptions;
    private ListView listView;
    private Button btMoreScene;
    private ImageView imageComment;
    private ImageView imageShare;

    private double latitude;
    private double longitude;//定位的的经纬度
    private String poiCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        listView = (ListView) findViewById(R.id.lv_show_scene);
        listView.setOnItemClickListener(this);
        btMoreScene = (Button) findViewById(R.id.bt_more_scene);
        btMoreScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUnity.searchPoi(latitude, longitude, 10000); //周边搜索半径为10000m 查找第....页
            }
        });
        View view = LayoutInflater.from(this).inflate(R.layout.item_around, null);
        imageComment = view.findViewById(R.id.image_comment);
        imageShare = view.findViewById(R.id.image_share);
        imageComment.setOnClickListener(this);
        imageShare.setOnClickListener(this);
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
        searchUnity = new SearchUnity(poiCode, "", this, handler);
        searchUnity.setLatitude(latitude);
        searchUnity.setLongitude(longitude);
        searchUnity.searchPoi(latitude, longitude, 10000); //周边搜索半径为10000m
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_comment:
                Intent intent = new Intent(this, CommentActivity.class);

                startActivity(intent);
                break;
            case R.id.image_share:
                //todo 一键分享sdk

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //地图显示当前页所有的景点  city address
        @SuppressWarnings("unchecked")
        HashMap<String, Object> hashMap = (HashMap<String, Object>) listView.getItemAtPosition(position);
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
}
