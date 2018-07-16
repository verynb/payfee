package com.mail.api;

import com.mail.support.ImapMailStore;
import javax.mail.Store;

/**
 * Created by yuanj on 2018/7/16.
 */
public class ImapStoreFactory {

  public static Store getStore(String host) {
    return ImapMailStore.getStoreFromCache(parseHost(host));
  }

  private static String parseHost(String host) {
    int start = host.indexOf("@");
    int last = host.lastIndexOf(".com");
    return host.substring(start+1, last);
  }
}
