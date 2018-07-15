package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class CancelTokenTask {

  private static Logger logger = LoggerFactory.getLogger(CancelTokenTask.class);
  private static String URL = HostConfig.HOST+"tokens/cancel";

  private static Map getParam(String type) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("token_type", type);
    return paramMap;
  }

  private static Map<String, String> getHeader() {
    Map<String, String> map = Maps.newHashMap();
    map.put("x-requested-with", "XMLHttpRequest");
    return map;
  }

  public static String execute(String type) {
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(CancelTokenTask.class, URL),
              new CrawlHttpConf(getParam(type), getHeader()));
      String jsonData = EntityUtils.toString(response.getResponse().getEntity());
      logger.info("取消转账token返回信息=" + jsonData);
      return jsonData;
    } catch (Exception e) {
      logger.info("取消转账token失败" + e.getMessage());
    } finally {
      response.getHttpGet().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
    return "unkonw";
  }
}
