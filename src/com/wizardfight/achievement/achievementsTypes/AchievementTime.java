package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import org.w3c.dom.Node;

/**
 * Created by Berlizov on 06.05.2015.
 */
public class AchievementTime extends Achievement {
    /**
     * Contains the starting time of fight.
     */
    private long startTime;
    /**
     * Conversion factor of game seconds in achievement steps.
     */
    private final double coef=5;

    /**
     *
     * @param googleApiClient
     * @param node
     * @throws IllegalArgumentException
     */
    public AchievementTime(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {//TODO:Test
        super(googleApiClient, node);
    }

    @Override
    void onFinish(FightCore fc) {
        super.onFinish(fc);
        double timeInSec = (System.currentTimeMillis() - startTime) / 1000 / coef;
        increment((int)timeInSec);
    }

    @Override
    void onStart(FightCore fc) {
        super.onStart(fc);
        startTime = System.currentTimeMillis();

    }
}
