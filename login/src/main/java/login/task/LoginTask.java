package login.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mail.support.LoginAuthTokenData;
import com.mail.support.LoginExceptionConstance;
import com.mail.support.LoginResult;

/**
 * Created by yuanj on 2017/11/27.
 */
@Slf4j
public class LoginTask {

  private static Logger logger = LoggerFactory.getLogger(LoginTask.class);
  private static String URL = HostConfig.HOST+"auth/login";

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
      if (response.getResponse().getStatusLine().getStatusCode() == 302) {
        logger.info("用户[" + userName + "]登录成功");
        return new LoginResult(200, LoginExceptionConstance.LOGIN_SUCCESS);
      } else if(response.getResponse().getStatusLine().getStatusCode()==422){
        logger.info("无法处理的请求实体,cookie过期");
        return new LoginResult(response.getResponse().getStatusLine().getStatusCode(), LoginExceptionConstance.ACCOUNT_EXCEPETION);
      }else {
        return new LoginResult(response.getResponse().getStatusLine().getStatusCode(), LoginExceptionConstance.NET_WORK_EXCEPETION);
      }
    } catch (Exception e) {
      return new LoginResult(500, LoginExceptionConstance.NET_WORK_EXCEPETION);
    } finally {
      response.getHttpPost().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

  public static LoginResult tryTimes(int tryTime, int space, String userName, String password)
      throws InterruptedException {
    logger.info("用户[" + userName + "]开始登录");
    if (StringUtils.isBlank(userName)) {
      logger.info("用户名为空");
      return new LoginResult(401, LoginExceptionConstance.USER_NAME_ISNULL);
    }
    if (StringUtils.isBlank(password)) {
      logger.info("密码为空");
      return new LoginResult(401, LoginExceptionConstance.PASSWORLD_ISNULL);
    }
    LoginAuthTokenData data = LoginAuthTokenTask.tryTimes(tryTime, space);
    Thread.sleep(RandomUtil.ranNum(20));
    if (!data.isActive()) {
      logger.info("登录token为空");
      return new LoginResult(401, LoginExceptionConstance.LOGIN_TOKEN_ISNULL);
    }
    Thread.sleep(RandomUtil.ranNum(1000));
    for (int i = 1; i <= tryTime + 2; i++) {
      LoginResult loginResult = execute(data.getResult(), userName, password);
      logger.info(loginResult.toString());
      if (loginResult.isActive()) {
        return loginResult;
      } else {
        try {
          Thread.sleep(RandomUtil.ranNum(space) * 100 + 0);
        } catch (InterruptedException e) {
        }
        logger.info("登录请求重试，剩余" + (tryTime + 2 - i) + "次");
      }
    }
    return new LoginResult(402, LoginExceptionConstance.TRY_EXCEPETION);
  }
}
