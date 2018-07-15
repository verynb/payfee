import com.bit.network.GetNetworkTime;
import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
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
      NodeList nl = doc.getElementsByTagName("project01");
      for (int i = 0; i < nl.getLength(); i++) {
        System.out.print("车牌号码:" + doc.getElementsByTagName("location").item(i).getFirstChild().getNodeValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
