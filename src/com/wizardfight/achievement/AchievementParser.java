package com.wizardfight.achievement;

import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.achievement.achievementsTypes.*;
import com.wizardfight.fight.FightCore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by 350z6233 on 20.04.2015.
 */
public abstract class AchievementParser {
    public static void parse(InputStream inputStream, Set<Achievement> achievementList, GoogleApiClient googleApiClient) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                try {
                    Node node = nodeList.item(i);
                    if (node.getNodeName().equalsIgnoreCase("achievement")) {
                        Node temp = node.getAttributes().getNamedItem("id");
                        if (temp != null) {
                            String id = temp.getNodeValue();
                            String name = null;
                            temp = node.getAttributes().getNamedItem("name");
                            if (temp != null) {
                                name = temp.getNodeValue();
                            }
                            Achievement achievement = null;

                            temp = node.getAttributes().getNamedItem("action");
                            if (temp != null) {
                                FightCore.CoreAction ca = FightCore.CoreAction.valueOf(
                                        temp.getNodeValue());
                                switch (ca) {
                                    case CM_SELF_CAST_SUCCESS:
                                        achievement=parseAchievementSpell(id,name,googleApiClient,node);
                                        break;
                                    case CM_SELF_HEALTH_CHANGED:
                                    case CM_ENEMY_HEALTH_MANA:
                                        achievement= parseAchievementHealth(id, name, googleApiClient, ca, node);
                                        break;
                                    case CM_FIGHT_END:
                                        achievement= parseAchievementWinLose(id, name, googleApiClient, ca, node);
                                        break;
                                    default:
                                        achievement = new AchievementCounter(id, name, googleApiClient, ca);
                                        break;
                                }
                            }else{
                                temp = node.getAttributes().getNamedItem("class");
                                if (temp != null&&temp.getNodeValue().equalsIgnoreCase("hodor")) {
                                    achievement = new Hodor(id,name,googleApiClient);
                                }
                            }
                            if (achievement != null) {
                                achievementList.add(achievement);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("XML_ach", "unknown type");
                }
            }
        } catch (Exception e) {
            Log.e("XML_ach", "");
        }
    }

    private static Achievement parseAchievementSpell(String ID, String Name, GoogleApiClient mGoogleApiClient, Node node) throws Exception{
        Node temp = node.getAttributes().getNamedItem("shape");
        Shape shape = Shape.getShapeFromString(temp.getNodeValue());
        return new AchievementSpellCounter(ID, Name, mGoogleApiClient,
                shape);
    }

    private static Achievement parseAchievementHealth(String ID, String Name, GoogleApiClient mGoogleApiClient, FightCore.CoreAction ca, Node node) throws Exception{
        boolean heal = false;
        Node temp = node.getAttributes().getNamedItem("shape");
        if (temp != null) {
            heal = Boolean.parseBoolean(temp.getNodeValue());
        }
        return new AchievementHealthCounter(ID, Name, mGoogleApiClient,
                ca,
                heal);
    }

    private static Achievement parseAchievementWinLose(String ID, String Name, GoogleApiClient mGoogleApiClient, FightCore.CoreAction ca, Node node) throws Exception {
        boolean win = false;
        boolean lose = false;
        Node temp = node.getAttributes().getNamedItem("win");
        if (temp != null) {
            win = Boolean.parseBoolean(temp.getNodeValue());
        }
        temp = node.getAttributes().getNamedItem("lose");
        if (temp != null) {
            lose = Boolean.parseBoolean(temp.getNodeValue());
        }
        return new AchievementWinLoseCounter(ID, Name, mGoogleApiClient,
                ca,
                win, lose);
    }
}
