package identity;

import com.bit.network.GetNetworkTime;
import com.google.common.collect.Lists;
import identity.LocationConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by yuanj on 2018/7/15.
 */
public class XmlReader {


  public static LocationConfig getConfig(String location, String pName) {
    try {
      Document document = getDoc();
      List<LocationConfig> configs = getProject(document, pName);
      return configs.stream()
          .filter(c -> c.getName().equals(location))
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      return null;
    }
  }

  private static Document getDoc() throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(GetNetworkTime.getNetworkConfig());
    return doc;
  }

  private static List<LocationConfig> getProject(Document doc, String pName) {
    List<LocationConfig> locationConfigs = Lists.newArrayList();
    NodeList pNodeList = doc.getElementsByTagName("project");
    Node pNode = null;
    for (int j = 0; j < pNodeList.getLength(); j++) {
      NamedNodeMap attrs = pNodeList.item(j).getAttributes();
      Node attr = attrs.item(0);
      if (attr.getNodeValue().equals(pName)) {
        pNode = pNodeList.item(j);
        break;
      }
    }
    NodeList bookList = pNode.getChildNodes();
    for (int i = 0; i < bookList.getLength(); i++) {
      NodeList cList = bookList.item(i).getChildNodes();
      String nameValue = "";
      String verValue = "";
      String timelimitValue = "";
      String passwordValue = "";
      for (int k = 0; k < cList.getLength(); k++) {
        if (cList.item(k).getNodeType() == Node.ELEMENT_NODE) {
          String name = cList.item(k).getNodeName();
          String value = cList.item(k).getFirstChild().getNodeValue();
          if (name.equals("name")) {
            nameValue = value;
          }
          if (name.equals("ver")) {
            verValue = value;
          }
          if (name.equals("timelimit")) {
            timelimitValue = value;
          }
          if (name.equals("password")) {
            passwordValue = value;
          }
          /*System.out.print("第" + (k + 1) + "个节点的节点名："
              + cList.item(k).getNodeName());
          //获取了element类型节点的节点值
          System.out.println("--节点值是：" + cList.item(k).getFirstChild().getNodeValue());*/
        }
      }
      locationConfigs.add(new LocationConfig(nameValue, verValue, timelimitValue, passwordValue));
    }
    return locationConfigs.stream()
        .filter(l -> l.filter())
        .collect(Collectors.toList());

  }

  public static void main(String arge[]) {

    try {
      System.out.println(getConfig("shenzhen", "收分").toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
