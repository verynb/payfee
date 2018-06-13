package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.SendMailResult;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class SendMailTask {

  private static Logger logger = LoggerFactory.getLogger(SendMailTask.class);
  private static String URL = "https://www.bitbackoffice.com/tokens";

  private static Map getParam(String token, String userId) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("authenticity_token", token);
    paramMap.put("token[user_id]", userId);
    paramMap.put("token[token_type]", "transfer");
//    result.getHttpConf().getRequestHeaders().put("x-requested-with", "XMLHttpRequest");
    return paramMap;
  }

  public static SendMailResult tryExcute(String token, String userId, long space) {
    SendMailResult result = execute(token, userId);
    if (result == null || result.getError().equals("number_exceeded")) {
      logger.info("token无效，开始取消token");
      String cancelStr = CancelTokenTask.execute();
      if (cancelStr.contains("success")) {
        logger.info("取消已有token成功");
        try {
          Thread.sleep(space);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        result = tryExcute(token, userId, space);
      } else {
        logger.info("取消已有token失败=" + cancelStr);
      }
    }
    return result;
  }

  private static SendMailResult execute(String token, String userId) {
    HttpPostResult response = null;
    try {
      response = HttpUtils
          .doPost(CrawlMeta.getNewInstance(SendMailTask.class, URL), new CrawlHttpConf(getParam(token, userId)));
      String returnStr = EntityUtils.toString(response.getResponse().getEntity());
      logger.info("发送邮件服务器返回值-" + returnStr);
      if (returnStr.contains("number_exceeded")) {
        logger.info("拒绝发送邮件，有未使用的邮件");
        return new SendMailResult("success", "number_exceeded");
      } else {
        logger.info("发送邮件成功");
        return GsonUtil.jsonToObject(returnStr, SendMailResult.class);
      }
    } catch (Exception e) {
      logger.info("发送邮件请求异常-" + e.getMessage());
      return null;
    } finally {
      response.getHttpPost().releaseConnection();
      response.getHttpClient().getConnectionManager().shutdown();
    }
  }

}
