package com.bit.network;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/11/27.
 */
@Slf4j
public class HttpUtils {

  private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
  // 默认超时时间：10s
  private static final int TIME_OUT = 100000;
  private static PoolingHttpClientConnectionManager cm = null;
//  private static HttpRequestRetryHandler httpRequestRetryHandler = null;

  private static RequestConfig config = RequestConfig
      .custom()
      .setRedirectsEnabled(true)
      .setSocketTimeout(TIME_OUT)
      .setConnectTimeout(TIME_OUT)
      .setConnectionRequestTimeout(TIME_OUT)
      .build();

  static {
    LayeredConnectionSocketFactory sslsf = null;
    try {
      sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
    } catch (NoSuchAlgorithmException e) {
      logger.error("创建SSL连接失败...");
    }
    Registry<ConnectionSocketFactory> sRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("https", sslsf)
        .register("http", new PlainConnectionSocketFactory())
        .build();
    cm = new PoolingHttpClientConnectionManager(sRegistry);
    // 设置最大的连接数
    cm.setMaxTotal(200);
    // 设置每个路由的基础连接数【默认，每个路由基础上的连接不超过2个，总连接数不能超过20】
    cm.setDefaultMaxPerRoute(200);

  }

  private static ClientAndCookie getHttpClient() {
    CookieStore cookieStore = new BasicCookieStore();
    CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultCookieStore(cookieStore)
        .setDefaultRequestConfig(config)
        .setRetryHandler(retryHandler())
        .setConnectionManager(cm)
        .build();
    return new ClientAndCookie(httpClient, cookieStore);
  }

  public static HttpResult doGet(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws Exception {

    ClientAndCookie clientAndCookie = getHttpClient();
    CookieStore cookieStore = clientAndCookie.getCookieStore();
    HttpClient httpClient = clientAndCookie.getHttpClient();
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
    ClientAndCookie clientAndCookie = getHttpClient();
    CookieStore cookieStore = clientAndCookie.getCookieStore();
    HttpClient httpClient = clientAndCookie.getHttpClient();
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


  public static String get(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws IOException {
    ClientAndCookie clientAndCookie = getHttpClient();
    CookieStore cookieStore = clientAndCookie.getCookieStore();
    HttpClient httpClient = clientAndCookie.getHttpClient();
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
    try {
      logger.info("Executing request " + httpGet.getRequestLine());
      HttpResponse response = httpClient.execute(httpGet);
      SessionHolder.updateCookie(cookieStore.getCookies());
      HttpEntity entity = response.getEntity();
      String result = EntityUtils.toString(entity, "utf-8");
      EntityUtils.consume(entity);
      logger.info("Request finished");
      return result;
    } finally {
      httpGet.releaseConnection();
    }
  }

  public static int login(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws IOException {
    ClientAndCookie clientAndCookie = getHttpClient();
    CookieStore cookieStore = clientAndCookie.getCookieStore();
    HttpClient httpClient = clientAndCookie.getHttpClient();
    HttpPost httpPost = new HttpPost(crawlMeta.getUrl());
    httpPost.setEntity(new UrlEncodedFormEntity(mapToName(httpConf.getRequestParams()), HTTP.UTF_8));
    // 设置请求头
    httpConf.getRequestHeaders().forEach((k, v) -> {
      httpPost.addHeader(k, v);
    });
    try {
      logger.info("Executing request " + httpPost.getRequestLine());
      HttpResponse response = httpClient.execute(httpPost);
      SessionHolder.updateCookie(cookieStore.getCookies());
      logger.info("Request finished");
      return response.getStatusLine().getStatusCode();
    } finally {
      httpPost.releaseConnection();
    }
  }


  public static String post(CrawlMeta crawlMeta, CrawlHttpConf httpConf) throws IOException {
    ClientAndCookie clientAndCookie = getHttpClient();
    CookieStore cookieStore = clientAndCookie.getCookieStore();
    HttpClient httpClient = clientAndCookie.getHttpClient();
    HttpPost httpPost = new HttpPost(crawlMeta.getUrl());
    httpPost.setEntity(new UrlEncodedFormEntity(mapToName(httpConf.getRequestParams()), HTTP.UTF_8));
    // 设置请求头
    httpConf.getRequestHeaders().forEach((k, v) -> {
      httpPost.addHeader(k, v);
    });
    try {
      logger.info("Executing request " + httpPost.getRequestLine());
      HttpResponse response = httpClient.execute(httpPost);
      SessionHolder.updateCookie(cookieStore.getCookies());
      HttpEntity entity = response.getEntity();
      String result = EntityUtils.toString(entity, "utf-8");
      EntityUtils.consume(entity);
      logger.info("Request finished");
      return result;
    } finally {
      httpPost.releaseConnection();
    }
  }


  private static HttpRequestRetryHandler retryHandler() {
    return (exception, executionCount, context) -> {

      logger.info("try request: " + executionCount);

      if (executionCount >= 5) {
        // Do not retry if over max retry count
        return false;
      }
      if (exception instanceof InterruptedIOException) {
        // Timeout
        return false;
      }
      if (exception instanceof UnknownHostException) {
        // Unknown host
        return false;
      }
      if (exception instanceof SSLException) {
        // SSL handshake exception
        return false;
      }
      HttpClientContext clientContext = HttpClientContext.adapt(context);
      HttpRequest request = clientContext.getRequest();
      boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
      if (idempotent) {
        // Retry if the request is considered idempotent
        return true;
      }
      return false;
    };
  }

}
