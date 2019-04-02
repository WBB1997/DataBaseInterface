package Util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

public class XmlUtil {
    private static Document document = null;

    public static Object getBean() {
        Object obj = null;
        try {
            Class c = Class.forName(getAttribute("AdapterPath"));
            obj = c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static String getAttribute(String Attr) {
        try {
            if (document == null)
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("resource/config.xml");
            NodeList nl = document.getElementsByTagName(Attr);
            Node classNode = nl.item(0).getFirstChild();
            return classNode.getNodeValue().trim();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}