package com.transfer.mailClient;

import com.google.common.collect.Lists;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
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
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * Created by Administrator on 2017/11/30.
 */
public class MailToken {

  private static final String PROTOCOL = "pop3";
  private static final String PORT = "110";
  private static final String HOST = "pop3.aliyun.com";

  private static Store getStore(String mail, String password) throws MessagingException {
    Properties props = new Properties();
    props.setProperty("mail.store.protocol", PROTOCOL);       // 协议
    props.setProperty("mail.pop3.port", PORT);             // 端口
    props.setProperty("mail.pop3.host", HOST);    // pop3服务器
    props.setProperty("mail.pop3.auth", "true");// 指定验证为true
    props.setProperty("mail.debug", "true");
    Session session = Session.getInstance(props);
    Store store = session.getStore("pop3");
    store.connect(mail, password);
    return store;
  }

  private static SearchTerm buildSearchTerm() throws UnsupportedEncodingException {

    DateTime dateTime=new DateTime(1531238140000L);

    SearchTerm sentDateTerm = new ReceivedDateTerm(ComparisonTerm.NE, new Date(dateTime.millisOfDay().withMinimumValue().getMillis()));
    SearchTerm sentDateTermEnd = new ReceivedDateTerm(ComparisonTerm.LT, new Date(1531238140000L));
    SearchTerm ft =
        new FlagTerm(new Flags(Flag.SEEN), false);
    SearchTerm subject = new SubjectTerm("Token for your TRANSFER");
    SearchTerm[] searchTerms = new SearchTerm[]{
        subject, ft,sentDateTerm
    };
    SearchTerm comparisonAndTerm = new AndTerm(searchTerms);
    return comparisonAndTerm;
  }

  public static List<MailTokenData> filterMails(String mail, String password) {
    Store store = null;
    Folder folder = null;
    List<MailTokenData> dataList = Lists.newArrayList();
    try {
      store = getStore(mail, password);
      folder = store.getFolder("INBOX");
      folder.open(Folder.READ_ONLY);
      Message message[] = folder.search(buildSearchTerm());
      for (int i = 0; i < message.length; i++) {
        ReceiveEmail re = new ReceiveEmail((MimeMessage) message[i]);
        if (StringUtils.isBlank(re.getSubject()) || !re.getSubject()
            .equals("Token for your TRANSFER")) {
          continue;
        }
        re.getMailContent((Part) message[i]);
        dataList.add(new MailTokenData(re.getToken(), re.getSentDate()));
      }
      dataList = dataList
          .stream()
          .sorted(Comparator.comparing(MailTokenData::getDate).reversed())
          .collect(Collectors.toList());
    } catch (Exception e) {
      return dataList;
    } finally {
      try {
        folder.close(true);
        store.close();
      } catch (MessagingException e) {

      }
      return dataList;
    }
  }

  public static boolean isreceived(List<MailTokenData> datas, long startTime) {
    if (CollectionUtils.isEmpty(datas)) {
      return false;
    }
    MailTokenData data = datas.get(0);
    if (data.getDate() >= startTime) {
      return true;
    } else {
      return false;
    }
  }

  public static void main(String[] args) {
    System.out.println(MailToken.filterMails("foshan001@aliyun.com", "liumeichen123"));
  }
}
