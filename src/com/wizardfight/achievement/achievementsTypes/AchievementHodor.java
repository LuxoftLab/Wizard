package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore;
import org.w3c.dom.Node;

/**
 * Created by 350z6233 on 19.04.2015.
 */
public class AchievementHodor extends Achievement {
    /**
     *  contains last player's spell.
     */
    Shape lastShape=null;
    int count=0;

    /**
     * Constructor of class AchievementHodor, which can parse xml.
     * Calls super class constructor {@link Achievement#Achievement(GoogleApiClient, Node)}
     * @param googleApiClient Object reference GoogleApiClient, which will be connected to the server.
     * @param node Node of xml.
     * @throws IllegalArgumentException
     */
    public AchievementHodor(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {
        super(googleApiClient, node);
    }

    /**
     * Reset {@link #lastShape}
     * @param fc link to FightCore of this fight
     */
    @Override
    public void onStart(FightCore fc) {
        super.onStart(fc);
        lastShape=null;
        count=0;
    }

    /**
     * @param fc Link to FightCore of this fight
     * @param action Action that changed the FightCore.
     */
    @Override
    public void update(FightCore fc, FightCore.CoreAction action) {
        super.update(fc,action);
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
