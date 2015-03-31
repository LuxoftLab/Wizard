package com.wizardfight;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.wizardfight.remote.WifiService;

/*
 * Manages Desktop connection for fight translation . 
 * Class checks net connection, shows its info, 
 * initiates connects/disconnects device to pc via TCP socket
 */
public class DesktopConnection extends Activity {
    private final Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            if (msg.what == WifiService.NO_ERROR) {
                showConnectedToPC();
            } else if (msg.what == WifiService.IO_FAIL) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.pc_conn_fail), Toast.LENGTH_SHORT).show();
                showConnectedToPC();
            }
        }
    };
    Dialog mNetDialog;
    private Button mConnect;
    private Button mSettings;
    private TextView mConnStatus;
    private TextView mNetName;
    private EditText mIP;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager conn = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = conn
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                showConnectionPage();
                if (!networkInfo.isConnected()) {
                    findViewById(R.id.pc_conn_layout).setVisibility(View.INVISIBLE);
                    showNoConnectionPage();
                }
            }
        };
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetDialog != null) {
            mNetDialog.dismiss();
            mNetDialog = null;
        }
        this.unregisterReceiver(mBroadcastReceiver);
        WifiService.clearHandler();
        mHandler.removeCallbacksAndMessages(null);

    }

    private void showNoConnectionPage() {

        //setContentView(R.layout.net_conn_not_found);
        View v = getLayoutInflater().inflate(R.layout.net_conn_not_found, null);
        mNetDialog = new Dialog(this, R.style.WDialog);
        mNetDialog.setTitle(R.string.no_network_conn);

        mSettings = (Button) v.findViewById(R.id.net_settings_button);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(
                        Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName(
                        "com.android.settings",
                        "com.android.settings.wifi.WifiSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mNetDialog.setContentView(v);
        mNetDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mNetDialog.dismiss();
                    mNetDialog = null;
                    finish();
                }
                return true;
            }
        });
        mNetDialog.show();
    }

    private void showConnectionPage() {
        WifiManager m = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = m.getConnectionInfo();
        DhcpInfo dhcpInfo = m.getDhcpInfo();

        setContentView(R.layout.net_conn_found);
        if (mNetDialog != null) {
            mNetDialog.dismiss();
            mNetDialog = null;
        }
        mConnStatus = (TextView) findViewById(R.id.pc_conn_status);
        mNetName = (TextView) findViewById(R.id.net_name);
        mIP = (EditText) findViewById(R.id.ip);
        if (wifiInfo.getSSID() == null)
            mNetName.setText(getString(R.string.net_name));
        else
            mNetName.setText(getString(R.string.net_name) + ": " + wifiInfo.getSSID());
        String lastIP = WifiService.getIP();
        if (lastIP != null) {
            mIP.setText(lastIP);
        } else {
            mIP.setText(convertIp(dhcpInfo.ipAddress));
        }
        mConnect = (Button) findViewById(R.id.connect_to_pc);

        showConnectedToPC();
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!WifiService.isConnected()) {
                    WifiService.init(mIP.getText().toString(), mHandler);
                } else {
                    WifiService.close();
                    showConnectedToPC();
                }
            }
        });
    }

    private void showConnectedToPC() {
        boolean connected = WifiService.isConnected();

        if (connected) {
            mConnStatus.setText(R.string.pc_conn_established);
            mConnect.setText(R.string.disconnect);
        } else {
            mConnStatus.setText(R.string.pc_conn_not_established);
            mConnect.setText(R.string.connect);
        }
    }

    private String convertIp(int adr) {
        int[] ip = new int[4];
        for (int i = 0; i < 4; i++) {
            ip[i] = adr & 0xff;
            adr >>= 8;
        }
        return String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
    }
}