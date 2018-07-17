package com.mail.support;

import com.google.common.collect.Lists;
import com.mail.api.ImapStoreFactory;
import com.mail.api.MailTokenData;
import com.mail.api.ReceiveEmail;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
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
      String password, SearchTerm searchTerm) {
    Store store = ImapStoreFactory.getStore(mail);
    Folder folder = null;
    List<MailTokenData> dataList = Lists.newArrayList();
    try {
      store.connect(mail, password);
      folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);
      List<Message> inboxMessages = Lists.newArrayList(Arrays.asList(folder.search(searchTerm)));
      dataList = inboxMessages.stream()
          .map(i -> new ReceiveEmail((MimeMessage) i))
          .filter(re -> re.filterSendUser(userName))
          .map(re -> new MailTokenData(re.getToken(), re.getSentDate()))
          .sorted(Comparator.comparing(MailTokenData::getDate).reversed())
          .collect(Collectors.toList());
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

}
