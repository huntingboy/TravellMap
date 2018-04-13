package com.nomad.geofence;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nomad.fragment.AddFenceFragment;
import com.nomad.fragment.ViewFenceFragment;
import com.nomad.travellmap.MainActivity;
import com.nomad.travellmap.R;

public class GeoFenceActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tab1;
    private TextView tab2;
    private FragmentTransaction ft;
    private AddFenceFragment fragment1;
    private ViewFenceFragment fragment2;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence);

        tab1 = (TextView) findViewById(R.id.tv_tab1_fence);
        tab2 = (TextView) findViewById(R.id.tv_tab2_fence);
        tab1.setOnClickListener(this);
        tab2.setOnClickListener(this);

        ft = getFragmentManager().beginTransaction();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
        fragment1 = new AddFenceFragment(latitude, longitude); //如果定位失败，此处的经纬度都是0
        ft.replace(R.id.container_fence, fragment1);
        ft.commit();
    }

    @Override
    public void onClick(View v) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        switch (v.getId()) {
            case R.id.tv_tab1_fence:
                fragment1 = new AddFenceFragment(latitude, longitude); //如果定位失败，此处的经纬度都是0
                ft.replace(R.id.container_fence, fragment1);
                break;
            case R.id.tv_tab2_fence:
                if (fragment2 == null) {
                    fragment2 = new ViewFenceFragment();
                }
                ft.replace(R.id.container_fence, fragment2);
                break;
        }
        ft.commit();
    }
}
