package identity;

import com.bit.network.GetNetworkTime;
import org.joda.time.DateTime;

/**
 * Created by yj on 2017/12/9.
 */
public class TimeCheck {

  private static final Long LIMIT_DAY = 1528646399000L;//天数限制610

  public static Boolean checkDay() {
    return GetNetworkTime.getNetworkDatetime() <= LIMIT_DAY;
  }

  public static Boolean checkMonth() {
    Long time = GetNetworkTime.getNetworkLimiteTime();
    if(time==null){
      throw new RuntimeException("取时间失败");
    }
    return GetNetworkTime.getNetworkDatetime() <= time;
  }

  public static Boolean checkVersion(String version) {
    String v = GetNetworkTime.getNetworkVersion();
    if(v==null){
      throw new RuntimeException("取时间失败");
    }
    return v.equals(version);
  }

  public static Boolean isCurrentDay(Long time) {
    DateTime dateTime = new DateTime(GetNetworkTime.getNetworkDatetime());
    return dateTime.minuteOfDay().withMinimumValue().isBefore(time) && dateTime.minuteOfDay().withMaximumValue()
        .isAfter(time);
  }
}
