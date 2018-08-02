package com.transfer.load;

import static com.transfer.load.TransferUserFilterUtil.distinctByKey;

import com.mail.support.ImapMailToken;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

  public static void initMail() {
    List<PayOutUserInfo> filters = users.stream()
        .filter(distinctByKey(u -> u.getMailbox()))
        .collect(Collectors.toList());
    filters.forEach(f -> {
      ImapMailToken.init(f.getMailbox(), f.getMailboxPassword());
    });
  }

}
