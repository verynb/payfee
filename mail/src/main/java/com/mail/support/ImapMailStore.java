package com.mail.support;

import com.google.common.collect.Lists;
import com.mail.api.MailTokenData;
import com.mail.api.ReceiveEmail;
import com.sun.mail.imap.IMAPFolder;
import identity.TimeCheck;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/11/30.
 */
@Slf4j
public class ImapMailStore {

  private static Logger logger = LoggerFactory.getLogger(ImapMailStore.class);

  private static final String PROTOCOL = "imap";
  private static final String PORT = "143";
  private static final List<String> HOSTS = Lists.newArrayList(
      "imap.mxhichina.com",
      "imap.163.com",
      "imap.aliyun.com");
  private static final Map<String, Store> storeMap = new ConcurrentHashMap<>(3);

  static {
    HOSTS.forEach(host -> {
      logger.info("INIT HOST[" + host + "]");
      storeMap.putIfAbsent(getImapPrivoder(host), getStore(host));
    });
  }

  public static Store getStoreFromCache(String key) {
    return storeMap.putIfAbsent(key, getStore(getHost(key)));
  }

  private static Store getStore(String host) {
    Properties props = new Properties();
    props.setProperty("mail.imap.protocol", PROTOCOL);       // 协议
    props.setProperty("mail.imap.port", PORT);             // 端口
    props.setProperty("mail.imap.host", host);    // imap服务器
    props.put("mail.imap.auth.plain.disable", "true");
    props.setProperty("mail.debug", "true");
    Session session = Session.getInstance(props);
    try {
      return session.getStore("imap");
    } catch (NoSuchProviderException e) {
      logger.info(e.getMessage());
    }
    return null;
  }

  private static String getImapPrivoder(String host) {
    int firstIndex = host.indexOf(".");
    int lastIndex = host.lastIndexOf(".");
    String provider = host.substring(firstIndex+1, lastIndex);
    logger.info("provider[" + provider + "]");
    return provider;
  }

  private static String getHost(String privoder) {

    return HOSTS.stream()
        .filter(h -> h.contains(privoder))
        .findFirst().orElse("imap.aliyun.com");
  }

}
