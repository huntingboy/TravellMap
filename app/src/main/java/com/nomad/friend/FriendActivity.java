package com.nomad.friend;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.nomad.fragment.AddFriendFragment;
import com.nomad.fragment.ViewFriendFragment;
import com.nomad.travellmap.R;

public class FriendActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tab1;
    private TextView tab2;
    private FragmentTransaction ft;
    private AddFriendFragment fragment1;
    private ViewFriendFragment fragment2;

    private int id; //记录当前哪个button被激活

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        tab1 = (TextView) findViewById(R.id.tv_tab1_friend);
        tab2 = (TextView) findViewById(R.id.tv_tab2_friend);
        tab1.setOnClickListener(this);
        tab2.setOnClickListener(this);

        ft = getFragmentManager().beginTransaction();

        fragment1 = new AddFriendFragment();
        ft.replace(R.id.container_friend, fragment1);
        ft.commit();
    }

    @Override
    public void onClick(View v) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        switch (v.getId()) {
            case R.id.tv_tab1_friend:
                if (id != R.id.tv_tab1_friend) {
                    tab2.setEnabled(true);
                    tab1.setEnabled(false);
                    ft.replace(R.id.container_friend, fragment1);
                    id = R.id.tv_tab1_friend;
                }
                break;
            case R.id.tv_tab2_friend:
                if (id != R.id.tv_tab2_friend) {
                    tab1.setEnabled(true);
                    tab2.setEnabled(false);
                    if (fragment2 == null) {
                        fragment2 = new ViewFriendFragment();
                    }
                    ft.replace(R.id.container_friend, fragment2);
                    id = R.id.tv_tab2_friend;
                }
                break;
        }
        ft.commit();
    }
}
