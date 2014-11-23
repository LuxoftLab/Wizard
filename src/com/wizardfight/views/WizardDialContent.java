package com.wizardfight.views;

public class WizardDialContent {
    private String text="";
    private boolean ui=false;
    private boolean health=false;
    private boolean mana=false;
    private boolean pause=false;

    public WizardDialContent() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUi() {
        return ui||health||mana;
    }

    public void setUi(boolean ui) {
        this.ui = ui;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public boolean isMana() {
        return mana;
    }

    public void setMana(boolean mana) {
        this.mana = mana;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
