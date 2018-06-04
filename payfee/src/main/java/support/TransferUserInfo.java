package support;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yuanj on 2017/12/1.
 */

@Getter
@Setter
public class TransferUserInfo {

  private int row;
  private String userName;
  private String password;
  private Double flag;

  public TransferUserInfo(int row, String userName, String password, Double flag) {
    this.row = row;
    this.userName = userName;
    this.password = password;
    this.flag = flag;
  }

  public Boolean filterUserInfo() {
    Boolean status = true;
    if (StringUtils.isBlank(this.getUserName())) {
      status = false;
    }
    if (StringUtils.isBlank(this.getPassword())) {
      status = false;
    }
    return status;
  }

  @Override
  public String toString() {
    return "TransferUserInfo{" +
        "续期帐号='" + userName + '\'' + '}';
  }
}
