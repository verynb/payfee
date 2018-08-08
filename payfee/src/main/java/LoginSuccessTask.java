import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.mail.support.LoginSuccessResult;
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
  private static int tryTime = 1000;

  public static LoginSuccessResult execute() {

    try {
      String response = HttpUtils.get(CrawlMeta.getNewInstance(LoginAuthTokenTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      return new LoginSuccessResult(200, doc, "success");
    } catch (Exception e) {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException e1) {
      }
      if (tryTime > 0) {
        tryTime--;
        return execute();
      } else {
        logger.info("登录后请求异常" + e.getMessage());
        return new LoginSuccessResult();
      }
    }
  }
}
