package com.wizardfight;

import android.view.*;

import com.wizardfight.remote.WifiService;
import com.wizardfight.views.*;
import com.wizardfight.FightMessage.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;

/**
 * Bluetooth fight activity. Extends Fight Activity with 
 * sending messages via Bluetooth, waiting second player,
 * receiving messages about Bluetooth state
 */
public class BtFightActivity extends FightActivity {
	private BluetoothService mBtService = null;
	private boolean mIsEnemyReady;
	private boolean mIsSelfReady;
	private Dialog mClientWaitingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIsEnemyReady = false;
		mIsSelfReady = true;
		
		mBtService = BluetoothService.getInstance();
		mBtService.setHandler(mFightCore);
		
		// Start listening clients if server
		if (mBtService.isServer()) {
			mBtService.start();
			initWaitingDialog(R.string.client_waiting);
		} else {
			initWaitingDialog(R.string.trying_to_connect);
			FightMessage fightRequest = new FightMessage(
					Target.ENEMY, FightAction.ENEMY_READY);
			sendFightMessage(fightRequest);
		}
		mFightEndDialog = new BtFightEndDialog();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBtService != null) {
			mBtService.stop();
			mBtService = null;
		}
	}
	
	@Override
	protected void startFight() {
		if (mClientWaitingDialog != null) {
			mClientWaitingDialog.dismiss();
			mClientWaitingDialog = null;
		}
		super.startFight();
		mIsEnemyReady = false;
		mIsSelfReady = false;
	}

	@Override 
	protected void handleEnemyReadyMessage() {
		mIsEnemyReady = true;
		if (D)
			Log.e(TAG, "self ready: " + mIsSelfReady
					+ ",enemy ready: " + mIsEnemyReady);
		if (!mBtService.isServer()) {
			return;
		}
		// if server: check whether we can start fight
		if (mIsSelfReady) {
			FightMessage startMsg = new FightMessage(Target.ENEMY,
					FightAction.FIGHT_START);
			sendFightMessage(startMsg);
			startFight();
		}
	}
	
	@Override
	protected void sendFightMessage(FightMessage fMessage) {
		super.sendFightMessage(fMessage);
		
		if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}

		byte[] send = fMessage.getBytes();
		// send to 2nd phone
		mBtService.write(send);
		// send to pc if connected
		WifiService.send(fMessage);
	}
	
	private void initWaitingDialog(int stringId) {
		View v = getLayoutInflater().inflate(R.layout.client_waiting, null);
		mClientWaitingDialog = new Dialog(this, R.style.WDialog);
		mClientWaitingDialog.setTitle(stringId);
		mClientWaitingDialog.setCancelable(false);
		RectButton cancel = (RectButton) v
				.findViewById(R.id.button_cancel_waiting);
		cancel.setOnClickListener(new CancelButtonListener());
		mClientWaitingDialog.setContentView(v);
		if (D) Log.e(TAG, "Show waiting dialog!");
		mClientWaitingDialog.setOnKeyListener(new Dialog.OnKeyListener() {
			boolean mmCancelled = false;
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && !mmCancelled) {
					mClientWaitingDialog.dismiss();
					mClientWaitingDialog = null;
					finish();
					mmCancelled = true;
				}
				return true;
			}
		});
	}

	private class CancelButtonListener implements OnClickListener {
		boolean mmCancelled = false;
		@Override
		public void onClick(View v) {
			if(!mmCancelled) {
				mClientWaitingDialog.dismiss();
				mClientWaitingDialog = null;
				finish();
				mmCancelled = true;
			}
		}
	}
	
	class BtFightEndDialog extends FightEndDialog {
		@Override
		public void init(String message) {
			mmDialog = new AlertDialog.Builder(BtFightActivity.this).create();
			super.init(message);
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case -1:
				if (D) Log.e(TAG, "CLICK RESTART");
				// send restart message
				mIsSelfReady = true;
				if (mIsEnemyReady) {
					FightMessage startMsg = new FightMessage(Target.ENEMY,
							FightAction.FIGHT_START);
					sendFightMessage(startMsg);
					startFight();
				} else {
					initWaitingDialog(R.string.client_waiting);
					FightMessage fightRequest = new FightMessage(
							Target.ENEMY, FightAction.ENEMY_READY);
					sendFightMessage(fightRequest);
				}
				break;
			case -2:
				finish();
				break;
			}
			mmIsNeedToShow = false;
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(mClientWaitingDialog != null) {
			mClientWaitingDialog.show();
		}
	}

	@Override
	void onBluetoothStateChange(int state) {
		if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + state);
        switch (state) {
            case BluetoothService.STATE_CONNECTED:
                startFight();
                break;
            case BluetoothService.STATE_NONE:
                break;
        }
	}
}