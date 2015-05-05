package com.wizardfight.achievement.achievementsTypes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightMessage;
import org.w3c.dom.Node;

/**
 * Counts the number of wins and losses of player.
 * <pre>{@code
 *  <achievement
 *  type="WinLose"
 *  id="CgkI2YKzhoMbEAIQGg"
 *  name="fighter"
 *  win="true"
 *  lose="true"
 *  />}
 *  </pre>
 * Created by 350z6233 on 19.04.2015.
 */
public class AchievementWinLose extends Achievement {
    /**
     * Flag that specifies whether to count player's wins for this achievement.
     * If win == true, counts wins.
     * If win == false, no counts wins.
     */
    boolean win = false;
    /**
     * Flag that specifies whether to count player's losess for this achievement.
     * If lose == true, counts loses.
     * If lose == false, no counts loses.
     */
    boolean lose = false;
    /**
     * Constructor of class AchievementCounter, which can parse xml.
     * Fills the fields: {@link #win},{@link #lose}.
     * Calls super class constructor {@link Achievement#Achievement(GoogleApiClient, Node)}
     * @param googleApiClient Object reference GoogleApiClient, which will be connected to the server.
     * @param node Node of xml.
     * @throws IllegalArgumentException
     */
    public AchievementWinLose(GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException {
        super(googleApiClient, node);
        Node temp = node.getAttributes().getNamedItem("win");
        if (temp != null) {
            win = Boolean.parseBoolean(temp.getNodeValue());
        }
        temp = node.getAttributes().getNamedItem("lose");
        if (temp != null) {
            lose = Boolean.parseBoolean(temp.getNodeValue());
        }
    }

    /**
     * Learns the player won or lost.
     * @param fc link to FightCore of this fight
     */
    @Override
    protected void onFinish(FightCore fc) {;
        FightMessage.Target winner = fc.getData().getWinner();
        if(((winner==FightMessage.Target.SELF)&&win)||
            ((winner==FightMessage.Target.ENEMY)&&lose)) {
            increment(1);
        }
    }
}
