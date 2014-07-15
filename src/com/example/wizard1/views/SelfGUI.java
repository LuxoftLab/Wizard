package com.example.wizard1.views;

import  com.example.wizard1.R;

import android.app.Activity;
import android.widget.TextView;

/*
 * Self GUI (locatated on the top)
 */
public class SelfGUI extends PlayerGUI {
	public SelfGUI(Activity a, int hp, int mana) {
		super(a);
		playerName = (TextView) a.findViewById(R.id.self_name);
		playerName.setText("Fix GUI #1");
		debugField = (TextView) a.findViewById(R.id.self_debug);
		healthBar = (HealthIndicator) a.findViewById(R.id.self_health);
		healthBar.setMaxValue(hp);
		manaBar = (ManaIndicator) a.findViewById(R.id.self_mana);
		manaBar.setMaxValue(mana);
		spellPicture = (SpellPicture) a.findViewById(R.id.self_spell);
		spellPicture.initAnimListener();
		BuffPicture[] buffs = new BuffPicture[5];
		buffs[0] = (BuffPicture)a.findViewById(R.id.self_buff1);
		buffs[1] = (BuffPicture)a.findViewById(R.id.self_buff2);
		buffs[2] = (BuffPicture)a.findViewById(R.id.self_buff3);
		buffs[3] = (BuffPicture)a.findViewById(R.id.self_buff4);
		buffs[4] = (BuffPicture)a.findViewById(R.id.self_buff5);
		buffPanel = new BuffPanel(buffs);
	}
}
