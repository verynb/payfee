package com.transfer.mailClient;

import com.bit.network.RandomUtil;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2018/7/15.
 */
public class FilterMailUtil {

  private static Logger logger = LoggerFactory.getLogger(FilterMailUtil.class);
  public static final String TOKEN_TYPE_TRANSFER = "transfer";
  public static final String TOKEN_TYPE_REQUEST_PAYOUT = "request_payout";
  public static final String TOKEN_TYPE_ADD_BITCOIN_ACCOUNT = "add_bitcoin_account";
  private static final String TOKEN_TRANSFER = "Token for your TRANSFER";//收分
  private static final String TOKEN_REQUEST_PAYOUT = "Token for your REQUEST PAYOUT";//体现
  private static final String TOKEN_ADD_BITCOIN_ACCOUNT = "Token for your ADD BITCOIN ACCOUNT";//加比特币账户


  public static List<MailTokenData> filterTransferMails(String userName, String mail, String password, int tryTimes,
      int space)  throws InterruptedException{
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken.filterMailsForIsNew(userName, mail, password, TOKEN_TRANSFER);
      if (CollectionUtils.isEmpty(tokenData)) {
        long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 10000;
        logger
            .info("获取邮件失败,等待" + tryMailSpace + "ms重新获取");
        Thread.sleep(tryMailSpace);
        logger
            .info("重新获取邮件开始剩余重试次数" + (tryTimes - i));
      } else {
        return tokenData;
      }
    }
    return null;
  }

  public static List<MailTokenData> filterRequestMails(String userName, String mail,
      String password, int tryTimes, int space)
      throws InterruptedException {
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, TOKEN_REQUEST_PAYOUT);
      if (CollectionUtils.isEmpty(tokenData)) {
        long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 10000;
        logger
            .info("获取邮件失败,等待" + tryMailSpace + "ms重新获取");
        Thread.sleep(tryMailSpace);
        logger
            .info("重新获取邮件开始剩余重试次数" + (tryTimes - i));
      } else {
        return tokenData;
      }
    }
    return null;
  }

  public static List<MailTokenData> filterAddMails(String userName, String mail, String password,int tryTimes, int space)
      throws InterruptedException {
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken.filterMailsForIsNew(userName, mail, password, TOKEN_ADD_BITCOIN_ACCOUNT);;
      if (CollectionUtils.isEmpty(tokenData)) {
        long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 10000;
        logger
            .info("获取邮件失败,等待" + tryMailSpace + "ms重新获取");
        Thread.sleep(tryMailSpace);
        logger
            .info("重新获取邮件开始剩余重试次数" + (tryTimes - i));
      } else {
        return tokenData;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    /*System.out.println(FilterMailUtil.filterTransferMails("yuanjiang123", "foshan001@aliyun.com", "liumeichen123"));
    System.out.println(FilterMailUtil.filterRequestMails("yuanjiang123", "foshan003@aliyun.com", "liumeichen123"));
    System.out.println(FilterMailUtil.filterAddMails("yuanjiang123", "foshan002@aliyun.com", "liumeichen123"));
    System.out
        .println(FilterMailUtil.filterAddMails("yuanjiang123", "m18981947043@163.com", "yuanjiang123"));
  }*/
  }

}
