import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.LoginAuthTokenData;
import support.LoginExceptionConstance;
import support.LoginResult;

/**
 * Created by yuanj on 2017/11/27.
 */
@Slf4j
public class LoginTask {

  private static Logger logger = LoggerFactory.getLogger(LoginTask.class);
  private static String URL = "https://www.bitbackoffice.com/auth/login";

  private static Map getParam(String tokenValue, String userName, String password) {
    Map<String, String> param = Maps.newHashMap();
    param.put("user[username]", userName);
    param.put("user[password]", password);
    param.put("authenticity_token", tokenValue);
    return param;
  }

  private static LoginResult execute(String tokenValue, String userName, String password) {
    HttpPostResult response = null;
    try {
      CrawlHttpConf conf = new CrawlHttpConf(getParam(tokenValue, userName, password));
      response = HttpUtils
          .doPost(CrawlMeta.getNewInstance(LoginTask.class, URL), conf);
      Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
      Element element = doc.select("body").first();
      String value = element.text();
      if (value.contains("You are being") || response.getResponse().getStatusLine().getStatusCode() == 302) {
        logger.info("用户[" + userName + "]登录成功");
        return new LoginResult(200, LoginExceptionConstance.LOGIN_SUCCESS);
      } else {
        logger.info("用户名或密码错误");
        return new LoginResult(400, LoginExceptionConstance.ACCOUNT_EXCEPETION);
      }
    } catch (Exception e) {
      return new LoginResult(500, LoginExceptionConstance.NET_WORK_EXCEPETION);
    } finally {
      response.getHttpPost().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

  public static LoginResult tryTimes(int tryTime, int space, String userName, String password) {
    if (StringUtils.isBlank(userName)) {
      logger.info("用户名为空");
      return new LoginResult(401, LoginExceptionConstance.USER_NAME_ISNULL);
    }
    if (StringUtils.isBlank(password)) {
      logger.info("密码为空");
      return new LoginResult(401, LoginExceptionConstance.PASSWORLD_ISNULL);
    }
    LoginAuthTokenData data = LoginAuthTokenTask.tryTimes(tryTime, space);
    if (!data.isActive()) {
      logger.info("登陆token为空");
      return new LoginResult(401, LoginExceptionConstance.LOGIN_TOKEN_ISNULL);
    }
    for (int i = 1; i <= tryTime + 2; i++) {
      LoginResult loginResult = execute(data.getResult(), userName, password);
      logger.info(loginResult.toString());
      if (loginResult.isActive()) {
        return loginResult;
      } else {
        try {
          Thread.sleep(RandomUtil.ranNum(space) * 1000 + 0);
        } catch (InterruptedException e) {
        }
        logger.info("登录请求重试，剩余" + (tryTime + 2 - i) + "次");
      }
    }
    return new LoginResult(402, LoginExceptionConstance.TRY_EXCEPETION);
  }
}
