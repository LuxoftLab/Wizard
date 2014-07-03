package com.example.wizard1.views;

import android.app.Activity;
import android.widget.TextView;

/*
 * Player GUI: name + mana bar + health bar 
 *            + spell picture + buff panel
 */
public class PlayerGUI {
	protected TextView playerName;
	protected Activity mainActivity;
	protected HealthIndicator healthBar;
	protected ManaIndicator manaBar;
	protected SpellPicture spellPicture;
	protected BuffPanel buffPanel;
	
	public PlayerGUI(Activity a) {
		mainActivity = a;
	}
	
	public HealthIndicator getHealthBar() { return healthBar; }
	public ManaIndicator getManaBar() { return manaBar; }
	public SpellPicture getSpellPicture() { return spellPicture; }
	public BuffPanel getBuffPanel() { return buffPanel; }
//	public BuffPicture[] getBuffs() { return buffPanel.getBuffs(); }
	public TextView getPlayerName() { return playerName; }
}
