package com.mail.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.GsonUtil;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.mail.api.SendMailResult;
import com.mail.support.FilterMailUtil;
import java.io.IOException;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class SendMailTask {

  private static Logger logger = LoggerFactory.getLogger(SendMailTask.class);
  private static String URL = HostConfig.HOST + "tokens";
  private static String PAY_OT_URL = HostConfig.HOST + "cashouts";
  private static int tryTimes = 10;

  private static Map getParam(String token, String userId, String type) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("authenticity_token", token);
    paramMap.put("token[user_id]", userId);
    paramMap.put("token[token_type]", type);
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("x-requested-with", "XMLHttpRequest");
    return headerMap;
  }

  private static String freshToken(String type, String token) throws IOException {
    logger.info("tokenTye=[" + type + "]");
    if (type.equals(FilterMailUtil.TOKEN_TYPE_REQUEST_PAYOUT) || type
        .equals(FilterMailUtil.TOKEN_TYPE_ADD_BITCOIN_ACCOUNT)) {
      String response = HttpUtils
          .get(CrawlMeta.getNewInstance(SendMailTask.class, PAY_OT_URL), new CrawlHttpConf());
      Document doc = Jsoup.parse(response);
      Element authTokenElement = doc.select("form[id=new_partition_cashout_partition]")
          .select("input[name=authenticity_token]").first();

      Element addBitElement = doc.select("form[id=new_user_account]")
          .select("input[name=authenticity_token]").first();
      if (type.equals(FilterMailUtil.TOKEN_TYPE_REQUEST_PAYOUT)) {
        String authToken = authTokenElement.val();
        logger.info("刷新后request_payout_token=[" + authToken + "]");
        return authToken;
      } else if (type.equals(FilterMailUtil.TOKEN_TYPE_ADD_BITCOIN_ACCOUNT)) {
        String addBitToken = addBitElement.val();
        logger.info("add_bitcoin_account_token=[" + addBitToken + "]");
        return addBitToken;
      }
    }
    return token;
  }


  public static  SendMailResult tryExcute(String token, String userId, long space, String type) {
    try {
      logger.info("开始发送邮件[" + userId + "]");
      SendMailResult result = execute(token, userId, type);
      if (result == null || result.getError().equals("number_exceeded")) {
        logger.info("token无效，开始取消token");
        String cancelStr = CancelTokenTask.execute(type);
        if (cancelStr.contains("success")) {
          logger.info("取消已有token成功");
          try {
            Thread.sleep(space);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          result = tryExcute(freshToken(type, token), userId, space, type);
        } else {
          logger.info("取消已有token失败=" + cancelStr);
        }
      }
      if (!result.isActive() && tryTimes > 0) {
        tryTimes--;
        return tryExcute(freshToken(type, token), userId, space, type);
      }
      return result;
    } catch (IOException e) {
      return new SendMailResult("f", "false");
    }

  }

  private static SendMailResult execute(String token, String userId, String type) throws IOException {
    String response = HttpUtils
        .post(CrawlMeta.getNewInstance(SendMailTask.class, URL),
            new CrawlHttpConf(getParam(token, userId, type), getHeader()));
    logger.info("发送邮件服务器返回值-" + response);
    if (response.contains("number_exceeded")) {
      logger.info("拒绝发送邮件，有未使用的邮件");
      return new SendMailResult("success", "number_exceeded");
    } else {
      logger.info("发送邮件成功");
      return GsonUtil.jsonToObject(response, SendMailResult.class);
    }
  }

}
