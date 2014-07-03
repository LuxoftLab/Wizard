package com.example.wizard1.views;

import  com.example.wizard1.R;

import android.app.Activity;
import android.widget.TextView;

/*
 * Self GUI (locatated on the top)
 */
public class SelfGUI extends PlayerGUI {
	public SelfGUI(Activity a) {
		super(a);
		playerName = (TextView) a.findViewById(R.id.self_name);
		playerName.setText("new sound");
		healthBar = (HealthIndicator) a.findViewById(R.id.self_health);
		manaBar = (ManaIndicator) a.findViewById(R.id.self_mana);
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
