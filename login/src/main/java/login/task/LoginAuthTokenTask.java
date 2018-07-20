package login.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.mail.support.LoginAuthTokenData;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取登录token
 */
public class LoginAuthTokenTask {

  private static Logger logger = LoggerFactory.getLogger(LoginAuthTokenTask.class);
  private static final String URL = HostConfig.HOST + "auth/login";
  private static final String INCAPSULA_ERROR = "Request unsuccessful. Incapsula incident ID: 877000090238199605-578629079485186202";

  public static LoginAuthTokenData execute() {
    LoginAuthTokenData loginAuthTokenData = null;
    try {
      String response = HttpUtils.get(CrawlMeta.getNewInstance(LoginAuthTokenTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      Element element = doc.select("input[name=authenticity_token]").first();
      if (!Objects.isNull(element)) {
        loginAuthTokenData = new LoginAuthTokenData(200, element.val());
        logger.info("auth_token[" + loginAuthTokenData.getResult() + "]");
        return loginAuthTokenData;
      } else {
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return execute();
      }
    } catch (IOException e) {
      return execute();
    }
  }
}
