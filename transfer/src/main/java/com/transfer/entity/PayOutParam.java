package com.transfer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Created by yuanj on 2017/11/29.
 */
@AllArgsConstructor
@Data
@ToString
public class PayOutParam {

  private String authenticityToken;
  private String userAccountId;
  private String userWalletId;
  private Double amount;
  private String token;

}
