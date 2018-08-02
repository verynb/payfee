package com.mail.support;

import com.google.common.collect.Lists;
import com.mail.api.ImapStoreFactory;
import com.mail.api.MailTokenData;
import com.mail.api.ReceiveEmail;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/11/30.
 */
@Slf4j
public class ImapMailToken {

  private static Logger logger = LoggerFactory.getLogger(ImapMailToken.class);

  public static List<MailTokenData> filterMailsForIsNew(String userName, String mail,
      String password, SearchTerm searchTerm, String tokenType) {
    Store store = ImapStoreFactory.getStore(mail);
    Folder folder = null;
    Folder rush = null;
    List<MailTokenData> dataList = Lists.newArrayList();
    try {
      logger.info("开始连接邮件服务器");
      store.connect(mail, password);
      folder = store.getFolder("INBOX");
      rush = store.getFolder("垃圾邮件");
      folder.open(Folder.READ_WRITE);
      rush.open(Folder.READ_WRITE);
      logger.info("打开邮箱成功");
      List<Message> inboxMessages = Lists.newArrayList(Arrays.asList(folder.search(searchTerm)));
      List<Message> inboxMessagesRush = Lists.newArrayList(Arrays.asList(rush.search(searchTerm)));
      inboxMessages.addAll(inboxMessagesRush);
      MailTokenData data = inboxMessages.stream()
          .map(i -> new ReceiveEmail((MimeMessage) i))
          .filter(re -> re.filterSubject(tokenType))
          .filter(re -> re.filterSendUser(userName))
          .map(re -> re.mailTokenData())
          .findFirst().orElse(null);
      if (data == null) {
        return dataList;
      } else {
        dataList.add(data);
      }
    } catch (Exception e) {
      return dataList;
    } finally {
      try {
        folder.close(true);
        store.close();
      } catch (Exception e) {
        logger.info(e.getMessage());
        return dataList;
      }
      return dataList;
    }
  }

  public static void init(String mail, String password) {
    Store store = ImapStoreFactory.getStore(mail);
    Folder folder = null;
    Folder rush = null;
    try {
      logger.info("开始连接邮件服务器");
      store.connect(mail, password);
      folder = store.getFolder("INBOX");
      rush = store.getFolder("垃圾邮件");
      folder.open(Folder.READ_WRITE);
      rush.open(Folder.READ_WRITE);
      List<Message> inboxMessages = Lists.newArrayList(Arrays.asList(folder.search(buildSearchTerm())));
      List<Message> inboxMessagesRush = Lists.newArrayList(Arrays.asList(rush.search(buildSearchTerm())));
      inboxMessages.addAll(inboxMessagesRush);
      inboxMessages.forEach(i -> {
        try {
          i.setFlag(Flag.SEEN, true);
        } catch (MessagingException e) {
          e.printStackTrace();
        }
      });
      logger.info("全部置为已读");
    } catch (Exception e) {
    } finally {
      try {
        folder.close(true);
        store.close();
      } catch (Exception e) {
        logger.info(e.getMessage());
      }
    }
  }

  private static SearchTerm buildSearchTerm() {

    SearchTerm ft =
        new FlagTerm(new Flags(Flag.SEEN), false);
    SearchTerm[] searchTerms = new SearchTerm[]{
        ft
    };
    SearchTerm comparisonAndTerm = new AndTerm(searchTerms);
    return comparisonAndTerm;
  }

}
