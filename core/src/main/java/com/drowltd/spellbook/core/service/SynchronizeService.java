package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.model.DictionaryEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SynchronizeService {
    private static final String UPDATE_URL = "http://78.128.18.63:7777/update/1276242306.xml";

    private static SynchronizeService instance;
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    public static SynchronizeService getInstance() {
        if (instance == null) {
            instance = new SynchronizeService();
        }

        return instance;
    }

    public List<DictionaryEntry> retrieveUpdatedEntries() {
        List<DictionaryEntry> updatedEntries = new ArrayList<DictionaryEntry>();

        try {
            URL updateUrl = new URL(UPDATE_URL);

            BufferedInputStream in = new BufferedInputStream(updateUrl.openStream());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("suggestion");
            System.out.println("Information of all suggestions");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node firstNode = nodeLst.item(s);

                if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                    DictionaryEntry dictionaryEntry = new DictionaryEntry();

                    Element firstElement = (Element) firstNode;
                    NodeList dictionaryNodeList = firstElement.getElementsByTagName("dictionary-name");
                    Element dictionaryElement = (Element) dictionaryNodeList.item(0);
                    NodeList dictionary = dictionaryElement.getChildNodes();
                    System.out.println("Dictionary : " + ((Node) dictionary.item(0)).getNodeValue());

                    NodeList wordNodeList = firstElement.getElementsByTagName("word");
                    Element wordElement = (Element) wordNodeList.item(0);
                    NodeList word = wordElement.getChildNodes();

                    System.out.println("Word : " + ((Node) word.item(0)).getNodeValue());

                    NodeList translationNodeList = firstElement.getElementsByTagName("translation");
                    Element translationElement = (Element) translationNodeList.item(0);
                    NodeList translation = translationElement.getChildNodes();

                    System.out.println("Translation : " + ((Node) word.item(0)).getNodeValue());

                    updatedEntries.add(dictionaryEntry);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updatedEntries;
    }

    public void pullUpdates() {

    }

    public void pushUpdates() {

    }

    public static void main(String[] args) {
        SynchronizeService synchronizeService = getInstance();
        synchronizeService.retrieveUpdatedEntries();
    }


}
