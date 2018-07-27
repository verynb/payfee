import com.transfer.job.TransferCrawlJob;
import config.ThreadConfig;
import com.mail.api.TransferUserInfo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by yuanj on 2018/6/13.
 */
public class TaskFactory {


  public static SimpleCrawlJob getSimpleInstance(final TransferUserInfo user, final ThreadConfig config) {
    return new SimpleCrawlJob(config, user.getUserName(),
        user.getPassword(), user.getPuserName(),
        user.getPpassword(), user.getPmail(),
        user.getPmailPassword(), user.getTransferAmount(),
        user.getFlag(), user.getIndex());
  }

  public static List<TransferCrawlJob> getTransferInstance(final List<TransferUserInfo> users,
      final ThreadConfig config) {

    List<TransferUserInfo> distincs = users.stream()
        .filter(distinctByKey(TransferUserInfo::getPuserName))
        .collect(Collectors.toList());

    List<com.transfer.entity.TransferUserInfo> transferUserInfos = distincs.stream()
        .map(d -> {
          List<String> transferTos = users.stream()
              .filter(u -> u.getPuserName().equals(d.getPuserName()))
              .map(TransferUserInfo::getUserName)
              .collect(Collectors.toList());
          return new com.transfer.entity.TransferUserInfo(d.getIndex(),
              d.getPuserName(),
              d.getPpassword(), d.getPmail(), d.getPmailPassword(), transferTos, "", "");
        }).collect(Collectors.toList());
    return transferUserInfos.stream()
        .map(userInfo -> new TransferCrawlJob(userInfo, config,200D))
        .collect(Collectors.toList());
  }

  public static <T> java.util.function.Predicate<T> distinctByKey(
      Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

}
