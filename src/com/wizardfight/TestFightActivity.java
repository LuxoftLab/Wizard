package com.wizardfight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

public class TestFightActivity extends FightActivity {
    private PlayerBot mPlayerBot;
    // test mode dialog with spell names
    private ArrayAdapter<String> mShapeNames;
    private AlertDialog.Builder mBotSpellDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBotSpellDialog();
        mFightEndDialog = new TestFightEndDialog();
        startFight();
    }

    @Override
    protected void setupApp() {
        if (mPlayerBot != null)
            mPlayerBot.release();
        mPlayerBot = new PlayerBot(PLAYER_HP, PLAYER_MANA, mHandler);
        super.setupApp();
    }

    @Override
    protected void sendFightMessage(FightMessage fMessage) {
        super.sendFightMessage(fMessage);

        if (mPlayerBot.getHandler() == null)
            return;
        Message msg = mPlayerBot.getHandler().obtainMessage(
                AppMessage.MESSAGE_FROM_ENEMY.ordinal(), fMessage);
        msg.sendToTarget();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove all messages from handler
        mHandler.removeCallbacksAndMessages(null);
        mPlayerBot.release();
        mPlayerBot = null;
        Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mBotSpellDialog.show();
            return true;
        }
        return super.onKeyUp(keyCode, event);
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
            // TODO Auto-generated method stub
            switch (which) {
                case -1:
                    // send restart message
                    FightMessage startMsg = new FightMessage(Target.ENEMY,
                            FightAction.FIGHT_START);
                    sendFightMessage(startMsg);
                    startFight();
                    break;
                case -2:
                    finish();
                    break;
            }
            mmIsNeedToShow = false;
        }
    }
}
