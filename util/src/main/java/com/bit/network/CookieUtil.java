package com.bit.network;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yuanj on 2018/5/28.
 */
public class CookieUtil {

  public static String cookieBuilder() {
    List<String> cookieStrings = SessionHolder
        .getCookies()
        .stream()
        .map(c -> createCookie(c)).collect(Collectors.toList());

    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < cookieStrings.size(); i++) {
      buffer.append(cookieStrings.get(i));
      if (i < cookieStrings.size() - 1) {
        buffer.append("; ");
      }
    }
    return buffer.toString();
  }

  private static String createCookie(LocalCookie c) {
    StringBuilder builder = new StringBuilder(c.getSessionKey());
    builder.append("=");
    builder.append(c.getSessionValue());
    return builder.toString();
  }

}
