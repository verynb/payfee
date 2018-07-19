package com.mail.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class CancelTokenTask {

  private static Logger logger = LoggerFactory.getLogger(CancelTokenTask.class);
  private static String URL = HostConfig.HOST + "tokens/cancel";

  private static Map getParam(String type) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("token_type", type);
    return paramMap;
  }

  private static Map<String, String> getHeader() {
    Map<String, String> map = Maps.newHashMap();
    map.put("x-requested-with", "XMLHttpRequest");
    return map;
  }

  public static String execute(String type) throws IOException {
    String response = HttpUtils.get(CrawlMeta.getNewInstance(CancelTokenTask.class, URL),
            new CrawlHttpConf(getParam(type), getHeader()));
    return response;
  }
}
