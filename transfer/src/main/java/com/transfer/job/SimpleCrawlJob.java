package com.transfer.job;

import com.bit.network.GetNetworkTime;
import com.bit.network.RandomUtil;
import com.transfer.entity.SendMailResult;
import com.transfer.entity.TransferPageData;
import com.transfer.entity.TransferParam;
import com.transfer.entity.TransferResult;
import com.transfer.entity.TransferUserInfo;
import com.transfer.entity.TransferWallet;
import com.transfer.entity.UserInfo;
import com.transfer.job.support.AbstractJob;
import com.transfer.mailClient.ImapMailToken;
import com.transfer.mailClient.MailTokenData;
import com.transfer.task.CancelTokenTask;
import com.transfer.task.GetReceiverTask;
import com.transfer.task.SendMailTask;
import com.transfer.task.TransferPageTask;
import com.transfer.task.TransferTask;
import com.transfer.task.TransferUtil;
import config.ThreadConfig;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class SimpleCrawlJob extends AbstractJob {

  private static Logger logger = LoggerFactory.getLogger(SimpleCrawlJob.class);
  private TransferUserInfo userInfo;
  //发邮件与收邮件时间间隔，默认10s
  private ThreadConfig config;

  public SimpleCrawlJob(TransferUserInfo userInfo,
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
      return;
    }
    //登录成功
    Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000 + 5000);
    transfer(this.userInfo.getEmail(), this.userInfo.getMailPassword(),
        this.userInfo.getTransferTo(),this.userInfo.getTransferAmount());

  }

  /**
   * 执行转账功能
   */
  private void transfer(String email, String mailPassword, String transferTo, Double transferAmount)
      throws InterruptedException {
    logger.info("开始抓取抓取转账页面数据");
    TransferPageData getTransferPage = TransferPageTask.tryTimes(config);
    if (CollectionUtils.isNotEmpty(getTransferPage.getTransferWallets())) {
      List<TransferWallet> filterList = getTransferPage.getTransferWallets()
          .stream()
          .filter(t -> t.getAmount() > 0)
          .collect(Collectors.toList());
      if (CollectionUtils.isEmpty(filterList)) {
        logger.info("转账金额没有大于0的数据");
        return;
      }
      if (!TransferUtil.enough(filterList, transferAmount)) {
        logger.info("转账总额小于[" + transferAmount + "]");
        return;
      }
      TransferWallet wallet = filterList.get(0);
      Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000 + 5000);
      UserInfo receiverInfo = GetReceiverTask.execute(transferTo);
      if (!Objects.isNull(receiverInfo) && receiverInfo.getResponse()) {
        Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000 + 5000);
        logger.info(
            "获取转出账户信息成功===>" + receiverInfo.toString());
        SendMailResult mailResult =
            SendMailTask
                .tryExcute(getTransferPage.getAuthToken(), getTransferPage.getTransferUserId(),
                    RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        if (!Objects.isNull(mailResult)) {//邮件发送成功的情况
          long mailStartTime = GetNetworkTime.getNetworkDatetime();
          long mailSpace = RandomUtil.ranNum(config.getMailSpaceTime()) * 1000 + 30000;
          logger.info(
              "休眠" + mailSpace + "ms后读取邮件");
          Thread.sleep(mailSpace);
          logger.info("开始读取邮件");
          List<MailTokenData> tokenData = tryReceiveMail(email, mailPassword, mailStartTime, mailSpace,
              config.getTransferErrorTimes());
          if (CollectionUtils.isEmpty(tokenData)) {
            throw new RuntimeException("获取邮件信息失败");
          } else {
            logger
                .info("邮件解析成功");
            transferByToken(email, mailPassword, getTransferPage, wallet, transferTo, receiverInfo, tokenData,
                transferAmount);
          }
        } else {
          throw new RuntimeException("获取邮件信息失败");
        }
      } else {
        throw new RuntimeException("获取转出人信息失败");
      }
    } else {
      throw new RuntimeException("抓取转账页面数据失败");
    }
  }

  private List<MailTokenData> tryReceiveMail(String email, String mailPassword, long mailStartTime, long mailSpace,
      int tryTimes)
      throws InterruptedException {
    for (int i = 1; i <= tryTimes; i++) {
      List<MailTokenData> tokenData = ImapMailToken
          .filterMailsForIsNew(userInfo.getUserName(), email, mailPassword);
      if (CollectionUtils.isEmpty(tokenData)) {
        long tryMailSpace = RandomUtil.ranNum(config.getMailSpaceTime()) * 1000 + 30000;
        logger
            .info("获取邮件失败,等待" + tryMailSpace + "ms重新获取");
        Thread.sleep(tryMailSpace);
        logger
            .info("重新获取邮件开始剩余重试次数" + (tryTimes - i));
      } else {
        return tokenData;
      }
    }
    return null;
  }

  private void transferByToken(String email,
      String mailPassword,
      TransferPageData getTransferPage,
      TransferWallet wallet,
      String transferTo,
      UserInfo receiverInfo,
      List<MailTokenData> tokenData, Double transferAmount) throws InterruptedException {
    TransferParam param = new TransferParam(getTransferPage.getAuthToken(),
        transferTo,
        wallet.getWalletId(),
        wallet.getAmount() - transferAmount >= 0 ? transferAmount : wallet.getAmount(),
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
//      transferByToken(email, mailPassword, getTransferPage, wallet, transferTo, receiverInfo, tokenData);
      String cancelStr = CancelTokenTask.execute();
      if (cancelStr.contains("success")) {
        logger.info("取消已有token成功");
        Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
        transfer(email, mailPassword, transferTo, transferAmount);
      } else {
        logger.info("取消已有token失败=" + cancelStr);
      }
    }
    if (transferCode.getStatus().equals("success")) {
      logger.info("转账成功，休眠500毫秒执行下一轮转账");
      Thread.sleep(RandomUtil.ranNum(config.getThreadspaceTime()) * 1000);
      logger.info("下一轮转账开始");
      transfer(email, mailPassword, transferTo,
          (wallet.getAmount() - transferAmount >= 0 ? 0 : transferAmount - wallet.getAmount()));
    } else {
      throw new RuntimeException("转账失败");
    }
  }

  @Override
  public void afterRun() {
  }
}
