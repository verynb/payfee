import config.ThreadConfig;
import identity.IdentityCheck;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import load.LoadData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.TransferUserInfo;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
  private static String version = "0.1";
  public static final List<TransferUserInfo> users = new CopyOnWriteArrayList();
  private static final String USER_PATH = "./account2.csv";
  private static final ThreadConfig config = new ThreadConfig(2, 100, 50);

  public static void main(String[] args) {
    IdentityCheck.checkVersion(version);
    IdentityCheck.checkIdentity();
    logger.info("[version=" + version + "] [" + new DateTime().toString("yyyy-MM-dd") + "]应用启动。。。");
    LoadData.loadUserInfoData(USER_PATH).forEach(u -> users.add(u));
    ScheduledThread s = new ScheduledThread();
    s.generateRenewal(users);
    List<TransferUserInfo> enough = s.filterNotEnoughAccount();
    if (CollectionUtils.isNotEmpty(enough)) {
      s.generateTransfer(enough);
      s.generateRenewal(enough);
    }
    LoadData.writeResult(users);
    int loginFail = LoadData.countResult(users, "-2");
    int noRenewal = LoadData.countResult(users, "0");
    int noLeft = LoadData.countResult(users, "1");
    int renewFail = LoadData.countResult(users, "2");
    logger.info("登录失败[" + loginFail + "]");
    logger.info("进入缴费[" + noRenewal + "]");
    logger.info("余额不足[" + noLeft + "]");
    logger.info("支付失败[" + renewFail + "]");
    logger.info("支付成功[" + (users.size() - loginFail - noLeft - noRenewal - renewFail) + "]");

    System.out.println("输入任意结束");
    Scanner scan = new Scanner(System.in);
    String read = scan.nextLine();
    while (StringUtils.isBlank(read)) {

    }
  }

  public List<TransferUserInfo> filterNotEnoughAccount() {
    return users.stream()
        .filter(u -> u.getFlag().equals("1"))
        .collect(Collectors.toList());
  }

  public void generateTransfer(final List<TransferUserInfo> users) {
    ExecutorService service = Executors.newFixedThreadPool(config.getThreadPoolSize());
    users.forEach(u -> {
      service.execute(TaskFactory.getTransferInstance(u, config));
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
  }

  public void generateRenewal(final List<TransferUserInfo> users) {
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
