package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.mail.api.MailTokenData;
import com.mail.support.FilterMailUtil;
import com.transfer.entity.AddBitAccountParam;
import com.transfer.entity.PayOutPageData;
import com.transfer.entity.PayOutUserInfo;
import config.ThreadConfig;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class RequestPayoutPageTask {

  private static Logger logger = LoggerFactory.getLogger(RequestPayoutPageTask.class);
  private static String URL = HostConfig.HOST + "cashouts";
  private static int tryTime = 20;

  public static PayOutPageData execute(String walletName) {
    try {
      String response = HttpUtils
          .get(CrawlMeta.getNewInstance(RequestPayoutPageTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
//      logger.info(doc.toString());
      PayOutPageData data = new PayOutPageData(doc, walletName);
      if (!data.isActive() && tryTime > 0) {
        tryTime--;
        return execute(walletName);
      }
      return data;
    } catch (IOException e) {
      return new PayOutPageData(null, walletName);
    }

  }
}
