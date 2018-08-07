import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.mail.support.LoginSuccessResult;
import java.io.IOException;
import login.task.LoginAuthTokenTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class LoginSuccessTask {

  private static Logger logger = LoggerFactory.getLogger(LoginSuccessTask.class);
  private static String URL = HostConfig.HOST;

  public static LoginSuccessResult execute() {

    try {
      String response = HttpUtils.get(CrawlMeta.getNewInstance(LoginAuthTokenTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      return new LoginSuccessResult(200, doc, "success");
    } catch (IOException e) {
      logger.info("登录后请求异常" + e.getMessage());
      return execute();
    }
  }
}
