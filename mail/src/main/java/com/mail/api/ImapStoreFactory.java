package com.mail.api;

import com.mail.support.ImapMailStore;
import com.mail.support.ImapMailToken;
import javax.mail.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2018/7/16.
 */
public class ImapStoreFactory {

  private static Logger logger = LoggerFactory.getLogger(ImapMailToken.class);


  public static Store getStore(String host) {
    return ImapMailStore.getStoreFromCache(parseHost(host));
  }

  private static String parseHost(String host) {
    logger.info("host[" + host + "]");
    int start = host.indexOf("@");
    int last = host.lastIndexOf(".com");
    String provider = host.substring(start + 1, last);
    String hostProvider = (!provider.equals("163") && !provider.equals("aliyun")) ? "mxhichina" : provider;
    logger.info("hostProvider[" + hostProvider + "]");
    return hostProvider;
  }
}
