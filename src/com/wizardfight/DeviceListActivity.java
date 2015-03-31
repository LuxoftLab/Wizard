package com.wizardfight;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/* Activity shows bounded devices and devices with enabled & visible Bluetooth 
 * When other device is chosen, Activity tries to connect to BT socket and starts 
 * fight activity
 */
public class DeviceListActivity extends Activity {
   
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = false;
    
    private LinearLayout mPairedList;
    private View mNewTitleLayout;
    private View mProgress;
    private TextView mTitleNewDevices;
    private LinearLayout mNewDevicesList;
    
    private int mNewDevicesCount;
    private String mNoNewDevices;
    private String mNoPairedDevices;

    private BluetoothAdapter mBtAdapter;
 	private BluetoothService mBtService = null;
 	private boolean mSearchCanceledByUser = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
			Toast.makeText(this, R.string.bt_not_available,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

        mNoNewDevices = getResources().getString(R.string.none_found);
        mNoPairedDevices = getResources().getString(R.string.none_paired);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        mPairedList = (LinearLayout) findViewById(R.id.paired_devices);
        mNewTitleLayout = findViewById(R.id.title_new_dev_layout);
        mProgress = findViewById(R.id.devices_scan_progress);
        mTitleNewDevices = (TextView)findViewById(R.id.title_new_devices);
        mNewDevicesList = (LinearLayout) findViewById(R.id.new_devices);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        // Add paired devices to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	addPairedDevice(device.getName() + "\n" + device.getAddress());
            }
        } else {
            addPairedDevice(mNoPairedDevices);
        }
    }
    
    @Override
	public void onStart() {
		super.onStart();
		if (mBtService == null) setup();
	}
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }
    
    private void setup() {
    	mBtService = BluetoothService.getInstance();
    	mBtService.init();
    }
    
    private void addPairedDevice(String s ) {
    	TextView v = (TextView) View.inflate(this, R.xml.device_list_item, null);
    	v.setOnClickListener(mDeviceClickListener);
    	v.setText(s);
    	mPairedList.addView(v);
    }
    private void addNewDevice(String s ) {
    	TextView v = (TextView) View.inflate(this, R.xml.device_list_item, null);
    	v.setOnClickListener(mDeviceClickListener);
    	v.setText(s);
    	mNewDevicesList.addView(v);
    }
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
    	mSearchCanceledByUser = false;
        if (D) Log.d(TAG, "doDiscovery()");

        if(mNewTitleLayout.getVisibility() != View.VISIBLE)
        	mNewTitleLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.devices_scan_progress).setVisibility(View.VISIBLE);

        mTitleNewDevices.setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    private final OnClickListener mDeviceClickListener = new OnClickListener() {
        public void onClick(View v) {
        	mSearchCanceledByUser = true;
            mBtAdapter.cancelDiscovery();
            
            String info = ((TextView) v).getText().toString();
            if( info.equals(mNoNewDevices) || info.equals(mNoPairedDevices)) {
            	return;
            }
            
            // parse MAC address
            String address = info.substring(info.length() - 17);
            BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
            mBtService.connect(device);      

            startFight();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    addNewDevice(device.getName() + "\n" + device.getAddress());
                    mNewDevicesCount++;
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	mProgress.setVisibility(View.GONE);
                mTitleNewDevices.setText(R.string.title_other_devices);
                if (mNewDevicesCount == 0) {
                	if(!mSearchCanceledByUser)
                		addNewDevice(mNoNewDevices);
                }
            }
        }
    };
    
    private void startFight() {
    	startActivity(new Intent(this, BtFightActivity.class));
    }
}
