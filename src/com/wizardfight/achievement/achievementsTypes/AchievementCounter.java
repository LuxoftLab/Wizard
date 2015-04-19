package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;

/**
 * Created by 350z6233 on 18.04.2015.
 */
public class AchievementCounter extends Achievement {
    FightCore.CoreAction coreAction;

    public AchievementCounter(String ID, String Name, GoogleApiClient mGoogleApiClient,
                              FightCore.CoreAction coreAction) {
        super(ID, Name, mGoogleApiClient);
        this.coreAction = coreAction;
    }

    @Override
    public void update(FightCore fc, FightCore.CoreAction action) {
        if(action == this.coreAction) {
            actionMatched(fc);
        }
    }
    protected void actionMatched(FightCore fc){
        increment(1);
    }
}
