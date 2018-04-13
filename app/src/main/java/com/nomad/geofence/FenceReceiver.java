package com.nomad.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.nomad.fragment.AddFenceFragment;
import com.nomad.travellmap.MainActivity;

//接收地理围栏行为广播
public class FenceReceiver extends BroadcastReceiver {

    private Context context;
    public FenceReceiver() {}
    public FenceReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(AddFenceFragment.GEOFENCE_BROADCAST_ACTION)) {
            Log.d("Amap", "fenceReceiver->onreceive()->GEOFENCE_BROADCAST_ACTION" + "地理围栏广播被触发");
            //解析广播内容
            //获取Bundle
            Bundle bundle = intent.getExtras();
            //获取围栏行为：
            int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
            //获取自定义的围栏标识：
            String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
            //获取围栏ID:
            String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
            //获取当前有触发的围栏对象：
            GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
            Log.d("Amap", "Mainactivity->mGeoFenceReceiver->onReceive===status:" + status + ", cutomId:" + customId);
            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            if (status == 1) {
                Toast.makeText(context, "进入地理围栏~", Toast.LENGTH_LONG).show();
                vibrator.vibrate(3000);
            } else if (status == 2) {
                // 离开围栏区域
                Toast.makeText(context, "离开地理围栏~", Toast.LENGTH_LONG).show();
                vibrator.vibrate(3000);
            } else if (status == 3) {
                // 围栏区域待了10分钟
                Toast.makeText(context, "地理围栏待了十分钟~", Toast.LENGTH_LONG).show();
                vibrator.vibrate(3000);
            }
        }
    }
}
