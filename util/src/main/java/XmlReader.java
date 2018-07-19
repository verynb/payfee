import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by yuanj on 2018/7/15.
 */
public class XmlReader {

  public static void main(String arge[]) {

    try {
//      GetNetworkTime.getNetworkLimiteTime()
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new FileInputStream("E:\\airbit.conf"));
      NodeList nl = doc.getElementsByTagName("location");
      Node localtion = nl.item(0);
      NodeList localtions=localtion.getChildNodes().item(0).getChildNodes();
      for (int i = 0; i < localtions.getLength(); i++) {
//        localtions.item(i)
        System.out.println(localtions.item(i).getNodeName());
//        nl.item(i).getChildNodes().item(0)
//        System.out.println(localtions.item(i).getNodeValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
