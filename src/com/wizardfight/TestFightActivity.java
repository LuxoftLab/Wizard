package com.wizardfight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

public class TestFightActivity extends FightActivity {
    private PlayerBot mPlayerBot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerBot = new PlayerBot(PLAYER_HP, PLAYER_MANA, mHandler);
        mPlayerBot.start();
        mFightEndDialog = new TestFightEndDialog();
        Log.e("testFAThread", Thread.currentThread().getName());
        startFight();
    }

    @Override
    protected void setupApp() {
        super.setupApp();
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
        }, 4500);//TODO Fix fast exit
        super.startFight();

    }
    @Override
    protected void sendFightMessage(FightMessage fMessage) {
        super.sendFightMessage(fMessage);
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
}
