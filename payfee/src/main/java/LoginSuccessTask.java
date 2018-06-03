import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.LoginSuccessResult;
import support.RenewalAmount;
import support.RenewalParam;
import support.RenewalWallet;

import java.util.List;

/**
 * Created by yuanj on 2017/11/27.
 */
public class LoginSuccessTask {

    private static Logger logger = LoggerFactory.getLogger(LoginSuccessTask.class);
    private static String URL = "https://www.bitbackoffice.com";

    public static LoginSuccessResult execute() {
        HttpResult response = null;
        try {
            response = HttpUtils.doGet(CrawlMeta.getNewInstance(LoginAuthTokenTask.class, URL), new CrawlHttpConf());
            Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
            return new LoginSuccessResult(200, doc,"success");
        } catch (Exception e) {
            logger.info("登录后请求异常"+e.getMessage());
            return new LoginSuccessResult(500, null,e.getMessage());
        } finally {
            response.getHttpGet().releaseConnection();
            response.getHttpClient().getConnectionManager().shutdown();
        }
    }

    public static LoginSuccessResult tryTimes(int tryTime, int space, String userName, String password) {
        LoginTask.tryTimes(tryTime, space, userName, password);
        return execute();
    }

    public static void main(String args[]) {
        LoginSuccessResult result = LoginSuccessTask.tryTimes(1, 100, "hyyi01", "puffs258180");
        RenewalAmount wallets = result.filterWallet();
        RenewalParam param = result.filterIdValue();
        System.out.print(wallets.toString());
        System.out.print(param.toString());
    }
}
