package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpResult;
import com.bit.network.HttpUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class CancelTokenTask {

  private static Logger logger = LoggerFactory.getLogger(CancelTokenTask.class);
  private static String URL = "https://www.bitbackoffice.com/tokens/cancel?token_type=transfer";

  /*private static CrawJobResult buildTask() {
    Set<String> selectRule = new HashSet<>();
    CrawlMeta crawlMeta = new CrawlMeta(URL, selectRule);
    CrawJobResult result = new CrawJobResult();
    result.setCrawlMeta(crawlMeta);
    result.getHttpConf().setMethod(HttpMethod.GET);
//    result.getHttpConf().getRequestParams().put("user", userName);
    result.getHttpConf().getRequestHeaders().put("x-requested-with", "XMLHttpRequest");
    return result;
  }*/
  public static String execute() {
    HttpResult response = null;
    try {
      response = HttpUtils
          .doGet(CrawlMeta.getNewInstance(CancelTokenTask.class, URL), new CrawlHttpConf());
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
