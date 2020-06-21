package com.punuo.sys.net;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

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
    private static final int SIGN_GET = 0x0001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        displaySignal = findViewById(R.id.display_sign);
        displayIdentity = findViewById(R.id.display_identity);
        mBaseHandler.sendEmptyMessage(SIGN_GET);
        MainActivityPermissionsDispatcher.is5GConnectedWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    void is5GConnected() {
        if (Build.VERSION.SDK_INT >= 29) {
            ServiceState serviceState = telephonyManager.getServiceState();
            try {
                Method hwOwnMethod = ServiceState.class.getMethod("getHwNetworkType");
                hwOwnMethod.setAccessible(true);
                int result = (int) hwOwnMethod.invoke(serviceState);
                Log.i("han.chen", "值为：" + result);
                if (result == 20) {
                    Log.i("han.chen", "5g网络");
                } else {
                    Log.i("han.chen", "非5g网络");
                }
            } catch (Exception e) {
                Log.i("han.chen", e.toString());
            }
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void getAllCellInfo() {
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
                        .append("ssRinr: ")
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
