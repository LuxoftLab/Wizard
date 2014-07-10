package com.example.wizard1;

import android.media.AudioManager;
import android.media.SoundPool;

import com.example.wizard1.views.SelfGUI;
import com.example.wizard1.views.EnemyGUI;

import java.util.ArrayList;

import com.example.wizard1.components.Vector2d;
import com.example.wizard1.components.Vector4d;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
/**
 * This is the main Activity that displays the current chat session.
 */
public class WizardFight extends Activity {
    // Debugging
    private static final String TAG = "Wizard Fight";
    private static final boolean D = true;
    // States of players
    private SelfState mSelfState;
    private EnemyState mEnemyState;
    // Message types sent from the BluetoothChatService Handler
    enum AppMessage { 
    	MESSAGE_STATE_CHANGE,
    	MESSAGE_READ,
    	MESSAGE_WRITE,
    	MESSAGE_DEVICE_NAME,
    	MESSAGE_TOAST,
    	MESSAGE_ADD_PROJECTION,
    	MESSAGE_SET_PROJECTION,
    	MESSAGE_CLEAR_PROJECTION,
    	MESSAGE_FROM_SELF,
    	MESSAGE_FROM_ENEMY,
        MESSAGE_PLAY_SOUND;
    }
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Layout Views
    private TextView mTitle;
    private SelfGUI mSelfGUI;
    private EnemyGUI mEnemyGUI;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Objects refered to accelerometer
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    // Accelerator Thread link
    private AcceleratorThread mAcceleratorThread = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    // is volume click action is in process 
    private boolean isBetweenVolumeClicks = false;
    private boolean isVolumeButtonBlocked = false; //+++++++++++++++++++++IN FUTURE -  ACCESS AFTER CONNECTING and TRUE HERE
    
