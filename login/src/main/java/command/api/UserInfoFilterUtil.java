package command.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yuanj on 2018/6/20.
 */
public class UserInfoFilterUtil {

  public static final List<TransferUserInfo> users = new CopyOnWriteArrayList();

  public static void filterAndUpdateFlag(int index, String flag,String message) {
    Optional<TransferUserInfo> filter = users.stream()
        .filter(u -> u.getIndex() == index)
        .findFirst();
    if (filter.isPresent()) {
      filter.get().setFlag(flag);
      filter.get().setFlagMessage(message);
    }
  }

}