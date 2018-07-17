package com.bit.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;

/**
 * Created by yuanj on 2018/7/17.
 */
@AllArgsConstructor
@Data
public class ClientAndCookie {

  private HttpClient httpClient;
  private CookieStore cookieStore;
}
