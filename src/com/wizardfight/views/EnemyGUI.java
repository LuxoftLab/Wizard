package com.wizardfight.views;

import  com.wizardfight.R;

import android.app.Activity;
import android.widget.TextView;

/*
 * Enemy player GUI (locatated on the bottom)
 */
public class EnemyGUI extends PlayerGUI {
	public EnemyGUI(Activity a, int hp, int mana) {
		super(a);
		playerName = (TextView) a.findViewById(R.id.enemy_name);
		debugField = (TextView) a.findViewById(R.id.enemy_debug);
		healthBar = (HealthIndicator) a.findViewById(R.id.enemy_health);
		healthBar.setMaxValue(hp);
		manaBar = (ManaIndicator) a.findViewById(R.id.enemy_mana);
		manaBar.setMaxValue(mana);
		spellPicture = (SpellPicture) a.findViewById(R.id.enemy_spell);
		BuffPicture[] buffs = new BuffPicture[5];
		buffs[0] = (BuffPicture)a.findViewById(R.id.enemy_buff1);
		buffs[1] = (BuffPicture)a.findViewById(R.id.enemy_buff2);
		buffs[2] = (BuffPicture)a.findViewById(R.id.enemy_buff3);
		buffs[3] = (BuffPicture)a.findViewById(R.id.enemy_buff4);
		buffs[4] = (BuffPicture)a.findViewById(R.id.enemy_buff5);
		buffPanel = new BuffPanel(buffs);
	}
}