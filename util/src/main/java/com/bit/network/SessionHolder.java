package com.bit.network;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.http.cookie.Cookie;

/**
 * Created by yuanj on 9/21/16.
 */
@Data
public class SessionHolder {

  private static final ThreadLocal<Session> threadSession = ThreadLocal.withInitial(() -> init());

  private static Session init() {
    Session session = new Session(Lists.newArrayList());
    return session;
  }

  private static void writeSession(final List<LocalCookie> localCookies) {
    final Session session = threadSession.get();
    session.setCookies(localCookies);
  }

  private static LocalCookie createLocalCookie(Cookie cookie) {
    return new LocalCookie(cookie.getName(), cookie.getValue());
  }

  public static List<LocalCookie> getCookies() {
    final Session session = threadSession.get();
    return session.getCookies();
  }


  public static void updateCookie(List<Cookie> cookies) {
    List<LocalCookie> localCookies = cookies
        .stream()
        .map(c -> createLocalCookie(c)).collect(Collectors.toList());
    writeSession(localCookies);
  }

}
