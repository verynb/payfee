package com.bit.network;

import java.io.IOException;
import java.io.InputStream;
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

  private static final String LIMITEDTIME_URL = "http://www.i9wine.com/airbit.conf";//时间限制

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

  public static InputStream getNetworkConfig() {
    try {
      URL url = new URL(LIMITEDTIME_URL);// 取得资源对象
      URLConnection uc = url.openConnection();// 生成连接对象
      uc.connect();// 发出连接
      InputStream inStream = uc.getInputStream();
      return inStream;
    } catch (MalformedURLException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }
}
