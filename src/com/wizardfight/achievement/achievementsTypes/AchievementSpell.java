package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;

/**
 * Created by 350z6233 on 20.04.2015.
 */
public class AchievementSpell extends Achievement{
    final Shape shape;
    public AchievementSpell(String ID, String Name, GoogleApiClient mGoogleApiClient, Shape shape) {
        super(ID, Name, mGoogleApiClient);
        this.shape=shape;
    }
    public void update(Shape shape){
        if(shape==this.shape){
            increment(1);
        }
    }
}
