package com.transfer.job;

import com.bit.network.GetNetworkTime;
import com.bit.network.RandomUtil;
import com.bit.network.SessionHolder;
import com.transfer.entity.PayOutPageData;
import com.transfer.entity.PayOutParam;
import com.transfer.entity.PayOutResult;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.PayOutWallet;
import com.transfer.entity.SendMailResult;
import com.transfer.entity.TransferPageData;
import com.transfer.entity.TransferParam;
import com.transfer.entity.TransferResult;
import com.transfer.entity.TransferUserInfo;
import com.transfer.entity.TransferWallet;
import com.transfer.entity.UserInfo;
import com.transfer.job.support.AbstractJob;
import com.transfer.mailClient.FilterMailUtil;
import com.transfer.mailClient.MailTokenData;
import com.transfer.task.CancelTokenTask;
import com.transfer.task.GetReceiverTask;
import com.transfer.task.PayOutTask;
import com.transfer.task.RequestPayoutPageTask;
import com.transfer.task.SendMailTask;
import com.transfer.task.TransferPageTask;
import com.transfer.task.TransferTask;
import com.transfer.task.TransferUtil;
import command.api.UserInfoFilterUtil;
import config.ThreadConfig;
import java.util.List;
import java.util.stream.Collectors;
import login.task.LoginTask;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.LoginResult;

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
  public void doFetchPage() throws Exception {
    LoginResult loginResult = LoginTask.tryTimes(config.getTransferErrorTimes(),
        config.getThreadspaceTime(),
        this.userInfo.getAccount(),
        this.userInfo.getAccountPassword());
    if (!loginResult.isActive()) {
      logger.info("用户[" + this.userInfo + "]登录失败");
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "-2a", "用户[" + this.userInfo + "]登录失败");
      //todo 会写失败日志
      return;
    }
    //登录成功
    transfer(this.userInfo.getMailbox(), this.userInfo.getMailboxPassword(),
        this.userInfo.getWalletName(), 200D);

  }

  /**
   * 执行体现功能
   */
  private void transfer(String email, String mailPassword, String walletName, Double mount)
      throws InterruptedException {
    PayOutPageData getTransferPage = RequestPayoutPageTask.tryTimes(userInfo, config, walletName);
    if (!getTransferPage.isActive()) {
      logger.info("抓取提现页面失败" + getTransferPage.toString());
      //todo 会写失败日志
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "-1a", "抓取提现页面失败");
      return;
    }
    logger.info("抓取提现页面数据成功[" + getTransferPage.toString() + "]");
    List<PayOutWallet> filterList = getTransferPage.getPayOutWallets()
        .stream()
        .filter(t -> t.getAmount() > 0)
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(filterList)) {
      logger.info("提现金额没有大于0的数据[" + getTransferPage.toString() + "]");
      //todo 会写失败日志
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0a", "提现金额小于0");
      return;
    }
    if (!TransferUtil.enoughPayOut(filterList, mount)) {
      logger.info("提现总额小于[" + mount + "]");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "1a", "提现总额小于[" + mount + "]");
      return;
    }
    PayOutWallet wallet = filterList.get(0);
    SendMailResult mailResult =
        SendMailTask
            .tryExcute(getTransferPage.getAuthToken(), "",
                RandomUtil.ranNum(config.getThreadspaceTime()) * 1000, FilterMailUtil.TOKEN_TYPE_REQUEST_PAYOUT);
    if (!mailResult.isActive()) {
      logger.info("发送邮件[" + "" + "]失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil
          .filterAndUpdateFlag(userInfo.getRow(), "3a", "发送邮件给[" + "" + "]失败");
      return;
    }
    long mailSpace = RandomUtil.ranNum(config.getMailSpaceTime()) * 100 + 10000;
    logger.info(
        "休眠" + mailSpace + "ms后读取邮件");
    Thread.sleep(mailSpace);
    List<MailTokenData> tokenData = FilterMailUtil.filterRequestMails(userInfo.getAccount(),
        email, mailPassword, config.getTransferErrorTimes(),
        config.getMailSpaceTime());
    if (CollectionUtils.isEmpty(tokenData)) {
      logger.info("获取邮件信息失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "4a", "获取邮件信息失败");
      return;
    } else {
      logger.info("邮件解析成功");
      transferByToken(email, mailPassword, getTransferPage, wallet, tokenData, mount);
    }
  }


  private void transferByToken(String email,
      String mailPassword,
      PayOutPageData getTransferPage,
      PayOutWallet wallet,
      List<MailTokenData> tokenData,
      Double transferAmount) throws InterruptedException {
    PayOutParam param = new PayOutParam(getTransferPage.getAuthToken(),
        getTransferPage.getUserAccountId(),
        wallet.getWalletId(),
        wallet.getAmount() - transferAmount >= 0 ? transferAmount : wallet.getAmount(),
        tokenData.get(0).getToken()
    );
    logger.info("开始提现");
    logger.info("提现参数=" + param.toString());
    PayOutResult transferCode = PayOutTask.execute(param);
    if (transferCode.getError().equals("invalid_token")) {
      tokenData.remove(0);
      Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
      logger.info("取消已有token");
      String cancelStr = CancelTokenTask.execute(FilterMailUtil.TOKEN_TYPE_REQUEST_PAYOUT);
      if (cancelStr.contains("success")) {
        logger.info("取消已有token成功");
        Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        transfer(email, mailPassword, getTransferPage.getUserAccountId(), transferAmount);
      } else {
        logger.info("取消已有token失败=" + cancelStr);
      }
    }
    if (transferCode.getStatus().equals("success")) {
      logger.info("提现成功");
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "5a", "提现成功");
      return;
    } else {
      logger.info("转账失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "5a", "转账失败");
      return;
    }
  }

  @Override
  public void afterRun() {
    SessionHolder.removeCookie();
  }


}
