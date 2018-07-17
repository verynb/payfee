package com.transfer;

import com.bit.network.RandomUtil;
import com.google.common.collect.Lists;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import com.transfer.job.RequestPayoutJob;
import com.transfer.job.TransferCrawlJob;
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
public class PayOutScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(PayOutScheduledThread.class);

  private static String version="1.3";

  private static final ThreadConfig config = new ThreadConfig(2, 10, 50);

  public static String getVersionData(){
    return new DateTime().getMillis()+"-"+version;
  }

  public static void main(String[] args) {
//    IdentityCheck.checkVersion(version);
//    IdentityCheck.checkIdentity();
    logger.info("[version=" + version + "] [" + new DateTime().toString("yyyy-MM-dd") + "]应用启动。。。");
    logger.info("开始加载用户数据");
    List<PayOutUserInfo> userInfos = Lists.newArrayList(new PayOutUserInfo(1,
        "lhha003",
        "hh8389lhhl",
        "lianghuihua01@bookbitbtc.com",
        "SHENzen007v",
        "whka014",
        "",
        "",""));
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    for (int i = 0; i < userInfos.size(); i++) {
      scheduledThreadPool.schedule(new RequestPayoutJob(userInfos.get(i), config),
          5, TimeUnit.SECONDS);
      try {
        int space = RandomUtil.ranNum(config.getThreadspaceTime() * 1000 + 5000);
        logger.info("任务时间间隔:" + space + "ms");
        Thread.sleep(space);
      } catch (InterruptedException e) {
      }
    }
    scheduledThreadPool.shutdown();
  }
}
