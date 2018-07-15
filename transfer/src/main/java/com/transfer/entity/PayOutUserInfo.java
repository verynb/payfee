package com.transfer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yuanj on 2017/12/1.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayOutUserInfo {

  private int row;
  private String account;
  private String accountPassword;
  private String mailbox;
  private String mailboxPassword;
  private String walletName;
  private String walletAddress;
  private String flag;
  private String flagMessage;

}
