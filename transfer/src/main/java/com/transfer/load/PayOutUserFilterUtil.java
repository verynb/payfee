package com.transfer.load;

import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yuanj on 2018/6/20.
 */
public class PayOutUserFilterUtil {

  public static final List<PayOutUserInfo> users = new CopyOnWriteArrayList();

  public static void filterAndUpdateFlag(int index, String flag,String message) {
    Optional<PayOutUserInfo> filter = users.stream()
        .filter(u -> u.getRow() == index)
        .findFirst();
    if (filter.isPresent()) {
      filter.get().setFlag(flag);
      filter.get().setFlagMessage(message);
    }
  }

}
