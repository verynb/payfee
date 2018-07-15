package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.transfer.entity.PayOutPageData;
import com.transfer.entity.TransferPageData;
import config.ThreadConfig;
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

  private static PayOutPageData execute(String accountName) {
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(RequestPayoutPageTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
      return new PayOutPageData(doc,accountName);
    } catch (Exception e) {
      logger.info("获取到转账页面请求异常-" + e.getMessage());
      return new PayOutPageData(null,accountName);
    } finally {
      response.getHttpGet().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

  public static PayOutPageData tryTimes(ThreadConfig config,String accountName) {
    logger.info("开始抓取转账页面数据");
    for (int i = 1; i <= config.getTransferErrorTimes() + 2; i++) {
      PayOutPageData code = execute(accountName);
      if (code.isActive()) {
        return code;
      } else {
        try {
          logger.info(code.toString());
          Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        } catch (InterruptedException e) {
        }
        logger.info("获取登录页面请求重试，剩余" + (config.getTransferErrorTimes() + 2 - i) + "次");
      }
    }
    return new PayOutPageData(null,accountName);
  }

}
