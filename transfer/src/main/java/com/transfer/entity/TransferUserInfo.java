package com.transfer.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yuanj on 2017/12/1.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferUserInfo {

  private int row;
  private String userName;
  private String password;
  private String email;
  private String mailPassword;
  private List<String> transferTo;
  private String flag;
  private String flagMessage;

}
