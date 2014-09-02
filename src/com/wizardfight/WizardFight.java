package com.wizardfight;

import com.wizardfight.recognition.Recognizer;
import com.wizardfight.views.*;

import java.util.ArrayList;

import com.wizardfight.components.Vector3d;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class WizardFight extends Activity {
	public static final int PLAYER_HP = 200;
	public static final int PLAYER_MANA = 100;
	// Debugging
	private PlayerBot mPlayerBot;
	private boolean mIsEnemyBot = false;
	private int myCounter;
	private static final String TAG = "Wizard Fight";
	private static final boolean D = false;
	// States of players
	private SelfState mSelfState;
	private EnemyState mEnemyState;
	private boolean areMessagesBlocked;

	// Message types sent from the BluetoothChatService Handler
	enum AppMessage {
		MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_WRITE, MESSAGE_DEVICE_NAME, MESSAGE_TOAST, MESSAGE_CONNECTION_FAIL, MESSAGE_FROM_SELF, MESSAGE_SELF_DEATH, MESSAGE_FROM_ENEMY, MESSAGE_MANA_REGEN;
	}

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Intent request codes
	private static final int REQUEST_START_FIGHT = 1;
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
	private SensorAndSoundThread mSensorAndSoundThread = null;
	// Member object for bluetooth services
	private BluetoothService mChatService = null;
	// Last key event action code 
	private int mLastAction = -1;
	// is volume click action is in process
	private boolean isBetweenVolumeClicks = false;
	private boolean isVolumeButtonBlocked = false; 
	private boolean isCountdown;
	private boolean isSelfReady;
	private boolean isEnemyReady;

	private Dialog mClientWaitingDialog;
	private EndDialogListener endDialogListener;
	// test mode dialog with spell names
	private ArrayAdapter<String> mShapeNames;
	private AlertDialog.Builder mBotSpellDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		Recognizer.init(getResources());

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// Get sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// check if it`s test mode
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mIsEnemyBot = extras.getBoolean("IS_ENEMY_BOT");
		}
		if (D)
			Log.e(TAG, "Enemy bot?: " + mIsEnemyBot);

		// initialize GUI and logic
		setupApp();
		isSelfReady = false;
		isEnemyReady = false;
		// Start listening clients if server
		if (mIsEnemyBot) {
			startFight();
		} else {
			if (mChatService.isServer()) {
				mChatService.start();
				initWaitingDialog(R.string.client_waiting);
			} else {
				initWaitingDialog(R.string.trying_to_connect);
			}
		}
		// Initialize bot spells dialog
		if (mIsEnemyBot) {
			initBotSpellDialog();
		}
		// Initialize end dialog listener
		endDialogListener = new EndDialogListener();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		if (isCountdown)
			return;
		// Initialize new accelerator thread
		mSensorAndSoundThread = new SensorAndSoundThread(this, mSensorManager,
				mAccelerometer);
		mSensorAndSoundThread.start();
		if (D)
			Log.e(TAG, "accelerator ran");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
		// if paused by countdown - don`t touch anything
		Log.e(TAG, "is countdown: " + isCountdown);
		if (isCountdown)
			return;

		stopSensorAndSound();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void stopSensorAndSound() {
		Log.e("Wizard Fight", "stop sensor and sound called");
		// stop cast if its started
		if (isBetweenVolumeClicks) {
			isBetweenVolumeClicks = false;
			isVolumeButtonBlocked = false;
		}

		if (mSensorAndSoundThread != null) {
			// stop cast
			mSensorAndSoundThread.stopGettingData();
			// unregister accelerator listener and end stop event loop
			if (D)
				Log.e(TAG, "accelerator thread try to stop loop");
			mSensorAndSoundThread.stopLoop();
			mSensorAndSoundThread = null;
		}
	}

	private void setupApp() {
		if (D)
			Log.d(TAG, "setupApp()");
		// for debugging
		myCounter = 0;
		if (mIsEnemyBot) {
			mPlayerBot = new PlayerBot(PLAYER_HP, PLAYER_MANA, mHandler);
		}
		// Create players states
		mEnemyState = new EnemyState(PLAYER_HP, PLAYER_MANA, null);
		mSelfState = new SelfState(PLAYER_HP, PLAYER_MANA, mEnemyState);
		// Initialize players UI
		mSelfGUI = new SelfGUI(this, PLAYER_HP, PLAYER_MANA);
		mEnemyGUI = new EnemyGUI(this, PLAYER_HP, PLAYER_MANA);
		// Initialize the BluetoothChatService to BT connections
		mChatService = BluetoothService.getInstance();
		mChatService.setHandler(mHandler);
		// Drop flags
		areMessagesBlocked = true;
		isCountdown = false;
		// Start mana regeneration
		mHandler.removeMessages(AppMessage.MESSAGE_MANA_REGEN.ordinal());
		mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
				.sendToTarget();
	}

	private void initWaitingDialog(int stringId) {
		View v = getLayoutInflater().inflate(R.layout.client_waiting, null);
		mClientWaitingDialog = new Dialog(this, R.style.ClientWaitingDialog);
		mClientWaitingDialog.setTitle(stringId);
		CancelButton cancel = (CancelButton) v
				.findViewById(R.id.button_cancel_waiting);
		cancel.setOnClickListener(new CancelButtonListener());
		mClientWaitingDialog.setContentView(v);
		if (D)
			Log.e(TAG, "Before show dialog");
		mClientWaitingDialog.show();
		mClientWaitingDialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
					mClientWaitingDialog.dismiss();
				}
				return true;
			}
		});
		if (D)
			Log.e(TAG, "After show dialog");
	}

	private void sendFightMessage(FightMessage fMessage) {
		// always send own health and mana
		fMessage.health = mSelfState.getHealth();
		fMessage.mana = mSelfState.getMana();

		mSelfGUI.getPlayerName().setText(
				"send fm: " + fMessage + " " + (myCounter++));

		if (mIsEnemyBot) {
			if (mPlayerBot.getHandler() == null)
				return;
			Message msg = mPlayerBot.getHandler().obtainMessage(
					AppMessage.MESSAGE_FROM_ENEMY.ordinal(), fMessage);
			msg.sendToTarget();
			return;
		}
		// if (D) Log.e(TAG, "send fm: " + fMessage + " " + myCounter);
		// if (D) Log.e(TAG, "state: " + mChatService.getState());

		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(getApplicationContext(), R.string.not_connected,
					Toast.LENGTH_SHORT).show();
			return;
		}

		byte[] send = fMessage.getBytes();
		mChatService.write(send);
	}

	private void startFight() {
		// close waiting dialog if opened
		if (mClientWaitingDialog != null) {
			mClientWaitingDialog.dismiss();
		}
		// start countdown
		if (D)
			Log.e(TAG, "before start countdown");
		isCountdown = true;
		Intent i = new Intent(this, Countdown.class);
		startActivityForResult(i, REQUEST_START_FIGHT);
		if (D)
			Log.e(TAG, "after start countdown");
		if (D)
			Log.e(TAG, "accelerator thread all stuff called");
		// drop ready flags
		isSelfReady = false;
		isEnemyReady = false;
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		/**
		 * Sends a message.
		 * 
		 * @param msg
		 *            A string of text to send.
		 */

		@Override
		public void handleMessage(Message msg) {
			AppMessage appMsg = AppMessage.values()[msg.what];
			switch (appMsg) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// start fight
					startFight();
					break;
				case BluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_CONNECTION_FAIL:
				Toast.makeText(getApplicationContext(),
						msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
				finish();
				break;
			case MESSAGE_FROM_SELF:
				if (areMessagesBlocked)
					return;
				FightMessage selfMsg = (FightMessage) msg.obj;
				handleSelfMessage(selfMsg);
				break;
			case MESSAGE_SELF_DEATH:
				FightMessage selfDeath = new FightMessage(Target.ENEMY,
						FightAction.FIGHT_END);
				sendFightMessage(selfDeath);
				break;
			case MESSAGE_FROM_ENEMY:
				byte[] recvBytes = (byte[]) msg.obj;
				FightMessage enemyMsg = FightMessage.fromBytes(recvBytes);
				mEnemyGUI.log("enemy msg: " + enemyMsg + " " + (myCounter++));
				// if (D) Log.e(TAG, "enemy msg: " + enemyMsg + " " +
				// (myCounter));

				switch (enemyMsg.action) {
				case ENEMY_READY:
					isEnemyReady = true;
					if (D)
						Log.e(TAG, "self ready: " + isSelfReady
								+ ",enemy ready: " + isEnemyReady);
					if (!mChatService.isServer()) {
						return;
					}
					// if server: check whether we can start fight
					if (isSelfReady && isEnemyReady) {
						FightMessage startMsg = new FightMessage(Target.ENEMY,
								FightAction.FIGHT_START);
						sendFightMessage(startMsg);
						startFight();
					}
					break;
				case FIGHT_START:
					startFight();
					break;
				case FIGHT_END:
					finishFight(Target.SELF);
					break;
				default:
					if (areMessagesBlocked)
						return;
					handleEnemyMessage(enemyMsg);
				}
				break;
			case MESSAGE_MANA_REGEN:
				mSelfState.manaTick();
				mEnemyState.manaTick();
				mSelfGUI.getManaBar().setValue(mSelfState.getMana());
				mEnemyGUI.getManaBar().setValue(mEnemyState.getMana());
				Message msgManaReg = this.obtainMessage(
						AppMessage.MESSAGE_MANA_REGEN.ordinal(), 0, 0, null);
				this.sendMessageDelayed(msgManaReg, 2000);
				break;
			default:
				if (D)
					Log.e("Wizard Fight", "Unknown message");
				break;
			}
		}

		private void handleSelfMessage(FightMessage selfMsg) {
			Shape sendShape = FightMessage.getShapeFromMessage(selfMsg);
			if (sendShape != Shape.NONE) {
				isVolumeButtonBlocked = false;
			}
			mSelfGUI.log("self msg : " + selfMsg + " " + (myCounter++));
			if (D)
				Log.e(TAG, "self msg : " + selfMsg + " " + myCounter);
			// request mana for spell
			boolean canBeCasted = mSelfState.requestSpell(selfMsg);
			if (!canBeCasted) {
				return;
			}
			// play shape sound 
			mSensorAndSoundThread.playShapeSound(sendShape);
			
			mSelfGUI.getManaBar().setValue(mSelfState.mana);

			if (selfMsg.target == Target.SELF) {
				// self influence to self
				handleMessageToSelf(selfMsg);
			} else {
				// self influence to enemy
				// tell enemy : target is he
				selfMsg.target = Target.SELF;
				sendFightMessage(selfMsg);
			}
			// draw casted shape
			if (sendShape != Shape.NONE) {
				mSelfGUI.getSpellPicture().setShape(sendShape);
			}
		}

		private void handleEnemyMessage(FightMessage enemyMsg) {

			Shape recvShape = FightMessage.getShapeFromMessage(enemyMsg);

			// refresh enemy health and mana (every enemy message contains it)
			mEnemyState.setHealthAndMana(enemyMsg.health, enemyMsg.mana);
			mEnemyGUI.getPlayerName().setText(
					"enemy hp and mana: " + enemyMsg.health + ", "
							+ enemyMsg.mana);
			if (D)
				Log.e(TAG, "enemy msg: " + enemyMsg + " " + myCounter);
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
					mEnemyGUI.getBuffPanel()
							.addBuff(mEnemyState.getAddedBuff());
				}
			}

			// refresh enemy 
			if (FightMessage.isSpellCreatedByEnemy(enemyMsg)) {
				mEnemyGUI.getSpellPicture().setShape(recvShape);
			}
			mEnemyGUI.getHealthBar().setValue(mEnemyState.health);
			mEnemyGUI.getManaBar().setValue(mEnemyState.mana);
		}

		private void handleMessageToSelf(FightMessage fMessage) {
			FightMessage sendMsg;
			// Enemy influence to player
			mSelfState.handleSpell(fMessage);
			if (mSelfState.health == 0) {
				finishFight(Target.ENEMY);
				return;
			}

			Buff addedBuff = mSelfState.getAddedBuff();
			Buff removedBuff = mSelfState.getRemovedBuff();
			Buff refreshedBuff = mSelfState.getRefreshedBuff();
			Shape spellShape = mSelfState.getSpellShape();

			if (removedBuff != null) {
				// buff was removed after spell,
				// send message about buff loss to enemy
				sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_OFF,
						removedBuff.ordinal());
				sendFightMessage(sendMsg);
				// remove buff from panel
				mSelfGUI.getBuffPanel().removeBuff(removedBuff);
				if (mSelfState.isBuffRemovedByEnemy()) {
					mSensorAndSoundThread.playBuffSound(removedBuff);
				}
			}

			if (addedBuff != null) {
				// buff added to player after spell (for example
				// DoT, HoT, or shield),
				// send message about enemy buff success
				sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_ON,
						addedBuff.ordinal());
				sendFightMessage(sendMsg);
				// add buff to panel
				mSelfGUI.getBuffPanel().addBuff(addedBuff);
			}

			if (addedBuff != null || refreshedBuff != null) {
				// send message of the buff tick
				if (addedBuff != null)
					refreshedBuff = addedBuff;
				FightMessage fm = new FightMessage(Target.SELF,
						FightAction.BUFF_TICK, refreshedBuff.ordinal());
				Message buffTickMsg = this.obtainMessage(
						AppMessage.MESSAGE_FROM_SELF.ordinal(), fm);
				this.sendMessageDelayed(buffTickMsg,
						refreshedBuff.getDuration());
			}

			if (addedBuff == null && removedBuff == null) {
				// nothing with buffs => just send self hp and mana to enemy
				sendMsg = new FightMessage(Target.ENEMY,
						FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
				sendFightMessage(sendMsg);
			}

			mSelfGUI.getHealthBar().setValue(mSelfState.health);
			mSelfGUI.getManaBar().setValue(mSelfState.mana);
		}

	};

	private void finishFight(Target winner) {
		areMessagesBlocked = true;
		stopSensorAndSound();
		mSelfGUI.clear();
		mEnemyGUI.clear();
		setupApp();

		String message;
		if (winner == Target.SELF) {
			message = "You win!";
		} else {
			// we must inform enemy about loss
			mHandler.obtainMessage(AppMessage.MESSAGE_SELF_DEATH.ordinal())
					.sendToTarget();
			message = "You lose!";
		}

		AlertDialog alert = new AlertDialog.Builder(WizardFight.this).create();
		alert.setTitle("Fight ended");
		alert.setMessage(message);
		alert.setButton("Restart", endDialogListener);
		alert.setButton2("Exit", endDialogListener);
		alert.setCancelable(false);
		alert.show();
	}

	public void buttonClick() {
		if (isVolumeButtonBlocked)
			return;

		if (!isBetweenVolumeClicks) {
			mSensorAndSoundThread.startGettingData();
			isBetweenVolumeClicks = true;

		} else {
			isVolumeButtonBlocked = true;

			ArrayList<Vector3d> records = mSensorAndSoundThread.stopAndGetResult();
			isBetweenVolumeClicks = false;

			if (records.size() > 10) {
				new RecognitionThread(mHandler, records)
						.start();
			} else {
				// if shord record - don`t recognize & unblock
				isVolumeButtonBlocked = false;
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.e(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_START_FIGHT:
			// countdown finished
			isCountdown = false;
			areMessagesBlocked = false;
			break;
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if(mLastAction == action) {
				return true;
			}
			buttonClick();
			mLastAction = action;
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	class CancelButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			mClientWaitingDialog.dismiss();
			finish();
		}
	}

	class EndDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case -1:
				// send restart message
				isSelfReady = true;
				if (isEnemyReady || mIsEnemyBot) {
					FightMessage startMsg = new FightMessage(Target.ENEMY,
							FightAction.FIGHT_START);
					sendFightMessage(startMsg);
					startFight();
				} else {
					initWaitingDialog(R.string.client_waiting);
					FightMessage fightRequest = new FightMessage(Target.ENEMY,
							FightAction.ENEMY_READY);
					sendFightMessage(fightRequest);
				}
				break;
			case -2:
				// send exit message to enemy - ?
				finish();
				break;
			}
			if (D)
				Log.e(TAG, "self ready: " + isSelfReady);
		}
	}

	private void initBotSpellDialog() {
		mBotSpellDialog = new AlertDialog.Builder(this);
		mBotSpellDialog.setIcon(R.drawable.ic_launcher);
		mBotSpellDialog.setTitle("Enemy spell: ");
		mShapeNames = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_singlechoice);
		for (Shape s : Shape.values()) {
			if (s != Shape.NONE && s != Shape.FAIL)
				mShapeNames.add(s.toString());
		}

		mBotSpellDialog.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		mBotSpellDialog.setAdapter(mShapeNames,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String str = mShapeNames.getItem(which);
						FightMessage fMsg = new FightMessage(Shape
								.getShapeFromString(str));
						Message msg = mPlayerBot.getHandler().obtainMessage(
								AppMessage.MESSAGE_FROM_SELF.ordinal(), fMsg);
						mPlayerBot.getHandler().sendMessageDelayed(msg, 500);
					}
				});
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mBotSpellDialog.show();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}