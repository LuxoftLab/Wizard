package com.wizardfight;

import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.wizardfight.FightMessage.*;
import com.wizardfight.remote.WifiService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.wizardfight.views.RectButton;

/*
 * Performs fighting with bot
 */
public class TestFightActivity extends FightActivity {
    private PlayerBot mPlayerBot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerBot = new PlayerBot(PLAYER_HP, PLAYER_MANA, mHandler);
        mPlayerBot.start();
        mFightEndDialog = new TestFightEndDialog();
        Log.e("testFAThread", Thread.currentThread().getName());
        setupBot();
    }

    private Dialog mDificultyDialog;
    void setupBot(){
        final View v = getLayoutInflater().inflate(R.layout.bot_setup, null);
        mDificultyDialog = new Dialog(this, R.style.WDialog);
        mDificultyDialog.setTitle(R.string.title_bot);
        RectButton cancel = (RectButton) v
                .findViewById(R.id.button_cancel_waiting);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDialClose();
            }
        });
        RadioGroup complexity = (RadioGroup) v
                .findViewById(R.id.radio_complexity);
        complexity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb=(RadioButton) v.findViewById(i);
                onComSetup(Double.parseDouble(rb.getHint().toString()));

            }
        });
        mDificultyDialog.setContentView(v);
        mDificultyDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onDialClose();
                }
                return true;
            }
        });
        mDificultyDialog.show();
    }
    void onComSetup(double k){
        mPlayerBot.setK(k);
        startFight();
        mDificultyDialog.dismiss();
        mDificultyDialog = null;
    }
    void onDialClose(){
        mDificultyDialog.dismiss();
        mDificultyDialog = null;
        finish();
    }
    @Override
    protected void startFight() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FightMessage startMsg = new FightMessage(Target.ENEMY,
                        FightAction.FIGHT_START);
                sendFightMessage(startMsg);

            }
        }, 4500);
        super.startFight();

    }
    
    @Override
    protected void sendFightMessage(FightMessage fMessage) {
    	if(mPlayerBot == null) return; // if user exists at start
        super.sendFightMessage(fMessage);
        
        // send to pc if connected
        WifiService.send(fMessage);
        
        // send to bot
        Message msg = mPlayerBot.getHandler().obtainMessage(
                AppMessage.MESSAGE_FROM_ENEMY.ordinal(), fMessage);
        msg.sendToTarget();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove all messages from handler
        mHandler.removeCallbacksAndMessages(null);
        mPlayerBot.release();
        mPlayerBot = null;
        Log.e(TAG, "--- ON DESTROY ---");
    }

    class TestFightEndDialog extends FightEndDialog {
        @Override
        public void init(String message) {
            Log.e(TAG, "INIT TEST FIGHT");
            mmDialog = new AlertDialog.Builder(TestFightActivity.this).create();
            super.init(message);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    // send restart message
                    startFight();
                    break;
                case -2:
                    finish();
                    break;
            }
            mmIsNeedToShow = false;
        }
    }

    //dont care in case with bot
	@Override
	void handleEnemyReadyMessage() {}
	@Override
	void onBluetoothStateChange(int state) {
		//do nothing
	}
}
