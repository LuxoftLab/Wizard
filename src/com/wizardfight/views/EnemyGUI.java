package com.wizardfight.views;

import  com.wizardfight.R;

import android.app.Activity;

/*
 * Enemy player GUI (located on the bottom)
 */
public class EnemyGUI extends PlayerGUI {
	public EnemyGUI(Activity a, int hp, int mana) {
		super();
		mHealthBar = (HealthIndicator) a.findViewById(R.id.enemy_health);
		mHealthBar.setMaxValue(hp);
		mManaBar = (ManaIndicator) a.findViewById(R.id.enemy_mana);
		mManaBar.setMaxValue(mana);
		mSpellPicture = (SpellPicture) a.findViewById(R.id.enemy_spell);
		BuffPicture[] buffs = new BuffPicture[5];
		buffs[0] = (BuffPicture)a.findViewById(R.id.enemy_buff1);
		buffs[1] = (BuffPicture)a.findViewById(R.id.enemy_buff2);
		buffs[2] = (BuffPicture)a.findViewById(R.id.enemy_buff3);
		buffs[3] = (BuffPicture)a.findViewById(R.id.enemy_buff4);
		buffs[4] = (BuffPicture)a.findViewById(R.id.enemy_buff5);
		mBuffPanel = new BuffPanel(buffs);
	}
}