package com.transfer.task;

import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HostConfig;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import com.transfer.entity.UserInfo;
import com.transfer.load.TransferUserFilterUtil;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/11/27.
 */
public class GetReceiverTask {

  private static volatile Map<String, UserInfo> receiverCache = Maps.newConcurrentMap();

  private static Logger logger = LoggerFactory.getLogger(GetReceiverTask.class);
  private static String URL = HostConfig.HOST + "users/is_down_line_binary";


  private static Map getParam(String userName) {
    Map<String, String> paramMap = Maps.newHashMap();
    paramMap.put("user", userName);
    return paramMap;
  }

  private static Map getHeader() {
    Map<String, String> headMap = Maps.newHashMap();
    headMap.put("x-requested-with", "XMLHttpRequest");
    return headMap;
  }

  public static UserInfo execute(String userName, int row) {
    try {
      logger.info("开始获取转出账户[" + userName + "]信息");
      if (receiverCache.containsKey(userName)) {
        logger.info("获取转出账户[" + userName + "]信息FROM CACHE");
        return receiverCache.get(userName);
      } else {
        String response = HttpUtils.get(CrawlMeta.getNewInstance(GetReceiverTask.class, URL),
            new CrawlHttpConf(getParam(userName), getHeader()));
        UserInfo userInfo = GsonUtil.jsonToObject(response, UserInfo.class);
        if (!userInfo.getResponse()) {
          logger.info("转账人[" + userName + "]不存在或者不存在于您的二进制树中");
          TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "转账人[" + userName + "]非下线");
          return new UserInfo();
        }
        receiverCache.putIfAbsent(userName, userInfo);
        return userInfo;
      }
    } catch (IOException e) {
      logger.info("获取转出账户[" + userName + "]信息失败");
      TransferUserFilterUtil.filterAndUpdateFlag(row, "0", "网络异常");
      return new UserInfo();
    }
  }
}
