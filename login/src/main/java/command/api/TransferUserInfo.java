package command.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yuanj on 2017/12/1.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferUserInfo {

  private String userName;
  private String password;
  private String puserName;
  private String ppassword;
  private String pmail;
  private String pmailPassword;
  private double transferAmount;
  private String flag;
  private String flagMessage;
  private int index;


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
    return "RenewalUserInfo{" +
        "续期帐号='" + userName + '\'' + '}';
  }
}
