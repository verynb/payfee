package com.transfer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Created by yuanj on 2017/11/28.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfo {

  private String user_id;
  private String name;
  private boolean response;

  public String getUser_id() {
    return user_id;
  }

  public String getName() {
    return name;
  }

  public Boolean getResponse() {
    return response;
  }


  @Override
  public String toString() {
    return "UserInfo{" +
        "user_id='" + user_id + '\'' +
        ", name='" + name + '\'' +
        ", response=" + response +
        '}';
  }

  public Boolean isActive() {
    return response;
  }
}
