package command.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/12/1.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferUserInfo {

  private static Logger logger = LoggerFactory.getLogger(TransferUserInfo.class);
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
    String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    Pattern regex = Pattern.compile(check);
    Matcher matcher = regex.matcher(this.getPmail());
    Boolean status = true;
    if (StringUtils.isBlank(this.getUserName())) {
      status = false;
    }
    if (StringUtils.isBlank(this.getPassword())) {
      status = false;
    }
    if (!matcher.matches()) {
      logger.info("mail[" + this.getPmail() + "]");
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
