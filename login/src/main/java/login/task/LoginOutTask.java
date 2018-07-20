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
public class LoginOutTask {

  private static Logger logger = LoggerFactory.getLogger(LoginOutTask.class);
  private static String URL = HostConfig.HOST + "auth/logout";

  private static Map getParam(String tokenValue) {
    Map<String, String> param = Maps.newHashMap();
    param.put("_method", "delete");
    param.put("authenticity_token", tokenValue);
    return param;
  }

  public static LoginResult execute(String tokenValue){
    CrawlHttpConf conf = new CrawlHttpConf(getParam(tokenValue));
    try {
      int code = HttpUtils
          .login(CrawlMeta.getNewInstance(LoginOutTask.class, URL), conf);
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
    }catch (IOException e){
      return new LoginResult(400,
          LoginExceptionConstance.TRY_EXCEPETION);
    }
  }
}
