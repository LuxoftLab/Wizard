package com.wizardfight.views;

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

	
	public void clear() {
		mBuffPanel.removeBuffs();
		mSpellPicture.setShape(Shape.NONE);
	}
	
	public HealthIndicator getHealthBar() { return mHealthBar; }
	public ManaIndicator getManaBar() { return mManaBar; }
	public SpellPicture getSpellPicture() { return mSpellPicture; }
	public BuffPanel getBuffPanel() { return mBuffPanel; }
}
