package com.wizardfight;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import com.wizardfight.fight.Buff;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightMessage;
import com.wizardfight.fight.FightMessage.Target;
import com.wizardfight.remote.WifiService;

public class BotFightCore extends FightCore {
	private final double mTimeToThink = 0.5;
	private final double mK;
	private Timer mTimer;
	private Shape shape;
	private Handler mUserHandler;

	public BotFightCore(Handler userHandler, double botSpeedCoeff) {
		mK = botSpeedCoeff;
		mUserHandler = userHandler;
	}

	@Override
	public void init() {
		super.init();
		mAreMessagesBlocked = false;
		if (mTimer != null)
			mTimer.cancel();
		mTimer = new Timer();
		shape = Shape.NONE;
	}

	@Override
	protected void startFight() {
		super.startFight();
		attack();
	}

	@Override
	protected void sendFightMessage(FightMessage msg) {
		msg.mHealth = mSelfState.getHealth();
		msg.mMana = mSelfState.getMana();
		msg.mIsBotMessage = true;

		// send to pc if connected
		WifiService.send(msg); //TODO not good place for this

		byte[] sendBytes = msg.getBytes();
		if(mUserHandler != null) {
			mUserHandler.obtainMessage(HandlerMessage.HM_FROM_ENEMY.ordinal(),
					sendBytes).sendToTarget();
		}		
	}

	@Override
	protected void finishFight(Target winner) {
		super.finishFight(winner);
		init();
	}

	@Override
	public void release() {
		mTimer.cancel();
		mTimer = null;
		shape = null;
		mUserHandler = null;
		super.release();
	}

	private void attack() {
		if (shape != Shape.NONE) {
			FightMessage selfMsg = new FightMessage(shape);
			boolean canBeCasted = mSelfState.requestSpell(selfMsg);
			if (canBeCasted) {
				if (selfMsg.mTarget == Target.SELF) {
					onMessageToSelf(selfMsg);
				} else {
					selfMsg.mTarget = Target.SELF;
					sendFightMessage(selfMsg);
				}
			}
		}
		shape = Shape.NONE;
		if ((!mSelfState.hasBuff(Buff.CONCENTRATION))
				&& (canSpell(Shape.V, Shape.CIRCLE) && (getEnemyState()
						.getHealth() > 30))) {
			shape = Shape.V;
		} else {
			if ((mEnemyState.hasBuff(Buff.HOLY_SHIELD)) && (canSpell(Shape.Z))) {
				shape = Shape.Z;
			} else {
				if (canSpell(Shape.CIRCLE))
					shape = Shape.CIRCLE;
				else if ((canSpell(Shape.TRIANGLE))
						&& (mEnemyState.getHealth() < 10))
					shape = Shape.TRIANGLE;
			}
		}
		if ((mSelfState.getHealth() < getEnemyState().getHealth())
				&& (shape != Shape.TRIANGLE)
				&& (!mSelfState.hasBuff(Buff.HOLY_SHIELD))
				&& (!mSelfState.hasBuff(Buff.CONCENTRATION))
				&& (canSpell(Shape.SHIELD))) {
			shape = Shape.SHIELD;
		}
		if ((shape != Shape.TRIANGLE) && (mSelfState.getHealth() < 40)) {
			shape = Shape.CLOCK;
		}
		try {
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					attack();
				}

			}, (int) ((shape.getCastTime() + mTimeToThink) * 1000 * mK));
		} catch (IllegalStateException e) {
		}
	}

	private boolean canSpell(Shape... shapes) {
		int manaCost = 0;
		for (Shape shape : shapes) {
			manaCost += shape.getManaCost();
		}
		return (manaCost < mSelfState.getMana());
	}
}
