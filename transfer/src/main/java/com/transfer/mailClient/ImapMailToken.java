package com.transfer.mailClient;

import com.google.common.collect.Lists;
import com.sun.mail.imap.IMAPFolder;
import com.transfer.job.TransferCrawlJob;
import identity.TimeCheck;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/11/30.
 */
public class ImapMailToken {

  private static final String PROTOCOL = "imap";
  private static final String PORT = "143";
  //    private static final String HOST = "imap.mxhichina.com";//阿里云企业邮箱
//  private static final String HOST = "imap.163.com";//163邮箱
private static final String HOST = "imap.aliyun.com";//阿里云个人邮箱
  private static Logger logger = LoggerFactory.getLogger(ImapMailToken.class);

  private static Store getStore(String mail, String password) throws MessagingException {
    Properties props = new Properties();
    props.setProperty("mail.imap.protocol", PROTOCOL);       // 协议
    props.setProperty("mail.imap.port", PORT);             // 端口
    props.setProperty("mail.imap.host", HOST);    // imap服务器
    props.put("mail.imap.auth.plain.disable", "true");
//    props.setProperty("mail.debug", "true");
    Session session = Session.getInstance(props);
    Store store = session.getStore("imap");
    store.connect(mail, password);
    return store;
  }


  public static List<MailTokenData> filterMailsForIsNew(String userName, String mail, String password) {
    Store store = null;
    IMAPFolder folder = null;
    IMAPFolder rubbishFolder = null;
    List<MailTokenData> dataList = Lists.newArrayList();
    try {
      logger.info("开始连接邮件服务器");
      store = getStore(mail, password);
      logger.info("连接邮件服务器成功");
      rubbishFolder = (IMAPFolder) store.getFolder("垃圾邮件");
      folder = (IMAPFolder) store.getFolder("INBOX");
      rubbishFolder.open(Folder.READ_WRITE);
      folder.open(Folder.READ_WRITE);
      logger.info("开始读取收件箱");
      SearchTerm comparisonTermeq = new SentDateTerm(ComparisonTerm.EQ, new Date());
      FlagTerm ft =
          new FlagTerm(new Flags(Flags.Flag.SEEN), false);
      SearchTerm comparisonAndTerm = new AndTerm(comparisonTermeq, ft);
      List<Message> inboxMessages = new ArrayList<Message>(Arrays.asList(folder.search(comparisonAndTerm)));
      logger.info("读取收件箱成功");
      logger.info("开始读取垃圾邮件");
      List<Message> rubbishMessages = new ArrayList<Message>(Arrays.asList(rubbishFolder.search(comparisonAndTerm)));
      logger.info("读取垃圾成功");
      inboxMessages.addAll(rubbishMessages);
      for (int i = 0; i < inboxMessages.size(); i++) {
        ReceiveEmail re = new ReceiveEmail((MimeMessage) inboxMessages.get(i));
        if (StringUtils.isBlank(re.getSubject()) || !re.getSubject()
            .equals("Token for your TRANSFER")) {
          logger.info("NOT Token");
          continue;
        }
        if (!re.isNew()) {
          logger.info("NOT NEW");
          re.delete();
          continue;
        }

        if (!TimeCheck.isCurrentDay(re.getSentDate())) {
          logger.info("NOT CURRENT DAY");
          re.delete();
          continue;
        }
        re.getMailContent((Part) inboxMessages.get(i));
        if (!re.getSendUser(userName.length()).equals(userName)) {
          re.getMimeMessage().setFlag(Flags.Flag.SEEN, false);
          logger.info("NOT CURRENT USER SEND");
          continue;
        }
        dataList.add(new MailTokenData(re.getToken(), re.getSentDate()));
      }
      dataList = dataList
          .stream()
          .sorted(Comparator.comparing(MailTokenData::getDate).reversed())
          .collect(Collectors.toList());
      logger.info("读取邮件完成");
    } catch (Exception e) {
      return dataList;
    } finally {
      try {
        folder.close(true);
        store.close();
      } catch (MessagingException e) {
        logger.info(e.getMessage());
      }
      return dataList;
    }
  }

  public static void main(String[] args) {
    /*for(int i=0;i<100;i++){
      new Thread(() ->{
        ImapMailToken.filterMailsForIsNew("yuanjiang123", "foshan001@aliyun.com", "liumeichen123");
      }).start();
    }*/
    System.out.println(ImapMailToken.filterMailsForIsNew("yuanjiang123", "foshan001@aliyun.com", "liumeichen123"));
    System.out.println(ImapMailToken.filterMailsForIsNew("yuanjiang123", "foshan003@aliyun.com", "liumeichen123"));
    System.out.println(ImapMailToken.filterMailsForIsNew("yuanjiang123", "foshan002@aliyun.com", "liumeichen123"));
    System.out.println(ImapMailToken.filterMailsForIsNew("yuanjiang123", "yuanjiangfreedom@aliyun.com", "yuanjiang123"));
  }
}
