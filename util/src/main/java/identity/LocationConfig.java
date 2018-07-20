package identity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yuanj on 2018/7/20.
 */
@Value
public class LocationConfig {

  private String name;
  private String ver;
  private String timelimit;
  private String password;

  public Boolean filter() {
    return !(StringUtils.isBlank(name) || StringUtils.isBlank(ver) || StringUtils.isBlank(timelimit) || StringUtils
        .isBlank(password));
  }

}
