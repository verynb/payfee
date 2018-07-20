package login.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.mail.support.LoginAuthTokenData;
import com.mail.support.LoginExceptionConstance;
import com.mail.support.LoginResult;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
@Slf4j
public class LoginTask {

  private static Logger logger = LoggerFactory.getLogger(LoginTask.class);
  private static String URL = HostConfig.HOST + "auth/login";

  private static Map getParam(String tokenValue, String userName, String password) {
    Map<String, String> param = Maps.newHashMap();
    param.put("user[username]", userName);
    param.put("user[password]", password);
    param.put("authenticity_token", tokenValue);
    return param;
  }

  private static LoginResult execute(String tokenValue, String userName, String password) {
    CrawlHttpConf conf = new CrawlHttpConf(getParam(tokenValue, userName, password));
    try {
      int code = HttpUtils
          .login(CrawlMeta.getNewInstance(LoginTask.class, URL), conf);
      if (code == 302) {
        return new LoginResult(200, LoginExceptionConstance.LOGIN_SUCCESS);
      }
      if (code == 200) {
        return new LoginResult(400,
            LoginExceptionConstance.ACCOUNT_EXCEPETION);
      } else {
        return new LoginResult(code,
            LoginExceptionConstance.LOGIN_TOKEN_ISNULL);
      }
    } catch (IOException e) {
      return new LoginResult(400,
          LoginExceptionConstance.TRY_EXCEPETION);
    }
  }

  public static LoginResult tryTimes(String userName, String password) {
    logger.info("用户[" + userName + "]开始登录");
    if (StringUtils.isBlank(userName)) {
      logger.info("用户名为空");
      return new LoginResult(401, LoginExceptionConstance.USER_NAME_ISNULL);
    }
    if (StringUtils.isBlank(password)) {
      logger.info("密码为空");
      return new LoginResult(401, LoginExceptionConstance.PASSWORLD_ISNULL);
    }
    LoginAuthTokenData data = LoginAuthTokenTask.execute();
    LoginResult result = execute(data.getResult(), userName, password);
    if (!result.isActive()) {
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return tryTimes(userName, password);
    } else {
      logger.info("用户[" + userName + "]登录成功");
      return result;
    }
  }
}
