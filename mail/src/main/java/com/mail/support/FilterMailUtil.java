package com.mail.support;

import com.bit.network.GetNetworkTime;
import com.bit.network.RandomUtil;
import com.google.common.collect.Lists;
import com.mail.api.MailTokenData;
import com.mail.api.SendMailResult;
import com.mail.task.SendMailTask;
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
import javax.mail.search.SubjectTerm;
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

  private static SearchTerm buildSearchTerm(Long startTimeMills, Long end, String tokenType)
      throws UnsupportedEncodingException {

    SearchTerm sentDateStrat = new SentDateTerm(ComparisonTerm.GE, new Date(startTimeMills));
//    SearchTerm sentDateEnd = new SentDateTerm(ComparisonTerm.LE, new Date(end));
    SearchTerm ft =
        new FlagTerm(new Flags(Flag.SEEN), false);
    SearchTerm subject = new SubjectTerm(tokenType);
    SearchTerm[] searchTerms = new SearchTerm[]{
        sentDateStrat, ft, subject
    };
    SearchTerm comparisonAndTerm = new AndTerm(searchTerms);
    return comparisonAndTerm;
  }

  public static List<MailTokenData> filterTransferMails(String sendMailToken, String userId,
      String userName, String mail, String password,
      int tryTimes, int space)
      throws InterruptedException, UnsupportedEncodingException {

    Long startMills = GetNetworkTime.getNetworkDatetime() - 2000*60;//发送时间向前推5s
    SendMailResult sendMailResult = SendMailTask.tryExcute(sendMailToken, userId, space, TOKEN_TYPE_TRANSFER);
    if (!sendMailResult.isActive()) {
      return Lists.newArrayList();
    }

    Long endMills = 2000*60L;
    for (int i = 1; i <= tryTimes; i++) {

      long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 8000;
      logger.info("等待" + tryMailSpace + "ms读取邮件");
      Thread.sleep(tryMailSpace);
      endMills = +tryMailSpace;
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, buildSearchTerm(startMills, endMills, TOKEN_TRANSFER));
      if (CollectionUtils.isNotEmpty(tokenData)) {
        return tokenData;
      } else {
        logger
            .info("读取邮件失败,开始重新读取剩余重试次数" + (tryTimes - i));
      }
    }
    return null;
  }

  public static List<MailTokenData> filterRequestMails(String sendMailToken, String userId,
      String userName, String mail,
      String password, int tryTimes, int space)
      throws InterruptedException, UnsupportedEncodingException {
    Long startMills = GetNetworkTime.getNetworkDatetime() - 5000;//发送时间向前推5s
    SendMailResult sendMailResult = SendMailTask.tryExcute(sendMailToken, userId, space, TOKEN_TYPE_REQUEST_PAYOUT);
    if (!sendMailResult.isActive()) {
      return Lists.newArrayList();
    }
    Long endMills = 0L;
    for (int i = 1; i <= tryTimes; i++) {

      long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 10000;
      logger.info("等待" + tryMailSpace + "ms读取邮件");
      Thread.sleep(tryMailSpace);
      endMills = +tryMailSpace;
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password, buildSearchTerm(startMills, endMills, TOKEN_REQUEST_PAYOUT));
      if (CollectionUtils.isNotEmpty(tokenData)) {
        return tokenData;
      } else {
        logger
            .info("读取邮件失败,开始重新读取剩余重试次数" + (tryTimes - i));
      }
    }
    return null;
  }

  public static List<MailTokenData> filterAddMails(String sendMailToken, String userId,
      String userName, String mail, String password, int tryTimes,
      int space)
      throws InterruptedException, UnsupportedEncodingException {
    Long startMills = GetNetworkTime.getNetworkDatetime() - 5000;//发送时间向前推5s
   /* SendMailResult sendMailResult = SendMailTask
        .tryExcute(sendMailToken, userId, space, TOKEN_TYPE_ADD_BITCOIN_ACCOUNT);
    if (!sendMailResult.isActive()) {
      return Lists.newArrayList();
    }*/
    Long endMills = 0L;
    for (int i = 1; i <= tryTimes; i++) {

      long tryMailSpace = RandomUtil.ranNum(space) * 1000 + 10000;
      logger.info("等待" + tryMailSpace + "ms读取邮件");
      Thread.sleep(tryMailSpace);
      endMills = +tryMailSpace;
      logger.info("开始读取邮件[" + mail + "]");
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userName, mail, password,
              buildSearchTerm(startMills, endMills, TOKEN_ADD_BITCOIN_ACCOUNT));
      if (CollectionUtils.isNotEmpty(tokenData)) {
        return tokenData;
      } else {
        logger
            .info("读取邮件失败,开始重新读取剩余重试次数" + (tryTimes - i));
      }
    }
    return null;
  }

  public static void main(String[] args) {
    try {
      try {
        System.out
            .println(FilterMailUtil.filterAddMails("", "", "Wang@N01",
                "lianghuihua01@bookbitbtc.com", "SHENzen007v", 1, 1));
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
