package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.PlayerState;

/**
 * Created by 350z6233 on 19.04.2015.
 */
public class AchievementHealthCounter extends AchievementCounter {
    private boolean heal;
    private int lastHealth;

    public AchievementHealthCounter(String ID, String Name, GoogleApiClient mGoogleApiClient,
                                    FightCore.CoreAction coreAction,
                                    boolean heal) {
        super(ID, Name, mGoogleApiClient, coreAction);
        this.heal = heal;
    }

    @Override
    public void onStart(FightCore fc) {
        super.onStart(fc);
        lastHealth = getCurrentHealth(fc);
    }

    @Override
    protected void actionMatched(FightCore fc) {
        int currentHealth = getCurrentHealth(fc);
        int delta = currentHealth - lastHealth;
        if((heal&&delta>0)||(!heal&&delta<0)){
            increment(Math.abs(delta));
        }
    }

    private int getCurrentHealth(FightCore fc) {
        PlayerState state;
        if (coreAction == FightCore.CoreAction.CM_SELF_HEALTH_CHANGED) {
            state = fc.getSelfState();
        } else {
            state = fc.getEnemyState();
        }
        return state.getHealth();
    }
}