    private SoundPool soundPool;
    private int soundID1;
    private int streamID;
    private boolean soundLoaded = false;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundID1 = soundPool.load(this, R.raw.magic, 1);
        streamID=-1;
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
        	@Override
        	public void onLoadComplete(SoundPool soundPool, int sampleId,
        	int status) {
        	soundLoaded = true;
        	}
        	});
        
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        // Get sensors
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
    }
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        // Initialize new accelerator thread
        mAcceleratorThread = new AcceleratorThread(mSensorManager, mAccelerometer);
		mAcceleratorThread.start();
		Log.e(TAG, "accelerator ran");
    }
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        // Initialize text views from main layout
        // Create players states
        mSelfState = new SelfState(200, 100);
        mEnemyState = new EnemyState(200, 100);
        // Initialize self UI
        mSelfGUI = new SelfGUI(this, 200, 100);
        mEnemyGUI = new EnemyGUI(this, 200, 100);
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
    }
    @Override
    public synchronized void onPause() {
    	super.onPause();
    	if(D) Log.e(TAG, "- ON PAUSE -");
    	//unregister accelerator listener and end its event loop
    	if(mAcceleratorThread != null) {
    		Log.e(TAG, "accelerator thread try to stop loop");
    		mAcceleratorThread.stopLoop();
    		mAcceleratorThread = null;
    	}
    }
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void sendFightMessage(FightMessage fMessage) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            Toast.makeText(, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

    	byte[] send = fMessage.getBytes();
        mChatService.write(send);
    }
    
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
    	/**
         * Sends a message.
         * @param message  A string of text to send.
         */
    	
    	
    	@Override
        public void handleMessage(Message msg) {
        	AppMessage appMsg = AppMessage.values()[ msg.what ];
            switch (appMsg) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
//                    isVolumeButtonBlocked = false; in future
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
//                    isVolumeButtonBlocked = true; in future
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                mEnemyGUI.getPlayerName().setText(mConnectedDeviceName);
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_ADD_PROJECTION:
                ((ProjectionView)findViewById(R.id.pv)).addProjection((ArrayList<Vector2d>) msg.obj);
                break;
            case MESSAGE_SET_PROJECTION:
                ((ProjectionView)findViewById(R.id.pv)).setProjection((ArrayList<Vector2d>) msg.obj);
                break;
            case MESSAGE_CLEAR_PROJECTION:
                ((ProjectionView)findViewById(R.id.pv)).clearProjection();
                break;
            case MESSAGE_FROM_SELF:
            	try {
 
            	FightMessage sendMessage = (FightMessage) msg.obj;
            	sendMessage = FightMessage.fromBytes(sendMessage.getBytes());
            	mSelfGUI.getPlayerName().setText("message " + sendMessage);
            	
            
            	switch( sendMessage.action ) {
            	case DAMAGE:
            	case HIGH_DAMAGE:
            		sendFightMessage(sendMessage);
            		mEnemyGUI.getSpellPicture().setShape( sendMessage.shape );
            		break;
            	case BUFF_ON:
            		// add buff to state
            		if(sendMessage.shape == Shape.SQUARE) {
            			sendMessage.param = Buff.SHIELD_ONE_SPELL.ordinal();
            		}
            		mSelfState.handleSpell(sendMessage);
            		// send message to enemy
            		sendFightMessage(sendMessage);
            		if (!isIndexGood(sendMessage.param)) break;
            		// add buff to GUI
            		Buff newBuff = Buff.values()[ sendMessage.param ];
            		mSelfGUI.getBuffPanel().addBuff(newBuff);
            		// show figure
            		mSelfGUI.getSpellPicture().setShape( sendMessage.shape );  //DELETE IN FUTURE
            		break;
            	case HEAL:
            		mSelfState.handleSpell(sendMessage);
            		int newHP = mSelfState.getHealth();
            		//send new hp to enemy
            		sendMessage.setParam(newHP);
            		sendFightMessage(sendMessage);
            		
            		//refresh GUI
            		mSelfGUI.getSpellPicture().setShape( sendMessage.shape );
            		mSelfGUI.getHealthBar().setValue(newHP);
            		break;
            	case NONE:
            		//show fail
            		mSelfGUI.getSpellPicture().setShape( sendMessage.shape );
            		break;
            	default:
            		mSelfGUI.getPlayerName().setText("unknown msg from myself");
            	}
            	
            	Log.e("Wizard Fight", "Enemy damage handling");
            	isVolumeButtonBlocked = false;
            	
            	
            	} catch(Exception e) {
            		mSelfGUI.getPlayerName().setText(e.toString());
            	}
            	break;
            case MESSAGE_FROM_ENEMY:
            	try {
            
            	byte[] recvBytes = (byte[]) msg.obj;
            	FightMessage recvMessage = FightMessage.fromBytes(recvBytes);
            	mSelfGUI.getPlayerName().setText(recvMessage.toString());
            	
            	switch( recvMessage.action ) {
            	case DAMAGE:
            	case HIGH_DAMAGE:
            		mSelfState.handleSpell(recvMessage);
            		
            		if( mSelfState.getCanceledBuff() != null ) {
            			Buff canceledBuff = mSelfState.getCanceledBuff();
            			//remove buff from panel
            			mSelfGUI.getBuffPanel().removeBuff(canceledBuff);
            			//send message about buff loss to enemy
            			FightMessage fm = new FightMessage(FightAction.BUFF_OFF);
            			fm.setParam( canceledBuff.ordinal() );
            			sendFightMessage( fm );	
            		}
            		
            		if( mSelfState.healthChanged ) {
            			//COUNT REVEIVED DAMAGE
                		int newHP = mSelfState.getHealth();
                		//send new hp to enemy
                		FightMessage fm = new FightMessage(FightAction.NEW_HP);
                		fm.setParam(newHP);
                		sendFightMessage( fm );	
                		
                		//refresh GUI
                		mSelfGUI.getSpellPicture().setShape( recvMessage.shape );
                		mSelfGUI.getHealthBar().setValue(newHP);
                		
                		//if lose
                		if( newHP == 0 ) {
                			Toast.makeText(getApplicationContext(), "YOU LOSE!",
                                    Toast.LENGTH_SHORT).show();
                		}
            		}
            		
            		break;
            	case BUFF_ON:
            		// received buff 
            		if (!isIndexGood(recvMessage.param)) break;
            		Buff buffToOn = Buff.values()[ recvMessage.param ];
            		// add buff to enemy state
            		mEnemyState.handleSpell(recvMessage);
            		// draw shape to enemy GUI
            		mEnemyGUI.getSpellPicture().setShape( recvMessage.shape );
            		// add buff to enemy GUI
            		mEnemyGUI.getBuffPanel().addBuff(buffToOn);
            		break;
            	case BUFF_OFF:
            		Log.e("Wizard Fight", "BUFF OFF PARAM: " + recvMessage.param);
            		if (!isIndexGood(recvMessage.param)) break;
            		Buff buffToOff = Buff.values()[ recvMessage.param ];
            		// remove buff from state
            		mEnemyState.handleSpell(recvMessage);
            		// remove buff from enemy GUI
            		mEnemyGUI.getBuffPanel().removeBuff(buffToOff);
            		break;
            	case HEAL:
            		mEnemyState.handleSpell(recvMessage);
            		//hp change after heal
            		mEnemyGUI.getSpellPicture().setShape( recvMessage.shape );
            		mEnemyGUI.getHealthBar().setValue( recvMessage.param );
            		break;
            	case NEW_HP:
            		//hp change after damage
            		int enemyHP = recvMessage.param;
            		mEnemyGUI.getHealthBar().setValue( enemyHP );
            		
            		if( enemyHP == 0 ) {
            			Toast.makeText(getApplicationContext(), "YOU WIN!",
                                Toast.LENGTH_SHORT).show();
            		}
            		break;
            	default:
            		mSelfGUI.getPlayerName().setText("unknown msg from enemy");
            	}
            	
            	} catch(Exception e) {
            		mSelfGUI.getPlayerName().setText(e.toString());
            	}
            	break;
            case MESSAGE_PLAY_SOUND:
            	if( !soundLoaded ) {
            		mSelfGUI.getPlayerName().setText("SOUND NOT LOADED");
            	}
//            	Log.e(TAG, "obj: " + (Float) msg.obj);
//                soundPool.setRate(streamID,(Float) msg.obj/2+0.75f);
                soundPool.setVolume(streamID,((Float) msg.obj),((Float) msg.obj));
//                Log.e(TAG, "Rate: " + (Float) msg.obj/2+0.75f + ", volume: " + ((Float) msg.obj)/3+0.25f );
                break;
            default:
            	Log.e("Wizard Fight", "Self damage handling");
            	break;
            }
        }
    };
    
    public boolean isIndexGood(int index) {
    	boolean indexGood = (index >= 0 && index <= 4);
    	if(!indexGood) { 
    		mSelfGUI.getPlayerName().setText("Wrong buff index!" + index);
    	}
    	return indexGood;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    public void buttonClick() {
    	if( isVolumeButtonBlocked ) return;
    	
		if( !isBetweenVolumeClicks ) {
			mAcceleratorThread.startGettingData();
//			mEnemyGUI.getPlayerName().setText("volume button started");
			isBetweenVolumeClicks = true;
			if( streamID == -1 ) {
				streamID=soundPool.play(soundID1, 0.25f, 0.25f, 0, -1, 1);
			} else {
				soundPool.resume(streamID);
			}
			
		} else {
			isVolumeButtonBlocked = true;
			soundPool.pause(streamID);
			
			ArrayList<Vector4d> records = mAcceleratorThread.stopAndGetResult();
			isBetweenVolumeClicks  = false;
			
			if( records.size() > 10 ) {
				new RecognitionThread(mHandler, records).start();
			} else {
				// if shord record - don`t recognize & unblock
				isVolumeButtonBlocked = false;
			}		
		}
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    switch( keyCode ) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (action == KeyEvent.ACTION_DOWN) {
            	buttonClick();
            }
            return true;
        default:
            return super.dispatchKeyEvent(event);
        }
	}
}