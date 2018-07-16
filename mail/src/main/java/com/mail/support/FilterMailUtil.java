package com.mail.support;

import com.bit.network.RandomUtil;
import com.mail.api.MailTokenData;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
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

  private static SearchTerm buildSearchTerm() throws UnsupportedEncodingException {

    SearchTerm sentDateTerm = new SentDateTerm(ComparisonTerm.EQ, new Date(1531738540000L));
    SearchTerm ft =
        new FlagTerm(new Flags(Flag.SEEN), false);

    SearchTerm[] searchTerms = new SearchTerm[]{
        sentDateTerm, ft
    };
    SearchTerm comparisonAndTerm = new AndTerm(searchTerms);
    return comparisonAndTerm;
  }

  public static List<MailTokenData> filterTransferMails(String userName, String mail,
      String password, int tryTimes,
      int space) throws InterruptedException, UnsupportedEncodingException {
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, buildSearchTerm(), TOKEN_TRANSFER);
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
      throws InterruptedException, UnsupportedEncodingException {
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, buildSearchTerm(), TOKEN_REQUEST_PAYOUT);
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

  public static List<MailTokenData> filterAddMails(String userName, String mail, String password, int tryTimes,
      int space)
      throws InterruptedException, UnsupportedEncodingException {
    for (int i = 1; i <= tryTimes; i++) {
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, buildSearchTerm(), TOKEN_ADD_BITCOIN_ACCOUNT);
      ;
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
    try {
      try {
        System.out
            .println(FilterMailUtil.filterTransferMails("yuanjiang123", "foshan001@aliyun.com", "liumeichen123", 1, 1));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
   /* System.out.println(FilterMailUtil.filterRequestMails("yuanjiang123", "foshan003@aliyun.com", "liumeichen123"));
    System.out.println(FilterMailUtil.filterAddMails("yuanjiang123", "foshan002@aliyun.com", "liumeichen123"));
    System.out
        .println(FilterMailUtil.filterAddMails("yuanjiang123", "m18981947043@163.com", "yuanjiang123"));
  }*/
  }

}
