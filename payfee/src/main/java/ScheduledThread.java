import com.mail.api.UserInfoFilterUtil;
import com.mail.support.ImapMailStore;
import com.transfer.job.TransferCrawlJob;
import com.transfer.load.TransferUserFilterUtil;
import config.ThreadConfig;
import identity.IdentityCheck;
import identity.LocationConfig;
import identity.XmlReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import load.LoadData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mail.api.TransferUserInfo;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
  private static String version = "1.3";
  private static int tryTime = 5;
  private static String location = "qita";
  private static String pName = "收分";
  private static final String USER_PATH = "./account.csv";
  private static final ThreadConfig config = new ThreadConfig(2, 10, 50);

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
    LoadData.loadUserInfoData(USER_PATH).forEach(u -> UserInfoFilterUtil.users.add(u));
    logger.info("开始初始化邮箱连接池");
    ImapMailStore.initImapMailStore();
    ScheduledThread s = new ScheduledThread();
    s.generateRenewal(UserInfoFilterUtil.users);
    s.tryTime();
    LoadData.writeResult(UserInfoFilterUtil.users);
    long failueCount = UserInfoFilterUtil.users.stream()
        .filter(u -> StringUtils.isNotBlank(u.getFlag()))
        .filter(u -> u.getFlag().equals("0")).count();
    long successCount = UserInfoFilterUtil.users.size() - failueCount;
    System.out.println("所有任务执行完毕，成功：" + successCount + ",失败：" + failueCount);
    System.out.println("输入任意结束");
    Scanner scan = new Scanner(System.in);
    String read = scan.nextLine();
    while (StringUtils.isBlank(read)) {

    }
  }

  private void tryTime() {
    List<TransferUserInfo> enough = this.filterNotEnoughAccount();
    if (CollectionUtils.isNotEmpty(enough) && (tryTime--) > 0) {
      this.generateTransfer(enough);
      this.generateRenewal(enough);
    }
  }

  private List<TransferUserInfo> filterNotEnoughAccount() {
    return UserInfoFilterUtil.users.stream()
        .filter(u -> u.getFlag().equals("-1"))
        .collect(Collectors.toList());
  }

  private void generateTransfer(final List<TransferUserInfo> users) {
    List<TransferCrawlJob> jobs = TaskFactory.getTransferInstance(users, config);
    ExecutorService service = Executors.newSingleThreadExecutor();
    jobs.forEach(j -> {
      service.execute(j);
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
  }

  private void generateRenewal(final List<TransferUserInfo> users) {
    ExecutorService service = Executors.newFixedThreadPool(config.getThreadPoolSize());
    users.forEach(u -> {
      service.execute(TaskFactory.getSimpleInstance(u, config));
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
  }

}
