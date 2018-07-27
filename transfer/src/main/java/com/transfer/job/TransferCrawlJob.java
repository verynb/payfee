package com.transfer.job;

import com.bit.network.SessionHolder;
import com.mail.api.MailTokenData;
import com.mail.support.FilterMailUtil;
import com.mail.support.LoginResult;
import com.transfer.entity.TransferPageData;
import com.transfer.entity.TransferParam;
import com.transfer.entity.TransferUserInfo;
import com.transfer.entity.TransferWallet;
import com.transfer.entity.UserInfo;
import com.transfer.job.support.AbstractJob;
import com.transfer.load.TransferUserFilterUtil;
import com.transfer.task.GetReceiverTask;
import com.transfer.task.TransferPageTask;
import com.transfer.task.TransferTask;
import config.ThreadConfig;
import java.util.List;
import login.task.LoginTask;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Created by yj on 2017/6/27.
 */
@Getter
@Setter
public class TransferCrawlJob extends AbstractJob {

  private static Logger logger = LoggerFactory.getLogger(TransferCrawlJob.class);
  private TransferUserInfo userInfo;
  //发邮件与收邮件时间间隔，默认10s
  private ThreadConfig config;

  private Double transferAmonut;

  public TransferCrawlJob(TransferUserInfo userInfo,
      ThreadConfig config,
      Double transferAmonut) {
    this.userInfo = userInfo;
    this.config = config;
    this.transferAmonut = transferAmonut;
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
        this.userInfo.getUserName(),
        this.userInfo.getPassword());
    if (!loginResult.isActive()) {
      logger.info("用户[" + this.userInfo.getUserName() + "]登录失败");
      TransferUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "登录失败");
      return;
    }
    //登录成功,执行转账
    for (int i = 0; i < this.userInfo.getTransferTo().size(); i++) {
      String u = this.userInfo.getTransferTo().get(i);
      logger.info("transferTo[" + u + "]");
      TransferPageData getTransferPage = TransferPageTask.execute(userInfo.getRow());
      if (getTransferPage == null || !getTransferPage.isActive()) {
        TransferUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "抓取转账页面数据失败");
        return;
      }
      if (!getTransferPage.walletAmont()) {
        logger.info("钱包金额为0[" + getTransferPage.toString() + "]");
        TransferUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "1", "转账成功");
        return;
      }
      logger.info("抓取转账页面数据成功[" + getTransferPage.toString() + "]");
      transfer(this.userInfo.getEmail(), this.userInfo.getMailPassword(), u, getTransferPage);
      if (this.transferAmonut == null) {
        logger.info("休眠2s处理下个钱包");
      } else {
        logger.info("休眠2s处理下个账户");
      }
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 执行转账功能
   */
  private void transfer(String email, String mailPassword, String transferTo, TransferPageData getTransferPage) {

    TransferWallet wallet = null;
    if (this.transferAmonut == null) {
      wallet = getTransferPage.getTransferWallets().get(0);
    } else {
      wallet = getTransferPage.getTransferWallets().stream()
          .filter(p -> p.getAmount() >= this.transferAmonut)
          .findFirst().orElse(null);
    }
    if (wallet == null) {
      logger.info("转账余额不足");
      TransferUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "转账余额不足");
      return;
    }
    UserInfo receiverInfo = GetReceiverTask.execute(transferTo, userInfo.getRow());
    if (!receiverInfo.isActive()) {
      return;
    }
    List<MailTokenData> tokenData = FilterMailUtil
        .filterTransferMails(getTransferPage.getAuthToken(),
            getTransferPage.getTransferUserId(),
            userInfo.getUserName(),
            email, mailPassword,
            config.getTransferErrorTimes(),
            config.getMailSpaceTime());
    if (CollectionUtils.isEmpty(tokenData)) {
      logger.info("获取邮件信息失败");
      //todo 会写失败日志，记录状态
      TransferUserFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0", "获取邮件信息失败");
      return;
    } else {
      logger.info("邮件解析成功");
      transferByToken(getTransferPage, wallet, transferTo, receiverInfo, tokenData);
    }
  }

  private void transferByToken(
      TransferPageData getTransferPage,
      TransferWallet wallet,
      String transferTo,
      UserInfo receiverInfo,
      List<MailTokenData> tokenData) {
    TransferParam param = new TransferParam(getTransferPage.getAuthToken(),
        transferTo,
        wallet.getWalletId(),
        wallet.getAmount(),
        tokenData.get(0).getToken(),
        getTransferPage.getTransferUserId(),
        receiverInfo.getUser_id()
    );
    logger.info("开始转账");
    logger.info("转账参数=" + param.toString());
    TransferTask.execute(param, userInfo.getRow());
    return;
  }

  @Override
  public void afterRun() {
    SessionHolder.removeCookie();
  }


}
