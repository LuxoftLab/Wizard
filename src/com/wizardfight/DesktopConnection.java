package com.wizardfight;

import com.wizardfight.remote.WifiService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DesktopConnection extends Activity {
	private Button mConnect;
	private Button mSettings;
	private TextView mConnStatus;
	private TextView mNetName;
	private EditText mIP;
	private boolean mConnected;
	private BroadcastReceiver mBroadcastReceiver;
	
	private final Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			if (msg.what == WifiService.INIT_NO_ERROR) {
				mConnected = true;
				setConnectedToPC();
			} else if (msg.what == WifiService.INIT_FAILED) {
				mConnected = false;
				setConnectedToPC();
			}
		};
	};

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

				if (networkInfo.isConnected()) {
					showConnectionPage();
				} else {
					showNoConnectionPage();
				}
			}
		};
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	private void showNoConnectionPage() {
		setContentView(R.layout.net_conn_not_found);
		mSettings = (Button) findViewById(R.id.net_settings_button);
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
	}
	
	private void showConnectionPage() {
		WifiManager m = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = m.getConnectionInfo();
		DhcpInfo dhcpInfo = m.getDhcpInfo();

		setContentView(R.layout.net_conn_found);
		mConnStatus = (TextView) findViewById(R.id.pc_conn_status);
		mNetName = (TextView) findViewById(R.id.net_name);
		mIP = (EditText) findViewById(R.id.ip);

		mNetName.setText(getString(R.string.net_name) + wifiInfo.getSSID());
		mIP.setText(convertIp(dhcpInfo.ipAddress));
		mConnect = (Button) findViewById(R.id.connect_to_pc);
		mConnected = false;
		setConnectedToPC();
		mConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(mConnected)) {
					WifiService.init(mIP.getText().toString(), mHandler);
					Log.e("Wizard Fight", mIP.getText().toString());
				} else {
					WifiService.close();
					mConnected = false;
					setConnectedToPC();
				}
			}
		});

	}
	
	private void setConnectedToPC() {
//		mConnected = WifiService.isConnected();
		if (mConnected) {
			Log.e("Wizard Fight", " -- INIT NO ERROR -- ");
			mConnStatus.setText(R.string.pc_conn_established);
			mConnect.setText(R.string.disconnect);
		} else {
			Log.e("Wizard Fight", " -- INIT WITH ERROR -- ");
			mConnStatus.setText(R.string.pc_conn_not_established);
			mConnect.setText(R.string.connect);
		}
		Log.e("Wizard Fight", "IS ALIVE: " + WifiService.isConnected());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mBroadcastReceiver);
		mHandler.removeCallbacksAndMessages(null);
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