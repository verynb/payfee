package com.transfer.task;

import com.transfer.entity.TransferWallet;
import java.util.List;

/**
 * Created by yuanj on 2018/6/13.
 */
public class TransferUtil {

  public static Boolean enough(List<TransferWallet> wallets, Double tranferAmount) {
    double total = wallets.stream()
        .mapToDouble(TransferWallet::getAmount)
        .sum();
    return total >= tranferAmount;
  }

}
