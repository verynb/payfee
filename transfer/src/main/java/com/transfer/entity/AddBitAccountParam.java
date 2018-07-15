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
public class AddBitAccountParam {

  private String authenticityToken;
  private String accountName;
  private String bitcoinAddress;
  private String token;

}
