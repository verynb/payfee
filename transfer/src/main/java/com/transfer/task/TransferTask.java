package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.TransferParam;
import com.transfer.entity.TransferResult;
import com.quick.hui.crawler.core.utils.GsonUtil;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class TransferTask {

  private static Logger logger = LoggerFactory.getLogger(TransferTask.class);
  private static String URL = "https://www.bitbackoffice.com/transfers";

  public static Map getParam(TransferParam param) {
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
   /* result.getHttpConf().getRequestHeaders().put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
    result.getHttpConf().getRequestHeaders().put("referer", URL);
    result.getHttpConf().getRequestHeaders().put("origin", "https://www.bitbackoffice.com");
    result.getHttpConf().getRequestHeaders().put("x-requested-with", "XMLHttpRequest");*/
    return paramMap;
  }

  public static TransferResult execute(TransferParam param) {
    HttpPostResult response = null;
    try {
      response = HttpUtils
          .doPost(CrawlMeta.getNewInstance(TransferTask.class, URL), new CrawlHttpConf(getParam(param)));
      String returnStr = EntityUtils.toString(response.getResponse().getEntity());
      logger.info("转账服务器返回:" + returnStr);
      if (returnStr.contains("invalid_token") || returnStr.contains("invalid_transfer")) {
        logger.info("转账token:" + param.getToken() + "不正确");
        return new TransferResult("error", "invalid_token");
      } else if (returnStr.contains("success")) {
        logger.info("转账成功");
        return GsonUtil.jsonToObject(returnStr, TransferResult.class);
      } else {
        logger.info("未知错误");
        return new TransferResult("error", "unkown");
      }
    } catch (Exception e) {
      logger.info("转账请求异常:" + e.getMessage());
      return new TransferResult("error", "500");
    } finally {
      response.getHttpPost().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
      logger.info("释放连接");
    }
  }
}
