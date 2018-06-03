import com.bit.network.*;
import com.google.common.collect.Maps;
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

import java.util.Map;

/**
 * Created by yuanj on 2017/11/27.
 */
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

    public static LoginResult execute(String tokenValue, String userName, String password) {
        logger.debug("tokenValue[" + tokenValue + "]" + "userName[" + userName + "]");
        HttpPostResult response = null;
        try {
            CrawlHttpConf conf = new CrawlHttpConf(getParam(tokenValue, userName, password));
            response = HttpUtils
                    .doPost(CrawlMeta.getNewInstance(LoginTask.class, URL), conf);
            Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
            System.out.print(doc.toString());
            Element element = doc.select("body").first();
            String value=element.text();
            if (value.contains("You are being")) {
                return new LoginResult(200, LoginExceptionConstance.LOGIN_SUCCESS);
            }else {
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
            return new LoginResult(401, LoginExceptionConstance.USER_NAME_ISNULL);
        }
        if (StringUtils.isBlank(password)) {
            return new LoginResult(401, LoginExceptionConstance.PASSWORLD_ISNULL);
        }
        LoginAuthTokenData data = LoginAuthTokenTask.tryTimes(tryTime, space);
        if (!data.isActive()) {
            return new LoginResult(401, LoginExceptionConstance.LOGIN_TOKEN_ISNULL);
        }
        for (int i = 1; i <= tryTime + 2; i++) {
            LoginResult loginResult = execute(data.getResult(), userName, password);
            logger.debug(loginResult.toString());
            if (loginResult.isActive()) {
                return loginResult;
            } else {
                try {
                    Thread.sleep(RandomUtil.ranNum(space) * 1000 + 5000);
                } catch (InterruptedException e) {
                }
            }
        }
        return new LoginResult(402, LoginExceptionConstance.TRY_EXCEPETION);
    }

    public static void main(String args[]) {
        LoginResult result = LoginTask.tryTimes(1, 100, "hyyi01", "puffs258180");
//        System.out.print(result.toString());
    }

}
