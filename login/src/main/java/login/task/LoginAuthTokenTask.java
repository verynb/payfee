package login.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.mail.support.LoginAuthTokenData;
import java.util.Objects;
import org.apache.http.util.EntityUtils;
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

  private static LoginAuthTokenData execute() {
    HttpResult response = null;
    LoginAuthTokenData loginAuthTokenData = null;
    try {
      response = HttpUtils.doGet(CrawlMeta.getNewInstance(LoginAuthTokenTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
      Element element = doc.select("input[name=authenticity_token]").first();
      if (!Objects.isNull(element)) {
        loginAuthTokenData = new LoginAuthTokenData(200, element.val());
        logger.info("auth_token[" + loginAuthTokenData.getResult() + "]");
      } else {
        loginAuthTokenData = new LoginAuthTokenData(400, INCAPSULA_ERROR);
        logger.info("auth_token失败,返回" + doc.toString());
      }
    } catch (Exception e) {
      logger.info("auth_token请求异常" + e.getMessage());
      return new LoginAuthTokenData(500, e.getMessage());
    } finally {
      response.getHttpGet().releaseConnection();
//            response.getHttpClient().getConnectionManager().shutdown();
    }
    return loginAuthTokenData;
  }

  public static LoginAuthTokenData tryTimes(int tryTime, int space) {
    for (int i = 1; i <= tryTime + 2; i++) {
      LoginAuthTokenData loginAuthTokenData = execute();
      if (loginAuthTokenData.getCode() == 200) {
        return loginAuthTokenData;
      } else {
        try {
          Thread.sleep(RandomUtil.ranNum(space) * 1000);
        } catch (InterruptedException e) {
        }
        logger.info("获取登录页面请求重试，剩余" + (tryTime + 2 - i) + "次");
      }
    }
    return new LoginAuthTokenData(400, INCAPSULA_ERROR);
  }

}
