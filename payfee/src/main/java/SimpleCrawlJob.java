import RanewalJob.AbstractJob;
import com.bit.network.SessionHolder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.*;

/**
 * <p>
 * Created by yj on 2017/6/27.
 */
@Getter
@Setter
public class SimpleCrawlJob extends AbstractJob {

    private ThreadConfig config;
    private String userName;
    private String password;
    private static Logger logger = LoggerFactory.getLogger(SimpleCrawlJob.class);
    private static Logger transferSuccessLogger = LoggerFactory.getLogger("transferSuccessLogger");
    private static Logger transferFailueLogger = LoggerFactory.getLogger("transferFailueLogger");

    public SimpleCrawlJob(ThreadConfig config, String userName, String password) {
        this.config = config;
        this.userName = userName;
        this.password = password;
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
            System.out.print("对应异常处理");
            //todo 对应异常处理
        }
        LoginSuccessResult successResult = LoginSuccessTask.execute();
        if (!successResult.isRenewal()) {
            System.out.print("不需要续期做对应处理");
            //todo 不需要续期做对应处理
        }
        RenewalParam param = successResult.filterIdValue();
        RenewalAmount amount = successResult.filterWallet();
        if (RenewalUtil.usageFee(amount)) {
            ActFee actFee = RenewalUtil.actFee(amount);
            param.setPayOneAmount(String.valueOf(actFee.getAccash()));
            param.setPayTwoAmount(String.valueOf(actFee.getAcrewards()));
            param.setPayThreeAmount(String.valueOf(actFee.getAcsavings()));
            param.setInputAmount(String.valueOf(amount.getAmount()));
            RenewalTask.execute(param);
        } else {
            System.out.print("余额不足异常处理");
            //todo 余额不足异常处理
        }
    }

    @Override
    public void afterRun() {
        SessionHolder.removeCookie();
    }
}
