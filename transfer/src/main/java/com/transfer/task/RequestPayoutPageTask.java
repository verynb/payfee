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

  public static PayOutPageData execute(String walletName) {
    try {
      String response = HttpUtils
          .get(CrawlMeta.getNewInstance(RequestPayoutPageTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      return new PayOutPageData(doc, walletName);
    } catch (IOException e) {
      return new PayOutPageData(null, walletName);
    }

  }

  public static PayOutPageData tryTimes(PayOutUserInfo userInfo, ThreadConfig config, String walletName) {
    logger.info("开始抓提现页面");
    for (int i = 1; i <= config.getTransferErrorTimes() + 2; i++) {
      PayOutPageData code = execute(walletName);
      if (code.isActive()) {
        return code;
      } else {
        try {
          if (StringUtils.isBlank(code.getUserAccountId())) {
            logger.info("添加火币地址");
            List<MailTokenData> tokenData = FilterMailUtil
                .filterAddMails(code.getAddBitToken(), "", userInfo.getAccount(),
                    userInfo.getMailbox(),
                    userInfo.getMailboxPassword(),
                    config.getTransferErrorTimes(), config.getMailSpaceTime());
            if (CollectionUtils.isNotEmpty(tokenData)) {
              AddBitAccountTask.execute(
                  new AddBitAccountParam(code.getAddBitToken(), userInfo.getWalletName(), userInfo.getWalletAddress(),
                      tokenData.get(0).getToken()));
            }
          }
          logger.info(code.toString());
          Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        } catch (InterruptedException e) {
          return new PayOutPageData(null, walletName);
        }
        logger.info("提现页面请求重试，剩余" + (config.getTransferErrorTimes() + 2 - i) + "次");
      }
    }
    return new PayOutPageData(null, walletName);
  }

}
