package com.wizardfight.achievement;

import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.achievement.achievementsTypes.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Class-factory, which is able to work with different types of isAchievementsLoadFailed.
 * Created by 350z6233 on 20.04.2015.
 */
public abstract class AchievementsFactory {
    /**
     * Enum that stores information about the existing types of isAchievementsLoadFailed
     */
    enum AchievementTypes {
        Counter(AchievementCounter.class),
        Spell(AchievementSpell.class),
        Health(AchievementHealth.class),
        WinLose(AchievementWinLose.class),
        Time(AchievementTime.class);

        final Class<? extends Achievement> achievementClass;

        AchievementTypes(Class<? extends Achievement> c){
            achievementClass=c;
        }

        public static Achievement parse(String typeName, GoogleApiClient googleApiClient, Node node) throws IllegalArgumentException,
                InvocationTargetException,
                NoSuchMethodException,
                InstantiationException,
                IllegalAccessException {
            return getValue(typeName).parse(googleApiClient,node);
        }

        public Achievement parse(GoogleApiClient googleApiClient,Node node) throws NoSuchMethodException,
                IllegalAccessException,
                InvocationTargetException,
                InstantiationException {
            return achievementClass.getConstructor(GoogleApiClient.class, Node.class).newInstance(googleApiClient,node);
        }

        public static AchievementTypes getValue(String typeName) throws IllegalArgumentException {
            for(AchievementTypes value : values()){
                if(value.name().equalsIgnoreCase(typeName))
                    return value;
            }
            throw new IllegalArgumentException("No enum const class");
        }
    }

    public static void parse(InputStream inputStream, GoogleApiClient googleApiClient,Set<Achievement> achievementSet) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                try {
                    Node node = nodeList.item(i);
                    if (node.getNodeName().equalsIgnoreCase("achievement")) {
                        Node temp = node.getAttributes().getNamedItem("type");
                        if (temp != null) {
                            String typeName = temp.getNodeValue();
                            Achievement achievement = AchievementTypes.parse(typeName,googleApiClient,node);
                            achievementSet.add(achievement);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("XML_ach", "unknown type");
                }catch (Exception e){
                    Log.e("XML_ach", "constructor error");
                }
            }
        } catch (Exception e) {
            Log.e("XML_ach", "");
        }
    }

  /*    private static Achievement parseAchievementWinLose(String ID, String Name, GoogleApiClient mGoogleApiClient, FightCore.CoreAction ca, Node node) throws Exception {
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
        return new AchievementWinLose(ID, Name, mGoogleApiClient,
                ca,
                win, lose);
    }*/
}
