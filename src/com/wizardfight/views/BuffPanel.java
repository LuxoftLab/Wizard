package com.wizardfight.views;

import com.wizardfight.Buff;

/*
 * Class that manages buff views
 */
public class BuffPanel {
	private final BuffPicture[] mBuffPics;
	public BuffPanel(BuffPicture[] buffPictures) {
		mBuffPics = buffPictures;
	}
	
	public void addBuff(Buff buff) {
        for (BuffPicture buffPic : mBuffPics) {
            Buff b = buffPic.getBuff();
            // if exists already
            if (b == buff) {
                return;
            }
            if (b == Buff.NONE) {
                buffPic.setBuff(buff);
                return;
            }
        }
	}
	
	public void removeBuff(Buff buff) {
		int rmIndex = -1;
		for(int i=0; i<mBuffPics.length; i++) {
			if(mBuffPics[i].getBuff() == buff) {
				rmIndex = i;
				break;
			}
		}
		if(rmIndex == -1) return;
		
		for(int i=rmIndex; i<mBuffPics.length-1; i++) {
			if(mBuffPics[i].getBuff() == Buff.NONE) break;
			
			Buff rightBuff = mBuffPics[i+1].getBuff();
			mBuffPics[i].setBuff(rightBuff);
		}
	}
	
	public void removeBuffs() {
        for (BuffPicture buffPic : mBuffPics) {
            buffPic.setBuff(Buff.NONE);
        }
	}
}
