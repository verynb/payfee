package com.bit.network;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/**
 * Created by yj on 2017/11/27.
 */
@Slf4j
public class HttpUtils {

  public static HttpResult doGet(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws Exception {
    CookieStore cookieStore = new BasicCookieStore();
    HttpClient httpClient = HttpClients
        .custom()
        .setDefaultCookieStore(cookieStore)
        .build();
    // 设置请求参数
    StringBuilder param = new StringBuilder(crawlMeta.getUrl()).append("?");
    httpConf.getRequestParams().forEach((k, v) -> {
      param.append(k);
      param.append("=");
      param.append(v);
      param.append("&");
    });
    HttpGet httpGet = new HttpGet(param.substring(0, param.length() - 1)); // 过滤掉最后一个无效字符
    // 设置请求头
    httpConf.getRequestHeaders().forEach((k, v) -> {
      httpGet.addHeader(k, v);
    });
    // 执行网络请求
    HttpResponse response = httpClient.execute(httpGet);
    //保存cookie
    SessionHolder.updateCookie(cookieStore.getCookies());
    return new HttpResult(httpClient, httpGet, response);
  }

  public static HttpPostResult doPost(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws Exception {
    CookieStore cookieStore = new BasicCookieStore();
    HttpClient httpClient = HttpClients
        .custom()
        .setDefaultCookieStore(cookieStore)
        .build();
    HttpPost httpPost = new HttpPost(crawlMeta.getUrl());
    httpPost.setEntity(new UrlEncodedFormEntity(mapToName(httpConf.getRequestParams()), HTTP.UTF_8));
    // 设置请求头
    httpConf.getRequestHeaders().forEach((k, v) -> {
      httpPost.addHeader(k, v);
    });
    HttpResponse response = httpClient.execute(httpPost);
    SessionHolder.updateCookie(cookieStore.getCookies());
    return new HttpPostResult(httpClient, httpPost, response);
  }

  private static List<NameValuePair> mapToName(Map<String, Object> paramMaps) {
    List<NameValuePair> params = Lists.newArrayList();
    paramMaps.forEach((k, v) -> {
      params.add(new BasicNameValuePair(k, v.toString()));
    });
    return params;
  }

}
