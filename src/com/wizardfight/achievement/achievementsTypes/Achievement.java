package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.wizardfight.fight.FightCore;

import java.util.Observer;


/**
 * Created by 350z6233 on 15.04.2015.
 */

public abstract class Achievement{
    private final GoogleApiClient mGoogleApiClient;
    private final String ID;
    private final String Name;

    public Achievement(String ID,  String Name, GoogleApiClient mGoogleApiClient) {
        this.ID = ID;
        this.Name = Name;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void increment(int count) {
        if (mGoogleApiClient.isConnected()) {
            Games.Achievements.increment(mGoogleApiClient, ID, count);
        }
    }

    public abstract void update(FightCore fc, FightCore.CoreAction action);

    @Override
    public String toString() {
        return "Achievement{" +
                "mGoogleApiClient='" + mGoogleApiClient + '\'' +
                ", ID='" + ID + '\'' +
                ", Name='" + Name + '\'' +
                '}';
    }
}
