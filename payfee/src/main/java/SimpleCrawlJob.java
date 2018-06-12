import RanewalJob.AbstractJob;
import com.bit.network.SessionHolder;
import config.ThreadConfig;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.ActFee;
import support.LoginResult;
import support.LoginSuccessResult;
import support.RenewalAmount;
import support.RenewalParam;
import support.RenewalUtil;
import support.TransferUserInfo;
import login.task.LoginTask;

/**
 * <p>
 * Created by yj on 2017/6/27.
 */
@Data
@Slf4j
public class SimpleCrawlJob extends AbstractJob {

  private ThreadConfig config;
  private String userName;
  private String password;
  private int index;
  private static Logger logger = LoggerFactory.getLogger(SimpleCrawlJob.class);


  public SimpleCrawlJob(final ThreadConfig config, String userName, String password, int index) {
    this.config = config;
    this.userName = userName;
    this.password = password;
    this.index = index;
  }

  @Override
  public void beforeRun() {

  }


  /**
   * 续期流程如下
   * 1.抓取登录静态页面，取到页面[authenticity_token]的值
   * 2.执行登录请求，获取登录后的cookies
   * 3.判断是否需要续期
   * 4.完成续期
   */
  @Override
  public void doFetchPage() throws Exception {
    LoginResult loginResult = LoginTask.tryTimes(config.getTransferErrorTimes(),
        config.getThreadspaceTime(),
        userName,
        password);
    if (!loginResult.isActive()) {
      logger.info("用户["+userName+"]登录失败");
      filterAndUpdateFlag(index, -2);
      return;
    }
    LoginSuccessResult successResult = LoginSuccessTask.execute();
    if (!successResult.isRenewal()) {
      logger.info("用户["+userName+"]不需要续期");
      filterAndUpdateFlag(index, 0);
      return;
    }
    RenewalParam param = successResult.filterIdValue();
    logger.info("用户["+userName+"]续期参数" + param.toString());
    RenewalAmount amount = successResult.filterWallet();
    logger.info("用户["+userName+"]续期钱包{" + amount.toString()+"}");
    if (RenewalUtil.usageFee(amount)) {
      ActFee actFee = RenewalUtil.actFee(amount);
      logger.info("用户["+userName+"]续期参数" + actFee.toString());
      param.setPayOneAmount(String.valueOf(actFee.getAccash()));
      param.setPayTwoAmount(String.valueOf(actFee.getAcrewards()));
      param.setPayThreeAmount(String.valueOf(actFee.getAcsavings()));
      param.setInputAmount(String.valueOf(amount.getAmount()));
      int code = RenewalTask.execute(param);
      if (code == 200) {
        logger.info("用户["+userName+"]续期成功");
        filterAndUpdateFlag(index, amount.getAmount());
        return;
      } else {
        logger.info("用户["+userName+"]续期失败");
        filterAndUpdateFlag(index, 2);
      }
    } else {
      logger.info("用户["+userName+"]续期余额不足");
      filterAndUpdateFlag(index, 1);
      return;
    }
  }

  @Override
  public void afterRun() {
    SessionHolder.removeCookie();
  }

  private void filterAndUpdateFlag(int index, double flag) {
    Optional<TransferUserInfo> filter = ScheduledThread.users.stream()
        .filter(u -> u.getRow() == index)
        .findFirst();
    if (filter.isPresent()) {
      filter.get().setFlag(flag);
    }
  }
}
