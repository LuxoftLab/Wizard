package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightMessage;

/**
 * Created by 350z6233 on 19.04.2015.
 */
public class AchievementWinLoseCounter extends AchievementCounter {
    boolean win;
    boolean lose;

    public AchievementWinLoseCounter(String ID, String Name, GoogleApiClient mGoogleApiClient,
                                     FightCore.CoreAction coreAction,
                                     boolean win, boolean lose) {
        super(ID, Name, mGoogleApiClient, coreAction);
        this.win = win;
        this.lose = lose;
    }

    @Override
    protected void actionMatched(FightCore fc) {
        FightMessage.Target winner = fc.getData().getWinner();
        if(((winner==FightMessage.Target.SELF)&&win)||
            ((winner==FightMessage.Target.ENEMY)&&lose)) {
            increment(1);
        }
    }

}
