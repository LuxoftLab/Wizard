package com.example.wizard1.views;

import com.example.wizard1.Buff;
import com.example.wizard1.FightMessage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/*
 * Class that contains links to player`s buff views
 */
public class BuffPanel {
	private BuffPicture[] buffPics;
	public BuffPanel(BuffPicture[] buffPictures) {
		buffPics = buffPictures;
	}
	
	public void addBuff(Buff buff) {
		for(int i=0; i<buffPics.length; i++) {
			Buff b = buffPics[i].getBuff();
			// if exists already
			if( b == buff ) {
				return;
			}
			if( b == Buff.NONE ) {
				buffPics[i].setBuff(buff);
				return;
			}
		}
	}
	
	public void removeBuff(Buff buff) {
		for(int i=0; i<buffPics.length; i++) {
			if(buffPics[i].getBuff() == buff) {
				buffPics[i].setBuff(Buff.NONE);
			}
		}
	}
	
//	public BuffPicture[] getBuffs() { return buffs; }
}
