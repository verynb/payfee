package com.transfer;

import com.bit.network.RandomUtil;
import com.google.common.collect.Lists;
import com.transfer.entity.TransferUserInfo;
import com.transfer.job.TransferCrawlJob;
import com.transfer.load.LoadTransferData;
import config.ThreadConfig;
import identity.IdentityCheck;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/12/2.
 */
public class TransferScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(TransferScheduledThread.class);

  private static String version = "1.3";

  private static final ThreadConfig config = new ThreadConfig(2, 10, 50);

  public static String getVersionData() {
    return new DateTime().getMillis() + "-" + version;
  }

  public static void main(String[] args) {
//    IdentityCheck.checkVersion(version);
//    IdentityCheck.checkIdentity();
    logger.info("[version=" + version + "] [" + new DateTime().toString("yyyy-MM-dd") + "]应用启动。。。");
    logger.info("开始加载用户数据");
//    List<TransferUserInfo> userInfos = LoadTransferData.loadUserInfoData("./account.csv");
    List<TransferUserInfo> userInfos = Lists.newArrayList(new TransferUserInfo(1, "lhha001", "hh8389lhhl",
        "lianghuihua01@bookbitbtc.com", "SHENzen007v", "lhha003", "", ""),
        new TransferUserInfo(1, "lhha002", "hh8389lhhl",
            "lianghuihua01@bookbitbtc.com", "SHENzen007v", "lhha003", "", ""),
        new TransferUserInfo(1, "lhha003", "hh8389lhhl",
            "lianghuihua01@bookbitbtc.com", "SHENzen007v", "lhha001", "", ""));

    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    for (int i = 0; i < userInfos.size(); i++) {
      scheduledThreadPool.schedule(new TransferCrawlJob(userInfos.get(i), config), 0, TimeUnit.SECONDS);
    }
    scheduledThreadPool.shutdown();
  }
}
