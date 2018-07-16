package com.transfer.job;

import com.bit.network.RandomUtil;
import com.bit.network.SessionHolder;
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
import com.transfer.task.SendMailTask;
import com.transfer.task.TransferPageTask;
import com.transfer.task.TransferTask;
import com.mail.api.UserInfoFilterUtil;
import config.ThreadConfig;
import java.util.List;
import java.util.stream.Collectors;
import login.task.LoginTask;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mail.support.LoginResult;

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

  public TransferCrawlJob(TransferUserInfo userInfo,
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
        this.userInfo.getUserName(),
        this.userInfo.getPassword());
    if (!loginResult.isActive()) {
      logger.info("用户[" + this.userInfo + "]登录失败");
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "-2a", "用户[" + this.userInfo + "]登录失败");
      //todo 会写失败日志
      return;
    }
    //登录成功
    transfer(this.userInfo.getEmail(), this.userInfo.getMailPassword(),
        this.userInfo.getTransferTo());

  }

  /**
   * 执行转账功能
   */
  private void transfer(String email, String mailPassword, String transferTo)
      throws InterruptedException {
    TransferPageData getTransferPage = TransferPageTask.tryTimes(config);
    if (!getTransferPage.isActive()) {
      logger.info("抓取转账页面失败" + getTransferPage.toString());
      //todo 会写失败日志
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "-1a", "抓取转账页面失败");
      return;
    }
    logger.info("抓取转账页面数据成功[" + getTransferPage.toString() + "]");
    List<TransferWallet> filterList = getTransferPage.getTransferWallets()
        .stream()
        .filter(t -> t.getAmount() > 0)
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(filterList)) {
      logger.info("转账金额没有大于0的数据[" + getTransferPage.toString() + "]");
      //todo 会写失败日志
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "0a", "转账金额小于0");
      return;
    }
    TransferWallet wallet = filterList.get(0);
    UserInfo receiverInfo = GetReceiverTask.execute(transferTo);
    if (!receiverInfo.isActive()) {
      logger.info("获取转出账户[" + transferTo + "]失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "2a", "获取转出账户[" + transferTo + "]失败");
      return;
    }
    logger.info("获取转出账户[" + transferTo + "]成功");
    SendMailResult mailResult =
        SendMailTask
            .tryExcute(getTransferPage.getAuthToken(), getTransferPage.getTransferUserId(),
                RandomUtil.ranNum(config.getThreadspaceTime()) * 1000, FilterMailUtil.TOKEN_TYPE_TRANSFER);
    if (!mailResult.isActive()) {
      logger.info("发送邮件[" + getTransferPage.getTransferUserId() + "]失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil
          .filterAndUpdateFlag(userInfo.getRow(), "3a", "发送邮件给[" + getTransferPage.getTransferUserId() + "]失败");
      return;
    }
    long mailSpace = RandomUtil.ranNum(config.getMailSpaceTime()) * 100 + 10000;
    logger.info(
        "休眠" + mailSpace + "ms后读取邮件");
    Thread.sleep(mailSpace);
    List<MailTokenData> tokenData = FilterMailUtil.filterTransferMails(userInfo.getUserName(),
        email, mailPassword,
        config.getTransferErrorTimes(),
        config.getMailSpaceTime());
    if (CollectionUtils.isEmpty(tokenData)) {
      logger.info("获取邮件信息失败");
      //todo 会写失败日志，记录状态
      UserInfoFilterUtil.filterAndUpdateFlag(userInfo.getRow(), "4a", "获取邮件信息失败");
      return;
    } else {
      logger.info("邮件解析成功");
      transferByToken(email, mailPassword, getTransferPage, wallet, transferTo, receiverInfo, tokenData);
    }
  }

  private void transferByToken(String email,
      String mailPassword,
      TransferPageData getTransferPage,
      TransferWallet wallet,
      String transferTo,
      UserInfo receiverInfo,
      List<MailTokenData> tokenData) throws InterruptedException {
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
    TransferResult transferCode = TransferTask.execute(param);
    if (transferCode.getError().equals("invalid_token")) {
      tokenData.remove(0);
      Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
      logger.info("取消已有token");
      String cancelStr = CancelTokenTask.execute(FilterMailUtil.TOKEN_TYPE_TRANSFER);
      if (cancelStr.contains("success")) {
        logger.info("取消已有token成功");
        Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        transfer(email, mailPassword, transferTo);
      } else {
        logger.info("取消已有token失败=" + cancelStr);
      }
    }
    if (transferCode.getStatus().equals("success")) {
        logger.info("转账成功，休眠500毫秒执行下一轮转账");
        Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        logger.info("下一轮转账开始");
        transfer(email, mailPassword, transferTo);
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
