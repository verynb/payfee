package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.bit.network.RandomUtil;
import com.google.common.collect.Lists;
import com.transfer.entity.TransferPageData;
import com.transfer.entity.TransferWallet;
import config.ThreadConfig;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class TransferPageTask {

  private static Logger logger = LoggerFactory.getLogger(TransferPageTask.class);
  private static String URL = "https://www.bitbackoffice.com/transfers";

  private static TransferPageData execute() {
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(TransferPageTask.class, URL),new CrawlHttpConf());
      Document doc = Jsoup.parse(EntityUtils.toString(response.getResponse().getEntity()));
      Element walletElement = doc.select("select[name=partition_transfer_partition[user_wallet_id]]").first();
      if (Objects.isNull(walletElement)) {
        logger.info("未获取到转账页面数据");
        logger.info("获取转账页面-" + doc.toString());
        return new TransferPageData("", "", Lists.newArrayList());
      }
      List<TransferWallet> transferWallets = walletElement.children().stream()
          .filter(e -> StringUtils.isNotBlank(e.val()))
          .map(e -> {
            String walletId = e.val();
            Double amount = Double.valueOf(e.text().substring(e.text().indexOf("$") + 1, e.text().length()));
            return new TransferWallet(walletId, amount);
          }).collect(Collectors.toList());

      Element authTokenElement = doc.select("input[name=authenticity_token]").first();
      Element transferUserIdElement = doc.select("input[name=partition_transfer_partition[user_id]]").first();
      logger.info("获取到转账页面数据成功,返回数据如下:");
      logger.info("authToken=" + authTokenElement.val());
      logger.info("transferUserId=" + transferUserIdElement.val());
      logger.info("transferWallets=" + transferWallets.toString());
      return new TransferPageData(authTokenElement.val(), transferUserIdElement.val(), transferWallets);
    } catch (Exception e) {
      logger.info("获取到转账页面请求异常-" + e.getMessage());
      return new TransferPageData("", "", Lists.newArrayList());
    } finally {
      response.getHttpGet().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

  public static TransferPageData tryTimes(ThreadConfig config) {
    try {
      Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000 + 5000);
    } catch (InterruptedException e) {
    }
    for (int i = 1; i <= config.getTransferErrorTimes() + 2; i++) {
      TransferPageData code = execute();
      if (CollectionUtils.isNotEmpty(code.getTransferWallets())) {
        return code;
      } else {
        try {
          Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000 + 5000);
        } catch (InterruptedException e) {
        }
        logger.info("获取登录页面请求重试，剩余" + (config.getTransferErrorTimes() + 2 - i) + "次");
      }
    }
    return new TransferPageData("", "", Lists.newArrayList());
  }

}
