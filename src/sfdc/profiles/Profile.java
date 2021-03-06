package sfdc.profiles;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sfdc.profiles.profileNodes.ElementBuilder;
import sfdc.profiles.profileNodes.ProfileNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Profile {
    private String path;
    private Set<ProfileNode> profileNodes = new HashSet<>();

    public Profile(String path) {
        this.path = path;
        parseFile();
    }

    /**
     * Adds or replaces existing permission node in the Profile xml.
     *
     * @param node - Concrete instance of ProfileNode, which may be ex. FieldPermission node.
     */
    public void add(ProfileNode node) {
        this.profileNodes.remove(node);
        this.profileNodes.add(node);
    }

    /**
     * Removes existing permission node in the Profile xml.
     *
     * @param node - Concrete instance of ProfileNode, which may be ex. FieldPermission node.
     */
    public void remove(ProfileNode node) {
        this.profileNodes.remove(node);
    }

    public void saveFile() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlStandalone(false);

            Element rootElement = document.createElementNS("http://soap.sforce.com/2006/04/metadata", "Profile");
            document.appendChild(rootElement);

            ElementBuilder elementBuilder = new ElementBuilder(document);

            ArrayList<ProfileNode> profileNodes = new ArrayList<>(this.profileNodes);
            profileNodes.sort(Comparator.naturalOrder());

            for (ProfileNode node : profileNodes) {
                rootElement.appendChild(node.getElement(elementBuilder));
            }


            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(new File(path))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseFile() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(path));

            NodeList childNodes = document.getDocumentElement().getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                ProfileNode profileNode = ProfileNode.newInstance(childNodes.item(i));

                if (profileNode != null) {
                    this.profileNodes.add(profileNode);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
