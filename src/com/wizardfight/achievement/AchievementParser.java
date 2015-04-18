package com.wizardfight.achievement;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.achievement.achievementsTypes.Achievement;
import com.wizardfight.achievement.achievementsTypes.AchievementSpellCounter;
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
                            temp = node.getAttributes().
                                    getNamedItem("name");
                            if (temp != null) {
                                name = temp.getNodeValue();
                            }
                            temp = node.getAttributes().
                                    getNamedItem("shape");
                            if (temp != null) {
                                Shape shape = Shape.getShapeFromString(temp.getNodeValue());
                                achievementList.add(new AchievementSpellCounter(id, name, googleApiClient, shape));
                            }
                        }
                    }
                }catch (Exception e){
                    //todo
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo
        }


    }
}
