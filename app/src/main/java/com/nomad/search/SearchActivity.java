package com.nomad.search;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.nomad.travellmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener, AdapterView.OnItemClickListener, Inputtips.InputtipsListener {

    private EditText editSearch = null;
    private Button btSearch = null;
    private Button btNext = null;
    private ListView lvPoi = null;
    private ProgressDialog progressDialog;

    private PoiSearch.Query  query = null;
    private PoiSearch poiSearch = null;
    private String keyword = null;
    private String cityCode = null;
    private int currentPage = 0;
    private ArrayList<HashMap<String, String>> arrayList = null; //POI的list集合
    private ArrayList<MarkerOptions> markerOptions = null; //POI对应的标记点选项的list集合

    private final static int POI_RESULT_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editSearch = (EditText) findViewById(R.id.edit_search);
        btSearch = (Button) findViewById(R.id.bt_search);
        btNext = (Button) findViewById(R.id.bt_next);
        lvPoi = (ListView) findViewById(R.id.lv_poi);

        cityCode = getResources().getString(R.string.city_code); //"420000" 湖北
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //todo 自动提示
                //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
                InputtipsQuery inputquery = new InputtipsQuery(s.toString().trim(), cityCode);
                inputquery.setCityLimit(true);//限制在当前城市
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_search:
                break;
            case R.id.bt_search:
                //keyWord表示搜索字符串
                //POI类别code
                //city code，""代表全国
                showProgressDiaglog();
                currentPage = 0;
                query = new PoiSearch.Query(keyword, cityCode);
                query.setPageSize(10);// 设置每页最多返回多少条poiitem
                query.setPageNum(currentPage++);//设置查询页码
                poiSearch = new PoiSearch(this, query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.searchPOIAsyn();
                break;
            case R.id.bt_next:
                if (currentPage > 0) {
                    showProgressDiaglog();
                    query.setPageNum(currentPage++);
                    poiSearch = new PoiSearch(this, query);
                    poiSearch.setOnPoiSearchListener(this);
                    poiSearch.searchPOIAsyn();
                } else {
                    Toast.makeText(this, "你还没有搜索！", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //异步回调的结果
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        dissmissProgressDialog();// 隐藏对话框
        if (i == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {// 是否是同一条
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    arrayList = new ArrayList<>();
                    markerOptions = new ArrayList<>();

                    if (poiItems != null && poiItems.size() > 0) {
                        //将PoiItems的标题和内容以列表的形式填到适配器，然后给listview显示。
                        for (int j = 0; j < poiItems.size(); j++) {
                            double latitude = poiItems.get(j).getLatLonPoint().getLatitude();
                            double longitude = poiItems.get(j).getLatLonPoint().getLongitude();
                            HashMap<String, String> hashMap = new HashMap<>();
                            MarkerOptions markerOption = new MarkerOptions();
                            hashMap.put("latitude", String.valueOf(latitude));
                            hashMap.put("longitude", String.valueOf(longitude));
                            hashMap.put("city", poiItems.get(j).getCityName());
                            hashMap.put("address", poiItems.get(j).getAdName());
                            arrayList.add(hashMap);
                            markerOption.position(new LatLng(latitude, longitude));
                            markerOptions.add(markerOption);
                        }

                        //显示到listview
                        SimpleAdapter simpleAdapter = new SimpleAdapter(this
                                , arrayList
                                , R.layout.item
                                , new String[]{"latitude", "longitude", "city", "address"}
                                , new int[]{R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4});
                        lvPoi.setAdapter(simpleAdapter);
                    } else if (suggestionCities != null && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT).show();
                    }
                }
            }else
                Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this, "错误代码：" + i, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    //点击listview item 跳转到地图，并且在地图上面显示当前页的点标记
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> hashMap = (HashMap<String, String>) lvPoi.getItemAtPosition(position);
        if (hashMap != null) {
            String latitude = hashMap.get("latitude");
            String longitude = hashMap.get("longitude");
            String address = hashMap.get("address");
            String city = hashMap.get("city");

            Intent intent = new Intent();
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("address", address);
            intent.putExtra("city", city);
            intent.putExtra("markerOptions", markerOptions);
            this.setResult(POI_RESULT_OK, intent);
            this.finish();
        }
    }

    /**自定义函数
     * 显示搜索对话框
     */
    private void showProgressDiaglog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在搜索:\n" + keyword);
        progressDialog.show();
    }

    /**自定义函数
     * 隐藏搜索对话框
     * 在onPoiSearched回调方法中被调用
     */
    private void dissmissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 自定义函数，当搜索没有结果时候被调用
     */
    private void showSuggestCity(List<SuggestionCity> suggestionCities) {
        String information = "推荐城市\n";
        for (int i = 0; i < suggestionCities.size(); i++) {
            information += "城市名称:" + suggestionCities.get(i).getCityName() + "\n";
        }
        Toast.makeText(this, information, Toast.LENGTH_SHORT).show();
    }

    //实现输入提示
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (i == 1000) {
            arrayList = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                double latitude = list.get(j).getPoint().getLatitude();
                double longitude = list.get(j).getPoint().getLongitude();
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("latitude", String.valueOf(latitude));
                hashMap.put("longitude", String.valueOf(longitude));
                hashMap.put("city", list.get(j).getName());
                hashMap.put("address", list.get(j).getAddress());
                arrayList.add(hashMap);

            }
            //显示输入提示到listview
            SimpleAdapter simpleAdapter = new SimpleAdapter(this
                    , arrayList
                    , R.layout.item
                    , new String[]{"latitude", "longitude", "city", "address"}
                    , new int[]{R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4});
            lvPoi.setAdapter(simpleAdapter);
        }
    }
}
