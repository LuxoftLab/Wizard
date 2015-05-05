package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import org.w3c.dom.Node;

/**
 * It counts the number of completed actions.
 * XML :
 * <pre>{@code
 * <achievement
 * type="Counter"
 * id="CgkI2YKzhoMbEAIQAw"
 * name="resistant"
 * action="CM_SELF_SHIELD_BLOCK" //contains an event to which class should react.
 * />}
 * </pre>
 * Created by 350z6233 on 18.04.2015.
 */

public class AchievementCounter extends Achievement {
    /**
     * It contains an event to which class should react.
     */
    private FightCore.CoreAction coreAction;

    /**
     * Constructor of class AchievementCounter, which can parse xml.
     * Fills the fields: {@link #coreAction}.
     * Calls super class constructor {@link Achievement#Achievement(GoogleApiClient, Node)}
     * @param googleApiClient Object reference GoogleApiClient, which will be connected to the server.
     * @param node Node of xml.
     * @throws IllegalArgumentException
     */
    public AchievementCounter(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {
        super(googleApiClient, node);
        coreAction = FightCore.CoreAction.valueOf(node.getAttributes().getNamedItem("action").getNodeValue());
        if(coreAction==null){
            throw new IllegalArgumentException("coreAction ");
        }
    }

    /**
     * Constructor of class AchievementCounter, which can parse xml with explicit set of coreAction.
     * Fills the fields: {@link #coreAction}.
     * Calls super class constructor {@link Achievement#Achievement(GoogleApiClient, Node)}
     * @param googleApiClient Object reference GoogleApiClient, which will be connected to the server.
     * @param node Node of xml.
     * @param coreAction [@link #coreAction]
     * @throws IllegalArgumentException
     */
    public AchievementCounter(GoogleApiClient googleApiClient,Node node,
                              FightCore.CoreAction coreAction) throws IllegalArgumentException {
        super(googleApiClient, node);
        if(coreAction==null){
            throw new IllegalArgumentException("coreAction ");
        }
        this.coreAction = coreAction;
    }

    /**
     * Check for matching FightCore events with {@link #coreAction}.
     * @param fc Link to FightCore of this fight
     * @param action Action that changed the FightCore.
     * @see #actionMatched(FightCore)
     */
    @Override
    public void update(FightCore fc, FightCore.CoreAction action) {
        super.update(fc,action);
        if(action == this.coreAction) {
            actionMatched(fc);
        }
    }

    /**
     * This method is called when events match with {@link #coreAction}.
     * @param fc link to FightCore of this fight
     * @see #update(FightCore, FightCore.CoreAction)
     */
    void actionMatched(FightCore fc){
        increment(1);
    }

}
