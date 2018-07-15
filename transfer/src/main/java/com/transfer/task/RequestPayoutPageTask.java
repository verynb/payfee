package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.transfer.entity.AddBitAccountParam;
import com.transfer.entity.PayOutPageData;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.SendMailResult;
import com.transfer.mailClient.FilterMailUtil;
import com.transfer.mailClient.MailTokenData;
import config.ThreadConfig;
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

  private static PayOutPageData execute(String walletName) {
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(RequestPayoutPageTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
      return new PayOutPageData(doc, walletName);
    } catch (Exception e) {
      logger.info("获取到转账页面请求异常-" + e.getMessage());
      return new PayOutPageData(null, walletName);
    } finally {
      response.getHttpGet().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

  public static PayOutPageData tryTimes(PayOutUserInfo userInfo, ThreadConfig config, String walletName) {
    logger.info("开始抓取转账页面数据");
    for (int i = 1; i <= config.getTransferErrorTimes() + 2; i++) {
      PayOutPageData code = execute(walletName);
      if (code.isActive()) {
        return code;
      } else {
        try {
          if (StringUtils.isBlank(code.getUserAccountId())) {
            SendMailResult sendMailResult = SendMailTask
                .tryExcute(code.getAddBitToken(), "", config.getThreadspaceTime(),
                    FilterMailUtil.TOKEN_TYPE_ADD_BITCOIN_ACCOUNT);
            if (sendMailResult.isActive()) {
              long mailSpace = RandomUtil.ranNum(config.getMailSpaceTime()) * 100 + 10000;
              logger.info(
                  "休眠" + mailSpace + "ms后读取邮件");
              Thread.sleep(mailSpace);
              List<MailTokenData> tokenData = FilterMailUtil.filterAddMails(userInfo.getAccount(),
                  userInfo.getMailbox(),
                  userInfo.getMailboxPassword(),
                  config.getTransferErrorTimes(), config.getMailSpaceTime());
              if (CollectionUtils.isNotEmpty(tokenData)) {
                AddBitAccountTask.execute(
                    new AddBitAccountParam(code.getAddBitToken(), userInfo.getWalletName(), userInfo.getWalletAddress(),
                        tokenData.get(0).getToken()));
              }
            }
          }
          logger.info(code.toString());
          Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        } catch (InterruptedException e) {
        }
        logger.info("获取登录页面请求重试，剩余" + (config.getTransferErrorTimes() + 2 - i) + "次");
      }
    }
    return new PayOutPageData(null, walletName);
  }

}
