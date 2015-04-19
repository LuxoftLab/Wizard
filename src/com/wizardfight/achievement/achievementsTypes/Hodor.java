package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore;

/**
 * Created by 350z6233 on 19.04.2015.
 */
public class Hodor extends Achievement {
    Shape lastShape=null;
    int count=0;


    public Hodor(String ID, String Name, GoogleApiClient mGoogleApiClient) {
        super(ID, Name, mGoogleApiClient);
    }

    @Override
    public void onStart(FightCore fc) {
        super.onStart(fc);
        lastShape=null;
        count=0;
    }

    @Override
    public void update(FightCore fc, FightCore.CoreAction action) {
        if(action==FightCore.CoreAction.CM_SELF_CAST_SUCCESS)
        {
            Shape custShape = fc.getData().getSelfShape();
            if (custShape == lastShape) {
                count++;
            }else{
                count=0;
                lastShape=custShape;
            }
            if(count==5){
                unlock();
            }
        }
    }
}
