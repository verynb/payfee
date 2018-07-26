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
      "imap.163.com", "imap.aliyun.com", "imap.mxhichina.com");

  private static final Map<String, Store> storeMap = new ConcurrentHashMap<>(3);

  public static void initImapMailStore() {
    HOSTS.forEach(host -> {
      logger.info("INIT HOST[" + host + "]");
      storeMap.putIfAbsent(getImapPrivoder(host), getStore(host));
    });
  }

  public static Store getStoreFromCache(String key) {
    return storeMap.get(key);
  }

  private static Store getStore(String host) {
    return getImapStore(host);
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

  public static String getImapPrivoder(String host) {
    try {
      int firstIndex = host.indexOf(".");
      int lastIndex = host.lastIndexOf(".");
      String provider = host.substring(firstIndex + 1, lastIndex);
      logger.info("provider[" + provider + "]");
      return (!provider.equals("163") && !provider.equals("aliyun")) ? "mxhichina" : provider;
    } catch (Exception e) {
      return "mxhichina";
    }

  }
}
