package com.bit.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/12/9.
 */
public class GetNetworkTime {

  private static final String webUrl = "http://www.taobao.com";//淘宝

  private static final String LIMITEDTIME_URL = "http://www.i9wine.com/limitedtime02.html";//时间限制

  private static final String FORMART = "yyyy-MM-dd HH:mm:ss";

  private static Logger logger = LoggerFactory.getLogger(GetNetworkTime.class);

  public static Long getNetworkDatetime() {
    try {
      URL url = new URL(webUrl);// 取得资源对象
      URLConnection uc = url.openConnection();// 生成连接对象
      uc.connect();// 发出连接
      long ld = uc.getDate();// 读取网站日期时间
      return ld;
    } catch (MalformedURLException e) {
      return System.currentTimeMillis();
    } catch (IOException e) {
      return System.currentTimeMillis();
    }
  }

  public static Long getNetworkLimiteTime() {
    HttpResult response = null;
    Long time = null;
    try {
      response = HttpUtils.doGet(CrawlMeta.getNewInstance(GetNetworkTime.class, LIMITEDTIME_URL), new CrawlHttpConf());
      String date = EntityUtils.toString(response.getResponse().getEntity());
      int index = date.indexOf(",");
      time = new SimpleDateFormat(FORMART).parse(date.substring(index + 1)).getTime();
      logger.info("time-->" + time);
    } catch (Exception e) {
      logger.info("取时间失败 Exception-->:" + e.getMessage());
      logger.info("取时间失败response-->" + response.getResponse().toString());
      throw new RuntimeException("取时间失败");
    }
    return time;
  }

  public static String getNetworkVersion() {
    HttpResult response = null;
    try {
      response = HttpUtils.doGet(CrawlMeta.getNewInstance(GetNetworkTime.class, LIMITEDTIME_URL), new CrawlHttpConf());
      String date = EntityUtils.toString(response.getResponse().getEntity());
      int index = date.indexOf(",");
      String version = date.substring(0, index);
      logger.info("version-->" + version);
      return version;
    } catch (Exception e) {
      logger.info("取版本号失败 Exception-->:" + e.getMessage());
      logger.info("取版本号失败response-->" + response.getResponse().toString());
      throw new RuntimeException("取时间失败");
    }
  }

  public static void main(String[] args) {
    getNetworkLimiteTime();
  }
}
