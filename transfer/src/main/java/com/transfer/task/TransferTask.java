package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.TransferParam;
import com.transfer.entity.TransferResult;
import com.transfer.load.TransferUserFilterUtil;
import java.io.IOException;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class TransferTask {

  private static Logger logger = LoggerFactory.getLogger(TransferTask.class);
  private static String URL = HostConfig.HOST + "transfers";

  private static Map getParam(TransferParam param) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("authenticity_token", param.getAuthenticityToken());
    paramMap.put("transfer_to", param.getTransferTo());
    paramMap
        .put("partition_transfer_partition[user_wallet_id]", param.getUserWalletId());
    paramMap.put("partition_transfer_partition[amount]", String.valueOf(param.getAmount()));

    paramMap
        .put("partition_transfer_partition[token]", param.getToken());
    paramMap
        .put("partition_transfer_partition[user_id]", param.getUserId());
    paramMap
        .put("partition_transfer_partition[receiver_id]", param.getReceiverId());
    paramMap
        .put("partition_transfer_partition[receiver_wallet_id]", "");
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
    headMap.put("referer", URL);
    headMap.put("origin", "https://www.bitbackoffice.com");
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }


  public static TransferResult execute(TransferParam param, int row) {
    try {
      String response = HttpUtils
          .post(CrawlMeta.getNewInstance(TransferTask.class, URL), new CrawlHttpConf(getParam(param), getHeader()));
      logger.info("转账服务器返回:" + response);
      if (response.contains("invalid_token") || response.contains("invalid_transfer")) {
        logger.info("转账token:" + param.getToken() + "不正确");
        TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "转账token:" + param.getToken() + "不正确");
      } else if (response.contains("success")) {
        logger.info("转账成功");
        TransferUserFilterUtil.filterAndUpdateFlag(row, "1", "转账成功");
        return GsonUtil.jsonToObject(response, TransferResult.class);
      } else {
        logger.info("未知错误");
        TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "未知错误");
      }
    } catch (IOException e) {
      TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "网络错误");
    }
    return new TransferResult("false", "false");
  }
}
