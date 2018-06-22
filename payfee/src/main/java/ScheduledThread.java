import command.api.UserInfoFilterUtil;
import config.ThreadConfig;
import identity.IdentityCheck;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import load.LoadData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import command.api.TransferUserInfo;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
  private static String version = "0.3";

  private static final String USER_PATH = "./account.csv";
  private static final ThreadConfig config = new ThreadConfig(2, 10, 50);

  public static void main(String[] args) {
    IdentityCheck.checkVersion(version);
    IdentityCheck.checkIdentity();
    logger.info("[version=" + version + "] [" + new DateTime().toString("yyyy-MM-dd") + "]应用启动。。。");
    LoadData.loadUserInfoData(USER_PATH).forEach(u -> UserInfoFilterUtil.users.add(u));
    ScheduledThread s = new ScheduledThread();
    s.generateRenewal(UserInfoFilterUtil.users);
    List<TransferUserInfo> enough = s.filterNotEnoughAccount();
    if (CollectionUtils.isNotEmpty(enough)) {
      s.generateTransfer(enough);
      s.generateRenewal(enough);
    }
    LoadData.writeResult(UserInfoFilterUtil.users);
    int loginFail = LoadData.countResult(UserInfoFilterUtil.users, "-2b");
    int noRenewal = LoadData.countResult(UserInfoFilterUtil.users, "0b");
    int noLeft = LoadData.countResult(UserInfoFilterUtil.users, "1b");
    int renewFail = LoadData.countResult(UserInfoFilterUtil.users, "2b");
    logger.info("登录失败[" + loginFail + "]");
    logger.info("进入缴费[" + noRenewal + "]");
    logger.info("余额不足[" + noLeft + "]");
    logger.info("支付失败[" + renewFail + "]");
    logger.info("支付成功[" + (UserInfoFilterUtil.users.size() - loginFail - noLeft - noRenewal - renewFail) + "]");

    System.out.println("输入任意结束");
    Scanner scan = new Scanner(System.in);
    String read = scan.nextLine();
    while (StringUtils.isBlank(read)) {

    }
  }

  public List<TransferUserInfo> filterNotEnoughAccount() {
    return UserInfoFilterUtil.users.stream()
        .filter(u -> u.getFlag().equals("1b"))
        .collect(Collectors.toList());
  }

  public List<TransferUserInfo> filterTransferedAccount() {
    return UserInfoFilterUtil.users.stream()
        .filter(u -> u.getFlag().equals("6a"))
        .collect(Collectors.toList());
  }

  public void generateTransfer(final List<TransferUserInfo> users) {
    ExecutorService service = Executors.newSingleThreadExecutor();
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
