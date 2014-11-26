package com.wizardfight.views;

public class WizardDialContent {
    private String mText = "";
    private boolean mUI = false;
    private boolean mHealth = false;
    private boolean mMana = false;
    private boolean mPause = false;

    public WizardDialContent() {
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public boolean isUi() {
        return mUI||mHealth||mMana;
    }

    public void setUi(boolean ui) {
        this.mUI = ui;
    }

    public boolean isHealth() {
        return mHealth;
    }

    public void setHealth(boolean health) {
        this.mHealth = health;
    }

    public boolean isMana() {
        return mMana;
    }

    public void setMana(boolean mana) {
        this.mMana = mana;
    }

    public boolean isPause() {
        return mPause;
    }

    public void setPause(boolean pause) {
        this.mPause = pause;
    }
}
