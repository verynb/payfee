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
import identity.LocationConfig;
import identity.XmlReader;
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

  private static String version = "1.0";
  private static String location = "qita";
  private static String pName = "提现";

  private static final ThreadConfig config = new ThreadConfig(2, 10, 100);

  private static LocationConfig locationConfig = null;

  static {
    logger.info("开始加载分区配置信息,当前应用[" + pName + "]当前分区[" + location + "],当前版本[" + version + "]");
    locationConfig = XmlReader.getConfig(location, pName);
    if (locationConfig != null) {
      logger.info("加载分区配置信息成功应用启动。。。");
    } else {
      logger.info("加载分区配置信息失败。。。");
      System.out.println("输入任意结束");
      Scanner scan = new Scanner(System.in);
      String read = scan.nextLine();
      while (StringUtils.isBlank(read)) {

      }
      System.exit(0);
    }
  }

  public static void main(String[] args) {
    IdentityCheck.checkIdentity(locationConfig.getTimelimit());
    IdentityCheck.checkVersion(version, locationConfig.getVer());
    IdentityCheck.checkPassword(3, locationConfig.getPassword());
    logger.info("开始加载用户数据");
    LoadPayoutData.loadUserInfoData("./account.csv").forEach(u -> PayOutUserFilterUtil.users.add(u));
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    for (int i = 0; i < PayOutUserFilterUtil.users.size(); i++) {
      scheduledThreadPool.schedule(new RequestPayoutJob(PayOutUserFilterUtil.users.get(i), config),
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
