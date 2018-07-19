package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.AddBitAccountParam;
import com.transfer.entity.PayOutParam;
import com.transfer.entity.PayOutResult;
import java.io.IOException;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class AddBitAccountTask {

  private static Logger logger = LoggerFactory.getLogger(AddBitAccountTask.class);
  private static String URL = HostConfig.HOST + "user_accounts";
  private static String referer = HostConfig.HOST + "cashouts";

  private static Map getParam(AddBitAccountParam param) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("authenticity_token", param.getAuthenticityToken());
    paramMap.put("user_account[account_name]", param.getAccountName());
    paramMap
        .put("user_account[bitcoin_address]", param.getBitcoinAddress());
    paramMap
        .put("user_account[token]", param.getToken());
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
    headMap.put("referer", referer);
    headMap.put("origin", HostConfig.HOST);
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }


  public static PayOutResult execute(AddBitAccountParam param){
    try{
      String response = HttpUtils
          .post(CrawlMeta.getNewInstance(AddBitAccountTask.class, URL), new CrawlHttpConf(getParam(param), getHeader()));
      logger.info("添加火币服务返回:" + response);
      if (response.contains("invalid_token") || response.contains("invalid_transfer")) {
        logger.info("火币token:" + param.getToken() + "不正确");
        return new PayOutResult("error", "invalid_token");
      } else if (response.contains("success")) {
        logger.info("添加火币成功");
        return GsonUtil.jsonToObject(response, PayOutResult.class);
      } else {
        logger.info("未知错误");
        return new PayOutResult("error", "unkown");
      }
    }catch (IOException e){
      return new PayOutResult("error", "unkown");
    }

  }
}
