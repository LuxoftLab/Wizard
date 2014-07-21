package com.example.wizard1;

import android.media.AudioManager;
import android.media.SoundPool;

import com.example.wizard1.views.SelfGUI;
import com.example.wizard1.views.EnemyGUI;

import java.io.IOException;
import java.util.ArrayList;

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
    	MESSAGE_FROM_SELF,
    	MESSAGE_FROM_ENEMY,
    	MESSAGE_MANA_REGEN,
        MESSAGE_PLAY_SOUND;
    }
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_START_FIGHT = 2;
    // Layout Views
    private TextView mTitle;
    private SelfGUI mSelfGUI;
    private EnemyGUI mEnemyGUI;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Objects referred to accelerometer
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    // Accelerator Thread link
    private AcceleratorThread mAcceleratorThread = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for bluetooth services
    private BluetoothChatService mChatService = null;
    // is volume click action is in process 
    private boolean isBetweenVolumeClicks = false;
    private boolean isVolumeButtonBlocked = false; //+++++++++++++++++++++IN FUTURE -  ACCESS AFTER CONNECTING and TRUE HERE
    
    private SoundPool soundPool;
    private int soundID1;
    private int streamID;
    
    private double gravity;
    private int myCounter;
    
    private boolean isCountdown = false;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        gravity = 0.0;
        try {
			Recognition.init(getResources());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("recognition", "", e);
		}
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
        // send fight request to enemy
        setupChat();
        if( mChatService.isServer() ) {
        	FightMessage fightRequest = 
        			new FightMessage(Target.ENEMY, FightAction.FIGHT_REQUEST);
        	sendFightMessage(fightRequest);
        	Log.e(TAG, "Fight request sent");
        }
        // start mana regen
        mHandler.removeMessages(AppMessage.MESSAGE_MANA_REGEN.ordinal());
        mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
        	.sendToTarget();
    }
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        if (mChatService == null) setupChat();
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Initialize new accelerator thread
        mAcceleratorThread = new AcceleratorThread(mSensorManager, mAccelerometer, gravity);
		mAcceleratorThread.start();
		Log.e(TAG, "accelerator ran");
		// Initialize sound
		Log.e(TAG, "Sound pool is null? : " + (soundPool == null));
		if(soundPool == null) {
        	soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        	soundID1 = soundPool.load(this, R.raw.magic, 1);
            streamID=-1;
        }
    }
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        // Initialize text views from main layout
        // Create players states
        mEnemyState = new EnemyState(200, 500, null);
        mSelfState = new SelfState(200, 500, mEnemyState);
        // Initialize players UI
        mSelfGUI = new SelfGUI(this, 200, 500);
        mEnemyGUI = new EnemyGUI(this, 200, 500);
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = BluetoothChatService.getInstance();
        mChatService.setHandler(mHandler);
    }
    @Override
    public synchronized void onPause() {
    	super.onPause();
    	if(D) Log.e(TAG, "- ON PAUSE -");
    	// if paused by countdown - don`t touch anything
    	if(isCountdown) return;
    	// stop cast if its started
    	if(isBetweenVolumeClicks) buttonClick();
    	// unregister accelerator listener and end its event loop
    	if(mAcceleratorThread != null) {
    		Log.e(TAG, "accelerator thread try to stop loop");
    		mAcceleratorThread.stopLoop();
    		mAcceleratorThread = null;
    	}
    	if(soundPool != null && streamID != -1) {
    		soundPool.stop(streamID);
    		soundPool.release();
    		soundPool = null;
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
    
    private void sendFightMessage(FightMessage fMessage) {
    	// always send own health and mana
    	fMessage.health = mSelfState.getHealth();
    	fMessage.mana = mSelfState.getMana();
    	
    	mSelfGUI.getPlayerName().setText("send fm: " + fMessage + " " + (myCounter++));
    	
//    	Log.e(TAG, "send fm: " + fMessage + " " + myCounter);
//        Log.e(TAG, "state: " + mChatService.getState());
    	
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

    	byte[] send = fMessage.getBytes();
        mChatService.write(send);
    }
    
    private void startCountdown() {
    	Intent intent = new Intent(this, Countdown.class);
    	startActivityForResult(intent, REQUEST_START_FIGHT);
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
                    break;
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_FROM_SELF:
            	FightMessage selfMsg = (FightMessage) msg.obj;
            	handleSelfMessage(selfMsg);
            	break;
            case MESSAGE_FROM_ENEMY:
            	byte[] recvBytes = (byte[]) msg.obj;
    			FightMessage enemyMsg = FightMessage.fromBytes(recvBytes);
    			mEnemyGUI.log("enemy msg: " + enemyMsg + " "  + (myCounter++));
//    			Log.e(TAG, "enemy msg: " + enemyMsg + " "  + (myCounter));
    			if(enemyMsg.action == FightAction.FIGHT_REQUEST) {
    				// if client, send approval to server
    				if(!mChatService.isServer()) {
    					sendFightMessage(enemyMsg);
    				}
    				// start countdown
    				Log.e(TAG, "before start countdown");
    				isCountdown = true;
    				startCountdown();
    				Log.e(TAG, "after start countdown");
    				// start calibration
    				mAcceleratorThread = new AcceleratorThread(mSensorManager, mAccelerometer, gravity);
    				mAcceleratorThread.start();
    				mAcceleratorThread.startGettingData();
    				Log.e(TAG, "accelerator thread all stuff called");
    			} else {
    				handleEnemyMessage(enemyMsg);
    			}
            	break;
            case MESSAGE_MANA_REGEN:
            	mSelfState.manaTick();
            	mEnemyState.manaTick();
            	mSelfGUI.getManaBar().setValue(mSelfState.getMana());
            	mEnemyGUI.getManaBar().setValue(mEnemyState.getMana());
            	Message msgManaReg = this.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), 0, 0, null);
            	this.sendMessageDelayed(msgManaReg, 2000);
            	break;
            case MESSAGE_PLAY_SOUND:
                soundPool.setVolume(streamID,((Float) msg.obj),((Float) msg.obj));
                break;
            default:
            	if(D) Log.e("Wizard Fight", "Self damage handling");
            	break;
            }
        }
    	
    	private void handleSelfMessage(FightMessage selfMsg) {
			Shape sendShape = FightMessage
					.getShapeFromMessage(selfMsg);
			if(sendShape != Shape.NONE) {
				isVolumeButtonBlocked = false;
			}
			mSelfGUI.log("self msg : " + selfMsg + " "  + (myCounter++));
			if(D) Log.e(TAG, "self msg : " + selfMsg + " "  + myCounter);
			boolean canBeCasted = mSelfState.requestSpell(selfMsg);
			if( !canBeCasted ) return;
			mSelfGUI.getManaBar().setValue(mSelfState.mana);

			if (selfMsg.target == Target.SELF) {
				// self influence to self
				handleMessageToSelf(selfMsg);
			} else {
				// self influence to enemy
				// tell enemy : target is he
				selfMsg.target = Target.SELF;
				sendFightMessage(selfMsg);
				if(sendShape != Shape.NONE) {
					mEnemyGUI.getSpellPicture().setShape(sendShape);
				}
			}
	}

	private void handleEnemyMessage(FightMessage enemyMsg) {
			
			Shape recvShape = FightMessage
					.getShapeFromMessage(enemyMsg);

			// refresh enemy health and mana (every enemy message contains it)
			mEnemyState.setHealthAndMana(enemyMsg.health, enemyMsg.mana);
			mEnemyGUI.getPlayerName().setText("enemy hp and mana: " + enemyMsg.health + ", " + enemyMsg.mana);
			if(D) Log.e(TAG, "enemy msg: " + enemyMsg + " "  + myCounter);
			if (enemyMsg.target == Target.SELF) {
				handleMessageToSelf(enemyMsg);
			} else {
				// Enemy influence to himself
				mEnemyState.handleSpell(enemyMsg);
				if (mEnemyState.getRemovedBuff() != null) {
					// remove buff from enemy GUI
					mEnemyGUI.getBuffPanel().removeBuff(
							mEnemyState.getRemovedBuff());
				}

				if (mEnemyState.getAddedBuff() != null) {
					// add buff to enemy GUI
					mEnemyGUI.getBuffPanel().addBuff(
							mEnemyState.getAddedBuff());
				}
				
				if(recvShape != Shape.NONE) {
					mEnemyGUI.getSpellPicture().setShape(recvShape);
				}
			}
			
			mEnemyGUI.getHealthBar().setValue(mEnemyState.health);
			mEnemyGUI.getManaBar().setValue(mEnemyState.mana);
	}
	
	private void handleMessageToSelf(FightMessage fMessage) {
		Log.e(TAG, "Message to self" + fMessage);
		FightMessage sendMsg;
		// Enemy influence to player
		mSelfState.handleSpell(fMessage);
		Buff addedBuff = mSelfState.getAddedBuff();
		Buff removedBuff = mSelfState.getRemovedBuff();
		Buff refreshedBuff = mSelfState.getRefreshedBuff();
		Shape spellShape = mSelfState.getSpellShape();
		
		if (removedBuff != null) {
			// buff was removed after spell,
			// send message about buff loss to enemy
			sendMsg = new FightMessage(Target.ENEMY,
					FightAction.BUFF_OFF, removedBuff.ordinal());
			sendFightMessage(sendMsg);
			// remove buff from panel
			mSelfGUI.getBuffPanel().removeBuff(removedBuff);	
		} 

		if (addedBuff != null) {
			// buff added to player after spell (for example
			// DoT, HoT,  or shield),
			// send message about enemy buff success
			sendMsg = new FightMessage(Target.ENEMY,
					FightAction.BUFF_ON, addedBuff.ordinal());
			sendFightMessage(sendMsg);
			// add buff to panel
			mSelfGUI.getBuffPanel().addBuff(addedBuff);
		}
		
		if (addedBuff != null || refreshedBuff != null) {
			// send message of the buff tick 
			if(addedBuff != null) refreshedBuff = addedBuff;
			FightMessage fm = new FightMessage(Target.SELF,
					FightAction.BUFF_TICK, refreshedBuff.ordinal());
			Message buffTickMsg = 
					this.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), fm);
			this.sendMessageDelayed(buffTickMsg, refreshedBuff.getDuration());
		}
		
		if(addedBuff == null && removedBuff == null) {
			// nothing with buffs => just send self hp and mana to enemy
			sendMsg = new FightMessage(Target.ENEMY, FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
			sendFightMessage(sendMsg);
		}
		
		mSelfGUI.getHealthBar().setValue(mSelfState.health);
		mSelfGUI.getManaBar().setValue(mSelfState.mana);

		if (mSelfState.getSpellShape() != Shape.NONE) {
			mSelfGUI.getSpellPicture().setShape(spellShape);
		}
	}
	
    };
        
    public void buttonClick() {
    	if( isVolumeButtonBlocked ) return;
    	
		if( !isBetweenVolumeClicks ) {
			mAcceleratorThread.startGettingData();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_START_FIGHT:
        	// here we can stop calibration
        	Log.e(TAG, "Acc null? : " + (mAcceleratorThread == null));
        	mAcceleratorThread.stopGettingData();
        	gravity = mAcceleratorThread.recountGravity();
        	mAcceleratorThread.stopLoop();
    		mAcceleratorThread = null;
        	Log.e(TAG, "countdown gravity: " + gravity);
        	isCountdown = false;
        	break;
        }
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