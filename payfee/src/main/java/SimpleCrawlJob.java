import RanewalJob.AbstractJob;
import com.bit.network.RandomUtil;
import com.bit.network.SessionHolder;
import com.mail.api.UserInfoFilterUtil;
import config.ThreadConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mail.support.ActFee;
import com.mail.support.LoginResult;
import com.mail.support.LoginSuccessResult;
import com.mail.support.RenewalAmount;
import com.mail.support.RenewalParam;
import com.mail.support.RenewalUtil;
import login.task.LoginTask;

/**
 * <p>
 * Created by yj on 2017/6/27.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class SimpleCrawlJob extends AbstractJob {

  private ThreadConfig config;
  private String userName;
  private String password;
  private String puserName;
  private String ppassword;
  private String pmail;
  private String pmailPassword;
  private double transferAmount;
  private String flag;
  private int index;
  private static Logger logger = LoggerFactory.getLogger(SimpleCrawlJob.class);

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
    LoginResult loginResult = LoginTask.tryTimes(
        userName,
        password);
    if (!loginResult.isActive()) {
      logger.info("用户[" + userName + "]登录失败");
      UserInfoFilterUtil.filterAndUpdateFlag(index, "0","用户[" + userName + "]登录失败");
      return;
    }
    logger.info("开始判断是否需要续期");
    LoginSuccessResult successResult = LoginSuccessTask.execute();
    if (!successResult.isRenewal()) {
      logger.info("用户[" + userName + "]不需要续期");
      UserInfoFilterUtil.filterAndUpdateFlag(index, "1","用户[" + userName + "]不需要续期");
      return;
    }
    logger.info("用户[" + userName + "]需要续期");
    RenewalParam param = successResult.filterIdValue();
    logger.info("用户[" + userName + "]续期参数" + param.toString());
    RenewalAmount amount = successResult.filterWallet();
    logger.info("用户[" + userName + "]续期钱包{" + amount.toString() + "}");
    if (RenewalUtil.usageFee(amount)) {
      ActFee actFee = RenewalUtil.actFee(amount);
      logger.info("用户[" + userName + "]续期参数" + actFee.toString());
      param.setPayOneAmount(String.valueOf(actFee.getAccash()));
      param.setPayTwoAmount(String.valueOf(actFee.getAcrewards()));
      param.setPayThreeAmount(String.valueOf(actFee.getAcsavings()));
      param.setInputAmount(String.valueOf(amount.getAmount()));
      int code = RenewalTask.execute(param);
      if (code == 200) {
        logger.info("用户[" + userName + "]续期成功");
        UserInfoFilterUtil.filterAndUpdateFlag(index, String.valueOf(amount.getAmount()),"用户[" + userName + "]续期成功");
        return;
      } else {
        logger.info("用户[" + userName + "]续期失败");
        UserInfoFilterUtil.filterAndUpdateFlag(index, "0","用户[" + userName + "]续期失败");
        return;
      }
    } else {
      logger.info("用户[" + userName + "]续期余额不足");
      UserInfoFilterUtil.filterAndUpdateFlag(index, "-1","用户[" + userName + "]续期余额不足");
      return;
    }

  }

  @Override
  public void afterRun() {
    SessionHolder.removeCookie();
  }

}
