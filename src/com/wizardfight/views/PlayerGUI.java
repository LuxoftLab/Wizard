package com.wizardfight.views;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.wizardfight.Shape;

/*
 * Player GUI: name + mana bar + health bar 
 *            + spell picture + buff panel
 */
public class PlayerGUI {
	HealthIndicator mHealthBar;
	ManaIndicator mManaBar;
	SpellPicture mSpellPicture;
	BuffPanel mBuffPanel;
	ImageView indicator;
	//
	public PlayerGUI(View hpId, View manaId, View spellId, View indicatorId,
					 BuffPanel bf) {
		mHealthBar = (HealthIndicator)hpId;
		mHealthBar.setMaxValue(com.wizardfight.fight.FightActivity.PLAYER_HP);
		mManaBar = (ManaIndicator)manaId;
		mManaBar.setMaxValue(com.wizardfight.fight.FightActivity.PLAYER_MANA);
		mSpellPicture = (SpellPicture)spellId;
		indicator = (ImageView)indicatorId;
		mBuffPanel = bf;
		ViewTreeObserver vto = indicator.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = indicator.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
				resize(indicator.getWidth(),indicator.getHeight());
			}

		});
	}
	public void resize(int w,int h){
		int barW = w*61/96, barH = h/5;
		int barMarginW = w*8/1000, hpMarginH = h*3/20, mpMarginH = h*36/100;
		int picSize = w*37/100, picMarginH = w*14/1000;
		int buffSize = h*2/10, buffMargin = 5;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(barW, barH);
		params.addRule(RelativeLayout.ALIGN_TOP, indicator.getId());
		params.addRule(RelativeLayout.ALIGN_LEFT, indicator.getId());
		params.setMargins(barMarginW, hpMarginH, 0, 0);
		mHealthBar.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams(barW, barH);
		params.addRule(RelativeLayout.ALIGN_TOP, indicator.getId());
		params.addRule(RelativeLayout.ALIGN_LEFT, indicator.getId());
		params.setMargins(barMarginW, mpMarginH, 0, 0);
		mManaBar.setLayoutParams(params);


		if(mSpellPicture!=null) {
			params = new RelativeLayout.LayoutParams(picSize, picSize);
			params.addRule(RelativeLayout.ALIGN_TOP, indicator.getId());
			params.addRule(RelativeLayout.ALIGN_RIGHT, indicator.getId());
			params.setMargins(0, picMarginH, picMarginH, 0);
			mSpellPicture.setLayoutParams(params);
		}

		if (mBuffPanel != null) {
			LinearLayout.LayoutParams params1;
			for (View buff : mBuffPanel.mBuffPics) {
				params1 = new LinearLayout.LayoutParams(buffSize, buffSize);
				params1.setMargins(buffMargin, buffMargin, buffMargin, buffMargin);
				buff.setLayoutParams(params1);

			}
		}
	}

	
	public void clear() {
		mBuffPanel.removeBuffs();
		mSpellPicture.setShape(Shape.NONE);
	}
	
	public HealthIndicator getHealthBar() { return mHealthBar; }
	public ManaIndicator getManaBar() { return mManaBar; }
	public SpellPicture getSpellPicture() { return mSpellPicture; }
	public BuffPanel getBuffPanel() { return mBuffPanel; }
}
