import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mail.support.RenewalIdConstance;
import com.mail.support.RenewalParam;

import java.util.Map;

import static com.mail.support.RenewalIdConstance.*;

/**
 * Created by yuanj on 2017/11/27.
 */
@Slf4j
public class RenewalTask {

  private static Logger logger = LoggerFactory.getLogger(RenewalTask.class);
  private static String URL = HostConfig.HOST + "bond_payments";

  private static String REFERER_URL = HostConfig.HOST;

  private static Map getParam(RenewalParam param) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put(RenewalIdConstance.AUTHENTICITYTOKEN, param.getAuthenticityToken());
    paramMap.put(RenewalIdConstance.PAYBONDID, param.getPayBondId());

    paramMap.put(PAYONEAMOUNT, param.getPayOneAmount());
    paramMap.put(PAYONEUSERWALLETID, param.getPayOneUserWalletId());
    paramMap.put(PAYONEUERID, param.getPayOneUerId());

    paramMap.put(PAYTWOAMOUNT, param.getPayTwoAmount());
    paramMap.put(PAYTWOUSERWALLETID, param.getPayTwoUserWalletId());
    paramMap.put(PAYTWOUERID, param.getPayTwoUerId());

    paramMap.put(PAYTHREEAMOUNT, param.getPayThreeAmount());
    paramMap.put(PAYTHREEUSERWALLETID, param.getPayThreeUserWalletId());
    paramMap.put(PAYTHREEUERID, param.getPayThreeUerId());

    paramMap.put(INPUTAMOUNT, param.getInputAmount());
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
    headMap.put("referer", REFERER_URL);
    headMap.put("origin", REFERER_URL);
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }


  public static int execute(RenewalParam param) {
    try {
      CrawlHttpConf conf = new CrawlHttpConf(getParam(param), getHeader());
      String response = HttpUtils
          .post(CrawlMeta.getNewInstance(RenewalTask.class, URL), conf);
      logger.debug("续期返回值" + response);
      if (response.contains("success")) {
        return 200;
      } else {
        return 400;
      }
    } catch (IOException e) {
      return 500;
    }
  }
}
