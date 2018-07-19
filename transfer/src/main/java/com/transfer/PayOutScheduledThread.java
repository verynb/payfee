package com.transfer;

import com.bit.network.RandomUtil;
import com.google.common.collect.Lists;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import com.transfer.job.RequestPayoutJob;
import com.transfer.job.TransferCrawlJob;
import com.transfer.load.LoadPayoutData;
import com.transfer.load.LoadTransferData;
import com.transfer.load.PayOutUserFilterUtil;
import com.transfer.load.TransferUserFilterUtil;
import config.ThreadConfig;
import identity.IdentityCheck;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/12/2.
 */
public class PayOutScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(PayOutScheduledThread.class);

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
    LoadPayoutData.loadUserInfoData("./account.csv").forEach(u -> PayOutUserFilterUtil.users.add(u));
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    for (int i = 0; i <  PayOutUserFilterUtil.users.size(); i++) {
      scheduledThreadPool.schedule(new RequestPayoutJob( PayOutUserFilterUtil.users.get(i), config),
          0, TimeUnit.SECONDS);
    }
    scheduledThreadPool.shutdown();
    while (true) {
      if (scheduledThreadPool.isTerminated()) {
        break;
      }
    }
    LoadPayoutData.writeResult(PayOutUserFilterUtil.users);
    long successCount = PayOutUserFilterUtil.users.stream().filter(u -> u.getFlag().equals("1")).count();
    long failueCount = PayOutUserFilterUtil.users.stream().filter(u -> u.getFlag().equals("0")).count();
    System.out.println("所有任务执行完毕，成功：" + successCount + ",失败：" + failueCount);
    System.out.println("输入任意结束");
    Scanner scan = new Scanner(System.in);
    String read = scan.nextLine();
    while (StringUtils.isBlank(read)) {

    }
  }
}
