package identity;

import com.bit.network.GetNetworkTime;
import java.text.SimpleDateFormat;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yj on 2017/12/9.
 */
public class TimeCheck {

  private static final Long LIMIT_DAY = 1528646399000L;//天数限制610
  private static final String FORMART = "yyyy-MM-dd HH:mm:ss";
  private static Logger logger = LoggerFactory.getLogger(TimeCheck.class);

  public static Boolean checkMonth(String time) {
    Long timeMills = 1601485200000L;
    logger.info("验证时间[" + time + "]");
    try {
      timeMills = new SimpleDateFormat(FORMART).parse(time).getTime();
    } catch (Exception e) {
      logger.info("验证时间[" + e.getMessage() + "]");
    }
    return GetNetworkTime.getNetworkDatetime() <= timeMills;
  }
}
