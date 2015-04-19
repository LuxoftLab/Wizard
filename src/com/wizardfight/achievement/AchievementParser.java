package com.wizardfight.achievement;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wizardfight.Shape;
import com.wizardfight.achievement.achievementsTypes.AchievementSpell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

/**
 * Created by 350z6233 on 20.04.2015.
 */
public abstract class AchievementParser {
    public static void parse(InputStream inputStream, List<AchievementSpell> achievementSpellList, GoogleApiClient googleApiClient) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (node.getNodeName().equalsIgnoreCase("achievement")) {
                    Node temp = node.getAttributes().getNamedItem("id");
                    if (temp != null) {
                        String id = temp.getNodeValue();
                        String name=null;
                        temp = node.getAttributes().
                                getNamedItem("name");
                        if (temp != null) {
                            name = temp.getNodeValue();
                        }
                        temp = node.getAttributes().
                                getNamedItem("shape");
                        if (temp!=null) {
                            Shape shape=Shape.valueOf(temp.getNodeValue());
                            achievementSpellList.add(new AchievementSpell(id, name, googleApiClient,shape));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo
        }


    }
}
