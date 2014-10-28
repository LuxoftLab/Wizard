package com.wizardfight;

import android.view.*;

import android.widget.LinearLayout;
import com.wizardfight.views.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
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
		mIsSelfReady = false;
		// Start listening clients if server
		if (mBtService.isServer()) {
			mBtService.start();
			initWaitingDialog(R.string.client_waiting);
		} else {
			initWaitingDialog(R.string.trying_to_connect);
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
		if (mIsSelfReady && mIsEnemyReady) {
			FightMessage startMsg = new FightMessage(Target.ENEMY,
					FightAction.FIGHT_START);
			sendFightMessage(startMsg);
			startFight();
		}
	}
	
	@Override
	protected void setupApp() {
		// Initialize the BluetoothChatService to BT connections
		mBtService = BluetoothService.getInstance();
		mBtService.setHandler(mHandler);
		super.setupApp();
	}

	@Override
	protected void sendFightMessage(FightMessage fMessage) {
		super.sendFightMessage(fMessage);
		
		// Check that we're actually connected before trying anything
		if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(getApplicationContext(), R.string.not_connected,
					Toast.LENGTH_SHORT).show();
			return;
		}

		byte[] send = fMessage.getBytes();
		mBtService.write(send);
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

	private class CancelButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			mClientWaitingDialog.dismiss();
			finish();
		}
	}
	
	class BtFightEndDialog extends FightEndDialog {
		@Override
		public void init(String message) {
			Log.e(TAG, "INIT BT FIGHT");
			mmDialog = new AlertDialog.Builder(BtFightActivity.this).create();
			super.init(message);
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case -1:
				Log.e(TAG, "CLICK RESTART");
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
				Log.e(TAG, "CLICK EXIT");
				finish();
				break;
			}
			mmIsNeedToShow = false;
		}
	}
}