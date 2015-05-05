package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore;
import org.w3c.dom.Node;

/**
 * It counts the number of completed spells.
 * XML :
 * <pre>{@code
 * <achievement
 * type="shape"
 * id="CgkI2YKzhoMbEAIQDg"
 * name="lord_of_darkness"
 * shape="z" //contains an shape to which class should react.
 * />}
 * </pre>
 * Created by 350z6233 on 20.04.2015.
 */
public class AchievementSpell extends AchievementCounter{
    /**
     * It contains an shape to which class should react.
     */
    private final Shape shape;

    /**
     * Constructor of class AchievementSpell, which can parse xml.
     * Fills the fields: {@link #shape}.
     * Calls super class constructor {@link AchievementCounter#AchievementCounter(GoogleApiClient, Node)}
     * @param googleApiClient Object reference GoogleApiClient, which will be connected to the server.
     * @param node Node of xml.
     * @throws IllegalArgumentException
     */
    public AchievementSpell(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {
        super(googleApiClient, node, FightCore.CoreAction.CM_SELF_CAST_SUCCESS);
        shape = Shape.getShapeFromString(node.getAttributes().getNamedItem("shape").getNodeValue());
        if(shape == null){
            throw new IllegalArgumentException("shape");
        }
    }

    /**
     * Check for matching shape with {@link #shape}.
     * @param fc link to FightCore of this fight
     */
    @Override
    protected void actionMatched(FightCore fc) {
        Shape custShape = fc.getData().getSelfShape();
        if (custShape == this.shape) {
            increment(1);
        }
    }
}
