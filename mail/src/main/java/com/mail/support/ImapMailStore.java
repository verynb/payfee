package com.mail.support;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/11/30.
 */
@Slf4j
public class ImapMailStore {

  private static Logger logger = LoggerFactory.getLogger(ImapMailStore.class);

  private static final List<String> HOSTS = Lists.newArrayList(
      "imap.163.com", "pop3.aliyun.com", "pop3.mxhichina.com");

  private static final Map<String, Store> storeMap = new ConcurrentHashMap<>(3);

  static {
    HOSTS.forEach(host -> {
      logger.info("INIT HOST[" + host + "]");
      storeMap.putIfAbsent(getImapPrivoder(host), getStore(host));
    });
  }

  public static Store getStoreFromCache(String key) {
//    return storeMap.putIfAbsent(key, getStore(getHost(key)));
    return storeMap.get("mxhichina");
  }

  private static Store getStore(String host) {
    String provider = getImapPrivoder(host);
    if (provider.equals("163")) {
      return getImapStore(host);
    } else {
      return getPop3Store(host);
    }
  }

  private static Store getImapStore(String host) {
    Properties props = new Properties();
    props.setProperty("mail.imap.protocol", "imap");       // 协议
    props.setProperty("mail.imap.port", "143");             // 端口
    props.setProperty("mail.imap.host", host);    // imap服务器
    props.put("mail.imap.auth.plain.disable", "true");
//    props.setProperty("mail.debug", "true");
    Session session = Session.getInstance(props);
    try {
      return session.getStore("imap");
    } catch (NoSuchProviderException e) {
      logger.info(e.getMessage());
    }
    return null;
  }

  private static Store getPop3Store(String host) {
    Properties props = new Properties();
    props.setProperty("mail.store.protocol", "pop3");       // 协议
    props.setProperty("mail.pop3.port", "110");             // 端口
    props.setProperty("mail.pop3.host", host);    // pop3服务器
//    props.setProperty("mail.pop3.auth", "true");// 指定验证为true
//    props.setProperty("mail.debug", "true");
    Session session = Session.getInstance(props);
    try {
      return session.getStore("pop3");
    } catch (NoSuchProviderException e) {
      logger.info(e.getMessage());
    }
    return null;
  }

  private static String getImapPrivoder(String host) {
    int firstIndex = host.indexOf(".");
    int lastIndex = host.lastIndexOf(".");
    String provider = host.substring(firstIndex + 1, lastIndex);
    logger.info("provider[" + provider + "]");
    return provider;
  }

  private static String getHost(String privoder) {

    return HOSTS.stream()
        .filter(h -> h.contains(privoder))
        .findFirst().orElse("pop3.mxhichina.com");
  }

}