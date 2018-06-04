import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import load.LoadData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.TransferUserInfo;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

  private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
  private static String version = "1.1";
  public static final List<TransferUserInfo> users = new CopyOnWriteArrayList();
  private static final String USER_PATH = "./account.csv";
  private static final ThreadConfig config = new ThreadConfig(2, 100, 50);

  public static void main(String[] args) {
    LoadData.loadUserInfoData(USER_PATH).forEach(u -> users.add(u));
    ScheduledExecutorService service = Executors.newScheduledThreadPool(config.getThreadPoolSize());
    users.forEach(u -> {
      service.schedule(new SimpleCrawlJob(config, u.getUserName(), u.getPassword(), u.getRow()),
          5, TimeUnit.SECONDS);
    });
    service.shutdown();
    while (true) {
      if (service.isTerminated()) {
        break;
      }
    }
    LoadData.writeResult(users);
    System.out.println("输入任意结束");
    Scanner scan = new Scanner(System.in);
    String read = scan.nextLine();
    while (StringUtils.isBlank(read)) {

    }
  }
}
