package com.wizardfight.fight;

import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wizardfight.CastActivity;
import com.wizardfight.Sound;
import com.wizardfight.R;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore.CoreAction;
import com.wizardfight.fight.FightMessage.*;
import com.wizardfight.remote.WifiMessage;
import com.wizardfight.remote.WifiService;
import com.wizardfight.views.BuffPanel;
import com.wizardfight.views.BuffPicture;
import com.wizardfight.views.FightBackground;
import com.wizardfight.views.PlayerGUI;

/**
 * Extends CastActivity for two players fighting. Adds two player states with
 * current hp, mana, activated buffs... Handles players messages (implements
 * fight logic) Adds countdown at start and dialog for restart.
 */
public abstract class FightActivity extends CastActivity implements Observer {

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final int PLAYER_HP = 200;
	public static final int PLAYER_MANA = 100;

	protected FightCore mCore;
	// private Dialog mNetDialog;
	protected FightEndDialog mFightEndDialog;
	// is activity running
	private boolean mIsRunning;
	private boolean mAreMessagesBlocked;
	// Layout Views
	private Countdown mCountdown;
	private PlayerGUI mSelfGUI;
	private PlayerGUI mEnemyGUI;
	// test mode dialog with spell names
	private FightBackground mBgImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// WARNING: fight core must be initialized ONLY before super.onCreate!
		mCore = new FightCore();
		mCore.addObserver(this);
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "--- FightActivity ON CREATE ---");
		setContentView(R.layout.fight);

		// add countdown view to the top
		LayoutInflater inflater = getLayoutInflater();
		View countdownView = inflater.inflate(R.layout.countdown, null);
		mCountdown = new Countdown(this, countdownView, mHandler);
		getWindow().addContentView(
				countdownView,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT));

		// Init on touch listener
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.fight_layout_root);
		rootLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mAreMessagesBlocked || mIsCastAbilityBlocked)
					return true;
				int action = event.getAction();
				if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_DOWN) {
					if (mLastTouchAction == action) {
						return true;
					}
					if (action == MotionEvent.ACTION_DOWN) {
						mBgImage.toBright();
					} else {
						mBgImage.toDark();
					}
					buttonClick();
					mLastTouchAction = action;
				}
				return false;
			}
		});
		// Initialize GUI and logic
		setupApp();
		// Initialize end dialog object
		mBgImage = (FightBackground) findViewById(R.id.fight_background);
	}

	@Override
	public void onResume() {
		if (D)
			Log.e(TAG, "--- FIGHTACTIVITY ON RESUME ---");
		mIsRunning = true;
		super.onResume();
		mBgImage.darkenImage();

		if (mFightEndDialog.isNeedToShow()) {
			mFightEndDialog.show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsRunning = false;
	}

	@Override
	public void onDestroy() {
		if (D)
			Log.e(TAG, "--- FIGHT ACTIVITY ON DESTROY ---");
		super.onDestroy();
		// remove all messages from handler
		WifiService.send(WifiMessage.LEAVE_FIGHT);
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	protected Handler getHandler() {
		return mCore.getHandler();
	}

	@Override
	public void update(Observable o, Object action) {
		CoreAction cm = (CoreAction) action;
		switch (cm) {
		case CM_BT_STATE_CHANGE:
			onBluetoothStateChange(mCore.getData().getBluetoothState());
			break;
		case CM_CONNECTION_FAIL:
			Toast.makeText(getApplicationContext(),
					mCore.getData().getFailStringId(), Toast.LENGTH_SHORT)
					.show();
			mAreMessagesBlocked = true;
			finish();
			break;
		case CM_COUNTDOWN_END:
			mAreMessagesBlocked = false;
			break;
		case CM_DEVICE_NAME:
			String deviceName = mCore.getData().getDeviceName();
			Toast.makeText(getApplicationContext(),
					getString(R.string.connected_to) + deviceName,
					Toast.LENGTH_SHORT).show();
			break;
		case CM_ENEMY_CAST:
			mEnemyGUI.getSpellPicture().setShape(
					mCore.getData().getEnemyShape());
			break;
		case CM_ENEMY_HEALTH_MANA:
			mEnemyGUI.getHealthBar()
					.setValue(mCore.getEnemyState().getHealth());
			mEnemyGUI.getManaBar().setValue(mCore.getEnemyState().getMana());
			break;
		case CM_ENEMY_NEW_BUFF:
			mEnemyGUI.getBuffPanel().addBuff(
					mCore.getEnemyState().getAddedBuff());
			break;
		case CM_ENEMY_READY:
			handleEnemyReadyMessage();
			break;
		case CM_ENEMY_REMOVED_BUFF:
			mEnemyGUI.getBuffPanel().removeBuff(
					mCore.getEnemyState().getRemovedBuff());
			break;
		case CM_FIGHT_END:
			finishFight(mCore.getData().getWinner());
			break;
		case CM_FIGHT_START:
			startFight();
			break;
		case CM_HEALTH_CHANGED:
			mSelfGUI.getHealthBar().setValue(mCore.getSelfState().getHealth());
			break;
		case CM_INFO_STRING:
			Toast.makeText(getApplicationContext(),
					mCore.getData().getInfoString(), Toast.LENGTH_SHORT).show();
			break;
		case CM_MANA_CHANGED:
			mSelfGUI.getManaBar().setValue(mCore.getSelfState().getMana());
			break;
		case CM_MESSAGE_TO_SEND:
			sendFightMessage(mCore.getData().getMessageToSend());
			break;
		case CM_NEW_BUFF:
			mSelfGUI.getBuffPanel()
					.addBuff(mCore.getSelfState().getAddedBuff());
			break;
		case CM_REMOVED_BUFF:
			mSelfGUI.getBuffPanel().removeBuff(
					mCore.getSelfState().getRemovedBuff());
			break;
		case CM_SELF_CAST_NOMANA:
			onSelfCastNoMana();
			break;
		case CM_SELF_CAST_SUCCESS:
			onSelfCastSuccess();
			break;
		default:
			break;
		}
	}

	void setupApp() {
		// Players logic
		mCore.init();
		// Initialize players UI
		findViewById(R.id.self_health);
		mSelfGUI = new PlayerGUI(findViewById(R.id.self_health),
				findViewById(R.id.self_mana), findViewById(R.id.self_spell),
				findViewById(R.id.imageView), new BuffPanel(
						(BuffPicture) findViewById(R.id.self_buff1),
						(BuffPicture) findViewById(R.id.self_buff2),
						(BuffPicture) findViewById(R.id.self_buff3),
						(BuffPicture) findViewById(R.id.self_buff4),
						(BuffPicture) findViewById(R.id.self_buff5)));
		mEnemyGUI = new PlayerGUI(findViewById(R.id.enemy_health),
				findViewById(R.id.enemy_mana), findViewById(R.id.enemy_spell),
				findViewById(R.id.imageView2), new BuffPanel(
						(BuffPicture) findViewById(R.id.enemy_buff1),
						(BuffPicture) findViewById(R.id.enemy_buff2),
						(BuffPicture) findViewById(R.id.enemy_buff3),
						(BuffPicture) findViewById(R.id.enemy_buff4),
						(BuffPicture) findViewById(R.id.enemy_buff5)));
		// Drop flags
		mAreMessagesBlocked = true; 
		// Last touch value
		mLastTouchAction = MotionEvent.ACTION_UP;
	}

	void onSelfCastSuccess() {
		Shape shape = mCore.getData().getSelfShape();
		mIsCastAbilityBlocked = false;
		Sound.playShapeSound(shape); // TODO move to other place?

		mSelfGUI.getSpellPicture().setShape(shape);
	}

	void onSelfCastNoMana() {
		Shape shape = mCore.getData().getSelfShape();
		if (shape != Shape.NONE) {
			mIsCastAbilityBlocked = false;
		}
		Sound.playNoManaSound(); // TODO move to other place?
	}

	abstract public void sendFightMessage(FightMessage fMessage);

	/*
	 * this method runs when FightCore iniates game start
	 * the purpose is to setup GUI / controller
	 */
	protected void startFight() {
		mCountdown.startCountdown();
	}
	
	/*
	 * the method runs when FightActivity initiates game start
	 * the purpose is to setup game logic and then init GUI / controller
	 */
	protected final void initStart() {
		mCore.startFight();
	}
	
	public abstract void handleEnemyReadyMessage();

	public abstract void onBluetoothStateChange(int state);

	private void finishFight(Target winner) {
		if (D)
			Log.e(TAG, "FINISH FIGHT");
		mAreMessagesBlocked = true;

		stopSensorAndSound();
		startNewSensorAndSound();
		// set GUI to initial state
		mBgImage.darkenImage();
		mSelfGUI.clear();
		mEnemyGUI.clear();
		// Recreate objects
		setupApp();

		String message = (winner == Target.SELF) ? getString(R.string.you_win)
				: getString(R.string.you_lose);

		mFightEndDialog.init(message);
		// consider the dialog call while activity is not running
		if (mIsRunning) {
			mFightEndDialog.show();
		} else {
			mFightEndDialog.setNeedToShow(true);
		}
	}

	public abstract class FightEndDialog implements DialogInterface.OnClickListener {
		protected AlertDialog mmDialog;
		protected boolean mmIsNeedToShow;

		public void init(String message) {
			if (D)
				Log.e(TAG, "INIT FIGHT END DIALOG");
			mmIsNeedToShow = false;
			mmDialog.setTitle(getString(R.string.fight_ended));
			mmDialog.setMessage(message);
			mmDialog.setButton(getString(R.string.restart), this);
			mmDialog.setButton2(getString(R.string.exit), this);
			mmDialog.setCancelable(false);
		}

		public boolean isNeedToShow() {
			return mmIsNeedToShow;
		}

		public void setNeedToShow(boolean isNeed) {
			mmIsNeedToShow = isNeed;
		}

		public void show() {
			mmDialog.show();
		}

		@Override
		abstract public void onClick(DialogInterface dialog, int which);
	}
}