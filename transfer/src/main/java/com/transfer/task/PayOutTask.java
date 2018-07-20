package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.PayOutParam;
import com.transfer.entity.PayOutResult;
import com.transfer.load.PayOutUserFilterUtil;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class PayOutTask {

  private static Logger logger = LoggerFactory.getLogger(PayOutTask.class);
  private static String URL = HostConfig.HOST + "cashouts";

  private static Map getParam(PayOutParam param) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("authenticity_token", param.getAuthenticityToken());
    paramMap.put("partition_cashout_partition[user_account_id]", param.getUserAccountId());
    paramMap
        .put("partition_cashout_partition[user_wallet_id]", param.getUserWalletId());

    paramMap.put("partition_cashout_partition[amount]", String.valueOf(param.getAmount()));

    paramMap
        .put("partition_cashout_partition[token]", param.getToken());
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
    headMap.put("referer", URL);
    headMap.put("origin", HostConfig.HOST);
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }


  public static PayOutResult execute(PayOutParam param,int index) {
    try {
      String response = HttpUtils
          .post(CrawlMeta.getNewInstance(PayOutTask.class, URL), new CrawlHttpConf(getParam(param), getHeader()));
      logger.info("提现服务器返回:" + response);
      if (response.contains("invalid_token") || response.contains("invalid_transfer")) {
        logger.info("提现token:" + param.getToken() + "不正确");
        PayOutUserFilterUtil.filterAndUpdateFlag(index, "0", "提现token:" + param.getToken() + "不正确");
        return new PayOutResult("error", "invalid_token");
      } else if (response.contains("success")) {
        logger.info("提现成功");
        PayOutUserFilterUtil.filterAndUpdateFlag(index, "1", "提现成功");
        return GsonUtil.jsonToObject(response, PayOutResult.class);
      } else {
        logger.info("response");
        PayOutUserFilterUtil.filterAndUpdateFlag(index, "0", GsonUtil.jsonToObject(response, PayOutResult.class).getError());
        return new PayOutResult("error", "unkown");
      }
    } catch (IOException e) {
      logger.info("提现请求异常:" + e.getMessage());
      PayOutUserFilterUtil.filterAndUpdateFlag(index, "0", "网络异常");
      return new PayOutResult("error", "500");
    }
  }
}
