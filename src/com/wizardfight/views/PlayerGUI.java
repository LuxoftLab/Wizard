package com.wizardfight.views;

import com.wizardfight.Shape;

import android.app.Activity;
import android.widget.TextView;

/*
 * Player GUI: name + mana bar + health bar 
 *            + spell picture + buff panel
 */
public class PlayerGUI {
	protected Activity mainActivity;
	protected HealthIndicator healthBar;
	protected ManaIndicator manaBar;
	protected SpellPicture spellPicture;
	protected BuffPanel buffPanel;
	
	public PlayerGUI(Activity a) {
		mainActivity = a;
	}
	
	public void clear() {
		buffPanel.removeBuffs();
		spellPicture.setShape(Shape.NONE);
	}
	
	public HealthIndicator getHealthBar() { return healthBar; }
	public ManaIndicator getManaBar() { return manaBar; }
	public SpellPicture getSpellPicture() { return spellPicture; }
	public BuffPanel getBuffPanel() { return buffPanel; }
}
