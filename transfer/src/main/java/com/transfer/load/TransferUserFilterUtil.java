package com.transfer.load;

import com.mail.support.ImapMailToken;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by yuanj on 2018/6/20.
 */
public class TransferUserFilterUtil {

  public static final List<TransferUserInfo> users = new CopyOnWriteArrayList();

  public static void filterAndUpdateFlag(int index, String flag, String message) {
    Optional<TransferUserInfo> filter = users.stream()
        .filter(u -> u.getRow() == index)
        .findFirst();
    if (filter.isPresent()) {
      filter.get().setFlag(flag);
      filter.get().setFlagMessage(message);
    }
  }

  public static <T> java.util.function.Predicate<T> distinctByKey(
      Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  public static void initMail() {
    List<TransferUserInfo> filters = users.stream()
        .filter(distinctByKey(u -> u.getEmail()))
        .collect(Collectors.toList());
    filters.forEach(f -> {
      ImapMailToken.init(f.getEmail(), f.getMailPassword());
    });
  }

}
