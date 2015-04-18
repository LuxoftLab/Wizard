package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore;

/**
 * Created by 350z6233 on 20.04.2015.
 */
public class AchievementSpellCounter extends AchievementCounter{
    final Shape shape;
    public AchievementSpellCounter(String ID, String Name, GoogleApiClient mGoogleApiClient, Shape shape) {
        super(ID, Name, mGoogleApiClient,FightCore.CoreAction.CM_SELF_CAST_SUCCESS);
        this.shape=shape;
    }

    @Override
    protected void asd(FightCore fc) {
        Shape custShape = fc.getData().getSelfShape();
        if (custShape == this.shape) {
            increment(1);
        }
    }
}
