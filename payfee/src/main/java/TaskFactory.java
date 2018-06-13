import com.transfer.job.TransferCrawlJob;
import config.ThreadConfig;
import support.TransferUserInfo;

/**
 * Created by yuanj on 2018/6/13.
 */
public class TaskFactory {


  public static SimpleCrawlJob getSimpleInstance(final TransferUserInfo user, final ThreadConfig config) {
    return new SimpleCrawlJob(config, user.getUserName(),
        user.getPassword(), user.getPuserName(),
        user.getPpassword(), user.getPmail(),
        user.getPmailPassword(), user.getTransferAmount(),
        user.getFlag(), user.getIndex());
  }

  public static TransferCrawlJob getTransferInstance(final TransferUserInfo user, final ThreadConfig config) {
    com.transfer.entity.TransferUserInfo userInfo = new com.transfer.entity.TransferUserInfo(user.getIndex(),
        user.getPuserName(),
        user.getPpassword(), user.getPmail(), user.getPmailPassword(), user.getUserName(), user.getTransferAmount());
    return new TransferCrawlJob(userInfo, config);
  }

}
