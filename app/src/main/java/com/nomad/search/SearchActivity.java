package com.nomad.search;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.nomad.travellmap.R;
import com.nomad.unity.SearchUnity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键字搜索POI
 */
public class SearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, Inputtips.InputtipsListener {

    private EditText editSearch = null;
    private Button btSearch = null;
    private Button btNext = null;
    private ListView lvPoi = null;

    private Handler handler;
    private String keyword = null;
    private String cityCode = null;
    private SearchUnity searchUnity;
    private ArrayList<MarkerOptions> markerOptions = null; //POI对应的标记点选项的list集合

    private final int RESULT_POI_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editSearch = (EditText) findViewById(R.id.edit_search);
        btSearch = (Button) findViewById(R.id.bt_search);
        btNext = (Button) findViewById(R.id.bt_next);
        lvPoi = (ListView) findViewById(R.id.lv_poi);

        cityCode = getResources().getString(R.string.city_code); //"420000" 湖北 "" 全国  此处为后者
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
                InputtipsQuery inputquery = new InputtipsQuery(s.toString().trim(), cityCode);
                inputquery.setCityLimit(false);//不限制在当前城市
                Inputtips inputTips = new Inputtips(SearchActivity.this, inputquery);
                inputTips.setInputtipsListener(SearchActivity.this);
                inputTips.requestInputtipsAsyn();
            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = String.valueOf(s).trim();
            }
        });
        btSearch.setOnClickListener(this);
        btNext.setOnClickListener(this);
        lvPoi.setOnItemClickListener(this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //1.得到map中的arraylist和markeroptions
                //2.显示到listview
                switch (msg.what) {
                    case 0:
                        Map<String, Object> map = searchUnity.getMap();
                        ArrayList<HashMap<String, Object>> arrayList = (ArrayList<HashMap<String, Object>>) map.get("arrayList");
                        markerOptions = (ArrayList<MarkerOptions>) map.get("markerOptions");
                        //显示到listview
                        SimpleAdapter simpleAdapter = new SimpleAdapter(SearchActivity.this
                                , arrayList
                                , R.layout.item_search
                                , new String[]{"latitude", "longitude", "city", "address"}
                                , new int[]{R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4});
                        lvPoi.setAdapter(simpleAdapter);
                        break;
                }

                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_search:
                //keyWord表示搜索字符串
                //POI类别code
                //city code，""代表全国
                if (keyword != null) {
                    searchUnity = new SearchUnity(this, keyword, cityCode, handler);
                    searchUnity.searchAll();
                }
                break;
            case R.id.bt_next:
                if (searchUnity != null && searchUnity.getCurrentPage() > 0) {
                    searchUnity.searchAll();
                } else {
                    Toast.makeText(this, "你还没有搜索！", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //点击listview item 跳转到地图，并且在地图上面显示当前页的点标记
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> hashMap = (HashMap<String, Object>) lvPoi.getItemAtPosition(position);
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

            Log.d("Amap", "mainactivity->onactivityresult()===>" + latitude + "," + longitude + "," + address + "," + city);
            bundle.putSerializable("markerOptions", markerOptions);
            intent.putExtra("bundle", bundle);
            this.setResult(RESULT_POI_OK, intent);
            this.finish();
        }
    }



    //实现输入提示
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (i == 1000) {
            ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
            markerOptions = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                double latitude = list.get(j).getPoint().getLatitude();
                double longitude = list.get(j).getPoint().getLongitude();
                String city = list.get(j).getName();
                String address = list.get(j).getAddress();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("latitude", latitude);
                hashMap.put("longitude", longitude);
                hashMap.put("city", city);
                hashMap.put("address", address);
                arrayList.add(hashMap);
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(new LatLng(latitude, longitude))
                        .title(city)
                        .snippet(address)
                        .draggable(false)
                        .setFlat(false)
                        .visible(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.add(markerOption);

            }
            //显示输入提示到listview
            SimpleAdapter simpleAdapter = new SimpleAdapter(this
                    , arrayList
                    , R.layout.item_search
                    , new String[]{"latitude", "longitude", "city", "address"}
                    , new int[]{R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4});
            lvPoi.setAdapter(simpleAdapter);
        }
    }
}
