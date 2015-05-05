package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.PlayerState;
import org.w3c.dom.Node;

/**
 * Counts changes in health.
 * XML :
 * <pre>{@code
 * <achievement
 * type="Health"
 * id="CgkI2YKzhoMbEAIQCw"
 * name="sharpshooter"
 * action="CM_ENEMY_HEALTH_MANA" // action on which this object should recalculate health
 * enemy="true" //{@link #enemy}
 * heal="false" //{@link #heal}
 * />}
 * <pre>
 * Created by 350z6233 on 19.04.2015.
 */
public class AchievementHealth extends AchievementCounter {
    /**
     * Flag, which defines what should be count: heal or damage.
     * If heal == true, counts heal.
     * If heal == false, counts damage.
     */
    private boolean heal = false;
    /**
     * Flag, which defines what should be count: enemy or self.
     * If enemy == true, counts enemy health.
     * If enemy == false, counts self health.
     */
    private boolean enemy = false;
    /**
     * Contains information about the latest state of health
     */
    private int lastHealth;
    /**
     * Counter of changes in health.
     */
    private int count;
    /**
     * Conversion factor of HP in achievement steps.
     */
    private final double coef=5;

    public AchievementHealth(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {
        super(googleApiClient, node);
        Node temp = node.getAttributes().getNamedItem("heal");
        if (temp != null) {
            heal = Boolean.parseBoolean(temp.getNodeValue());
        }
        temp = node.getAttributes().getNamedItem("enemy");
        if (temp != null) {
            enemy = Boolean.parseBoolean(temp.getNodeValue());
        }

    }

    /**
     * Reset on start of fight
     */
    @Override
    protected void onStart(FightCore fc) {
        super.onStart(fc);
        lastHealth = getCurrentHealth(fc);
        count=0;
    }

    /**
     * Calculation of health changes.
     * @param fc link to FightCore of this fight
     */
    @Override
    protected void actionMatched(FightCore fc) {
        int currentHealth = getCurrentHealth(fc);
        int delta = currentHealth - lastHealth;
        if((heal&&delta>0)||(!heal&&delta<0)){
            count+=Math.abs(delta);

        }
    }

    /**
     * Calculate the achievement steps that the player has moved in this fight.
     * @param fc link to FightCore of this fight
     */
    @Override
    protected void onFinish(FightCore fc) {
        super.onFinish(fc);
        increment((int)(count/coef));
    }

    private int getCurrentHealth(FightCore fc) {
        PlayerState state;
        if (enemy) {
            state = fc.getEnemyState();
        } else {
            state = fc.getSelfState();
        }
        return state.getHealth();
    }
}
