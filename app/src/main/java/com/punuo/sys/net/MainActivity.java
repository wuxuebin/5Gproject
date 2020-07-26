package com.punuo.sys.net;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.punuo.sys.net.push.ProcessTasks;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.ToastUtils;

import java.lang.reflect.Method;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity {
    private TelephonyManager telephonyManager;
    private List<CellInfo> cellInfoList;
    private TextView displayIdentity;
    private TextView displaySignal;
    private TextView netType;
    private static final int SIGN_GET = 0x0001;

    private Button mapbutton;


    private SDKReceiver mReceiver;

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();

            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(MainActivity.this,"apikey验证失败，地图功能无法正常使用",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Toast.makeText(MainActivity.this,"apikey验证成功",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        displaySignal = findViewById(R.id.display_sign);
        displayIdentity = findViewById(R.id.display_identity);
        netType = findViewById(R.id.netType);

        mapbutton=findViewById(R.id.mapbutton);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        mBaseHandler.sendEmptyMessage(SIGN_GET);
        MainActivityPermissionsDispatcher.is5GConnectedWithCheck(this);
        mapbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,DynamicDemo.class);
                startActivity(intent);
            }
        });
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    void is5GConnected() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            ServiceState serviceState = telephonyManager.getServiceState();
            try {
                Method hwOwnMethod = ServiceState.class.getMethod("getHwNetworkType");
                hwOwnMethod.setAccessible(true);
                int result = (int) hwOwnMethod.invoke(serviceState);
                Log.i("han.chen", "值为：" + result);
                if (result == 20) {
                    netType.setText("5g网络");
                    Log.i("han.chen", "5g网络");
                } else {
                    netType.setText("非5g网络");
                    Log.i("han.chen", "非5g网络");
                }
            } catch (Exception e) {
                Log.i("han.chen", e.toString());
            }
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void getAllCellInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cellInfoList = telephonyManager.getAllCellInfo();
        for (int i = 0; i < cellInfoList.size(); i++) {
            CellInfo cellInfo = cellInfoList.get(i);
            if (cellInfo instanceof CellInfoNr) {
                CellInfoNr cellInfoNr = (CellInfoNr) cellInfo;
                CellIdentityNr cellIdentityNr = (CellIdentityNr) cellInfoNr.getCellIdentity();
                StringBuilder identity = new StringBuilder();
                identity.append("基站信息: \n")
                        .append("基站编号 CID: ")
                        .append(cellIdentityNr.getNci())
                        .append("\n")
                        .append("Physical Cell Id: ")
                        .append(cellIdentityNr.getPci())
                        .append("\n")
                        .append("Tracking Area Code: ")
                        .append(cellIdentityNr.getTac())
                        .append("\n")
                        .append("New Radio Absolute Radio Frequency Channel Number: ")
                        .append(cellIdentityNr.getNrarfcn())
                        .append("\n")
                        .append("移动国家代码 MCC: ")
                        .append(cellIdentityNr.getMccString())
                        .append("\n")
                        .append("移动网络号码 MNC: ")
                        .append(cellIdentityNr.getMncString())
                        .append("\n");
                displayIdentity.setText(identity.toString());

                CellSignalStrengthNr cellSignalStrengthNr = (CellSignalStrengthNr) cellInfoNr.getCellSignalStrength();
                StringBuilder signal = new StringBuilder();
                signal.append("信号信息：\n")
                        .append("asuLevel: ")
                        .append(cellSignalStrengthNr.getAsuLevel())
                        .append("\n")
                        .append("dbm: ")
                        .append(cellSignalStrengthNr.getDbm())
                        .append("\n")
                        .append("level: ")
                        .append(cellSignalStrengthNr.getLevel())
                        .append("\n")
                        .append("csiRsrp: ")
                        .append(cellSignalStrengthNr.getCsiRsrp())
                        .append("\n")
                        .append("csiRsrq: ")
                        .append(cellSignalStrengthNr.getCsiRsrq())
                        .append("\n")
                        .append("csiSinr: ")
                        .append(cellSignalStrengthNr.getCsiSinr())
                        .append("\n")
                        .append("ssRsrp: ")
                        .append(cellSignalStrengthNr.getSsRsrp())
                        .append("\n")
                        .append("ssRsrq: ")
                        .append(cellSignalStrengthNr.getSsRsrq())
                        .append("\n")
                        .append("ssSinr: ")
                        .append(cellSignalStrengthNr.getSsSinr())
                        .append("\n");
                displaySignal.setText(signal.toString());
            }
        }
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION})
    void onPermissionError() {
        ToastUtils.showToast("权限获取失败");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        //获取信号集合
        MainActivityPermissionsDispatcher.getAllCellInfoWithCheck(this);
        mBaseHandler.sendEmptyMessageDelayed(SIGN_GET, 3 * 1000);
    }


}
