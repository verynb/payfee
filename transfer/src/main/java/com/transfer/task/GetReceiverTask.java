package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.UserInfo;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class GetReceiverTask {

  private static Logger logger = LoggerFactory.getLogger(GetReceiverTask.class);
  private static String URL = HostConfig.HOST+"users/is_down_line_binary";


  private static Map getParam(String userName) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("user", userName);
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }

  public static UserInfo execute(String userName) {
    logger.info("开始获取转出账户[" + userName + "]信息");
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(GetReceiverTask.class, URL),
              new CrawlHttpConf(getParam(userName), getHeader()));
      String jsonData = EntityUtils.toString(response.getResponse().getEntity());
//      logger.info("获取转账人信息=" + jsonData);
      UserInfo userInfo = GsonUtil.jsonToObject(jsonData, UserInfo.class);
      if (!userInfo.getResponse()) {
        logger.info("转账人[" + userName + "]不存在或者不存在于您的二进制树中");
      }
      return userInfo;
    } catch (Exception e) {
      logger.info("获取转账人信息失败" + e.getMessage());
      return new UserInfo("", "", false);
    } finally {
      response.getHttpGet().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }
}
