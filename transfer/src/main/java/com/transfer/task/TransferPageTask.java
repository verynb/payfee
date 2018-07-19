package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.transfer.entity.TransferPageData;
import com.transfer.load.TransferUserFilterUtil;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class TransferPageTask {

  private static Logger logger = LoggerFactory.getLogger(TransferPageTask.class);
  private static String URL = HostConfig.HOST + "transfers";

  public static TransferPageData execute(int row) {
    try {
      String response = HttpUtils
          .get(CrawlMeta.getNewInstance(TransferPageTask.class, URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      TransferPageData data = new TransferPageData(doc);
      if (!data.isActive()) {
        logger.info("钱包为空" + data.toString());
        TransferUserFilterUtil.filterAndUpdateFlag(row, "1", "钱包为空");
      }
      if (!data.walletAmont()) {
        logger.info("钱包金额为0" + data.toString());
        TransferUserFilterUtil.filterAndUpdateFlag(row, "1", "钱包金额为0");
      }
      return data;
    } catch (IOException e) {
      TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "网络异常");
      return new TransferPageData(null);
    }

  }

}
