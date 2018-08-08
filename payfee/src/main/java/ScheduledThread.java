import com.mail.api.TransferUserInfo;
import com.mail.api.UserInfoFilterUtil;
import com.mail.support.ImapMailStore;
import com.transfer.job.TransferCrawlJob;
import config.ThreadConfig;
import identity.IdentityCheck;
import identity.LocationConfig;
import identity.XmlReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import load.LoadData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
  private static String version = "1.1";
  private static int tryTime = 5;
  private static String pName = "续期";
  private static final String USER_PATH = "./续期.csv";
  private static final ThreadConfig config = new ThreadConfig(2, 10, 50);

  private static LocationConfig locationConfig = null;

  static {
    logger.info("开始加载分区配置信息,当前应用[" + pName + "],当前版本[" + version + "]");
    System.out.println("请输入用户名:");
    Scanner scanUserName = new Scanner(System.in);
    String readUserName = scanUserName.next();
    while (StringUtils.isBlank(readUserName)) {
    }
    locationConfig = XmlReader.getConfig(readUserName, pName);
    if (locationConfig != null && locationConfig.getName().equals("unkonwn")) {
      System.out.println("用户名错误");
      System.out.println("输入任意结束");
      Scanner scan = new Scanner(System.in);
      String read = scan.nextLine();
      while (StringUtils.isBlank(read)) {

      }
      System.exit(0);
    }
    if (locationConfig != null && !locationConfig.getName().equals("unkonwn")) {
      logger.info("加载分区配置信息成功应用启动。。。");
      logger.info("开始初始化邮箱连接池");
      ImapMailStore.initImapMailStore();
      logger.info("初始化邮箱连接池成功");
    } else {
      logger.info("未找到当前应用[" + pName + "]分区配置信息!");
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
    UserInfoFilterUtil.initMail();
    ScheduledThread s = new ScheduledThread();
    s.generateRenewal(UserInfoFilterUtil.users);
    s.tryTime();
    LoadData.writeResult(UserInfoFilterUtil.users, USER_PATH);
    long successCount = UserInfoFilterUtil.users.stream()
        .filter(u -> StringUtils.isNotBlank(u.getFlag()))
        .filter(u -> u.getFlag().equals("1") || Double.valueOf(u.getFlag()) > 100)
        .count();
    long failueCount = UserInfoFilterUtil.users.size() - successCount;
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
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {

      }
      tryTime();
    }
  }

  private List<TransferUserInfo> filterNotEnoughAccount() {
    return UserInfoFilterUtil.users.stream()
        .filter(u -> u.getFlag().equals("-1"))
        .collect(Collectors.toList());
  }

  private void generateTransfer(final List<TransferUserInfo> users) {
    List<TransferCrawlJob> jobs = TaskFactory.getTransferInstance(users, config);
    ScheduledExecutorService service = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    jobs.forEach(j -> {
      service.schedule(j, 0, TimeUnit.SECONDS);
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
  }

  private void generateRenewal(final List<TransferUserInfo> users) {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    users.forEach(u -> {
      service.schedule(TaskFactory.getSimpleInstance(u, config), 0, TimeUnit.SECONDS);
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
  }

}
