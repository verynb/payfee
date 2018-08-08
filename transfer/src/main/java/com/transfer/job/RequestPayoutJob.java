package com.transfer.job;

import com.bit.network.SessionHolder;
import com.mail.api.MailTokenData;
import com.mail.support.FilterMailUtil;
import com.mail.support.LoginResult;
import com.transfer.entity.AddBitAccountParam;
import com.transfer.entity.PayOutPageData;
import com.transfer.entity.PayOutParam;
import com.transfer.entity.PayOutResult;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.PayOutWallet;
import com.transfer.job.support.AbstractJob;
import com.transfer.load.PayOutUserFilterUtil;
import com.transfer.task.AddBitAccountTask;
import com.transfer.task.PayOutTask;
import com.transfer.task.RequestPayoutPageTask;
import config.ThreadConfig;
import java.util.List;
import login.task.LoginTask;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Created by yj on 2017/6/27.
 */
@Getter
@Setter
public class RequestPayoutJob extends AbstractJob {

  private static Logger logger = LoggerFactory.getLogger(RequestPayoutJob.class);
  private PayOutUserInfo userInfo;
  //发邮件与收邮件时间间隔，默认10s
  private ThreadConfig config;

  public RequestPayoutJob(PayOutUserInfo userInfo,
      ThreadConfig config) {
    this.userInfo = userInfo;
    this.config = config;
  }

  @Override
  public void beforeRun() {

  }

  /**
   * 转账流程如下
   * 1.抓取登录静态页面，取到页面[authenticity_token]的值
   * 2.执行登录请求，获取登录后的cookies
   * 3.进入转账页面
   * 3-1：获取转账金额
   * 3-2：获取转账钱包ID
   * 3-3：获取转账人ID
   * 3-4：获取转账转账页面authenticity_token
   * 4.获取转账户信息
   * 5.向转账账户发邮件生成转账TOKEN
   * 6.登录邮件获取token
   * 7.完成转账
   */
  @Override
  public void doFetchPage() {
    LoginResult loginResult = LoginTask.tryTimes(
        this.userInfo.getAccount(),
        this.userInfo.getAccountPassword());
    if (!loginResult.isActive()) {
      logger.info("用户[" + this.userInfo.getAccount() + "]登录失败");
      PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "登录失败");
      //todo 会写失败日志
      return;
    }
    //登录成功
    transfer(this.userInfo.getMailbox(), this.userInfo.getMailboxPassword(),
        this.userInfo.getWalletName(), 100D);

  }

  private String addBitAddress(String addBitToken) {
    logger.info("添加火币地址");
    List<MailTokenData> tokenData = FilterMailUtil
        .filterAddMails(addBitToken, "", userInfo.getAccount(),
            userInfo.getMailbox(),
            userInfo.getMailboxPassword(),
            config.getTransferErrorTimes(), config.getMailSpaceTime());
    if (CollectionUtils.isNotEmpty(tokenData)) {
      PayOutResult payOutResult = AddBitAccountTask.execute(
          new AddBitAccountParam(addBitToken, userInfo.getWalletName(), userInfo.getWalletAddress(),
              tokenData.get(0).getToken()));
      if (payOutResult.getStatus().equals("error")) {
        PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "添加火币地址失败");
        return "error";
      } else {
        return "success";
      }
    } else {
      PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "添加火币token获取失败");
      return "error";
    }

  }

  /**
   * 执行体现功能
   */
  private void transfer(String email, String mailPassword, String walletName, Double mount) {
    PayOutPageData getTransferPage = RequestPayoutPageTask.execute(walletName);
    if (!getTransferPage.isActive()) {
      logger.info("抓取提现页面失败" + getTransferPage.toString());
      //todo 会写失败日志
      PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "抓取提现页面失败");
      return;
    }
    if (StringUtils.isBlank(getTransferPage.getUserAccountId())) {
      String addResult = addBitAddress(getTransferPage.getAddBitToken());
      if (addResult.equals("success")) {
        try {
          logger.info("等待10s重新提现");
          Thread.sleep(10000L);
        } catch (InterruptedException e) {
        } finally {
          transfer(email, mailPassword, walletName, mount);
        }
      } else {
        return;
      }
    }
    logger.info("抓取提现页面数据成功[" + getTransferPage.toString() + "]");
//    PayOutWallet wallet = getTransferPage.getPayOutWallets().get(0);
    PayOutWallet wallet = getTransferPage.getPayOutWallets()
        .stream()
        .filter(t -> t.getAmount() > mount)
        .findFirst().orElse(null);
    if (wallet == null) {
      logger.info("没有大于100的钱包[" + getTransferPage.toString() + "]");
      //todo 会写失败日志
      PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "1", "提现金额小于100");
      return;
    }
    List<MailTokenData> tokenData = FilterMailUtil
        .filterRequestMails(getTransferPage.getAuthToken(), "", userInfo.getAccount(),
            email, mailPassword, config.getTransferErrorTimes(),
            config.getMailSpaceTime());
    if (CollectionUtils.isEmpty(tokenData)) {
      logger.info("获取邮件信息失败");
      //todo 会写失败日志，记录状态
      PayOutUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "获取邮件信息失败");
      return;
    } else {
      logger.info("邮件解析成功");
      transferByToken(getTransferPage, wallet, tokenData);
    }
  }


  private void transferByToken(
      PayOutPageData getTransferPage,
      PayOutWallet wallet,
      List<MailTokenData> tokenData) {
    PayOutParam param = new PayOutParam(getTransferPage.getAuthToken(),
        getTransferPage.getUserAccountId(),
        wallet.getWalletId(),
        wallet.getAmount() > 200D ? 200 : wallet.getAmount(),
        tokenData.get(0).getToken()
    );
    logger.info("开始提现");
    logger.info("提现参数=" + param.toString());
    PayOutTask.execute(param, userInfo.getRow());
    return;
  }

  @Override
  public void afterRun() {
    SessionHolder.removeCookie();
  }


}
