package com.wizardfight.views;

import com.wizardfight.Shape;

/*
 * Player GUI: name + mana bar + health bar 
 *            + spell picture + buff panel
 */
public class PlayerGUI {
	HealthIndicator healthBar;
	ManaIndicator manaBar;
	SpellPicture spellPicture;
	BuffPanel buffPanel;

	
	public void clear() {
		buffPanel.removeBuffs();
		spellPicture.setShape(Shape.NONE);
	}
	
	public HealthIndicator getHealthBar() { return healthBar; }
	public ManaIndicator getManaBar() { return manaBar; }
	public SpellPicture getSpellPicture() { return spellPicture; }
	public BuffPanel getBuffPanel() { return buffPanel; }
}
