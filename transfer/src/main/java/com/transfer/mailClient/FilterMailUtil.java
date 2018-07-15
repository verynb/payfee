package com.transfer.mailClient;

import java.util.List;

/**
 * Created by yuanj on 2018/7/15.
 */
public class FilterMailUtil {

  public static final String TOKEN_TYPE_TRANSFER="transfer";
  public static final String TOKEN_TYPE_REQUEST_PAYOUT="request_payout";
  public static final String TOKEN_TYPE_ADD_BITCOIN_ACCOUNT="add_bitcoin_account";
  private static final String TOKEN_TRANSFER = "Token for your TRANSFER";//收分
  private static final String TOKEN_REQUEST_PAYOUT = "Token for your REQUEST PAYOUT";//体现
  private static final String TOKEN_ADD_BITCOIN_ACCOUNT = "Token for your ADD BITCOIN ACCOUNT";//加比特币账户


  public static List<MailTokenData> filterTransferMails(String userName, String mail, String password) {
    return ImapMailToken.filterMailsForIsNew(userName, mail, password, TOKEN_TRANSFER);
  }

  public static List<MailTokenData> filterRequestMails(String userName, String mail, String password) {
    return ImapMailToken.filterMailsForIsNew(userName, mail, password, TOKEN_REQUEST_PAYOUT);
  }

  public static List<MailTokenData> filterAddMails(String userName, String mail, String password) {
    return ImapMailToken.filterMailsForIsNew(userName, mail, password, TOKEN_ADD_BITCOIN_ACCOUNT);
  }

  public static void main(String[] args) {
    System.out.println(FilterMailUtil.filterTransferMails("yuanjiang123", "foshan001@aliyun.com", "liumeichen123"));
    System.out.println(FilterMailUtil.filterRequestMails("yuanjiang123", "foshan003@aliyun.com", "liumeichen123"));
    System.out.println(FilterMailUtil.filterAddMails("yuanjiang123", "foshan002@aliyun.com", "liumeichen123"));
    System.out
        .println(FilterMailUtil.filterAddMails("yuanjiang123", "m18981947043@163.com", "yuanjiang123"));
  }

}
