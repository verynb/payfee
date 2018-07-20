package com.transfer.entity;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Administrator on 2017/11/27.
 */
@Getter
@Setter
public class TransferPageData {

  private String authToken = "";
  private String transferUserId = "";
  private List<TransferWallet> transferWallets = Lists.newArrayList();
  private Document doc = null;

  public TransferPageData(Document doc) {
    this.doc = doc;
    init();
  }

  private void init() {
    if (doc == null) {
      return;
    }
    Element walletElement = doc.select("select[name=partition_transfer_partition[user_wallet_id]]").first();
    if (Objects.isNull(walletElement)) {
      return;
    }
    List<TransferWallet> transferWallets = walletElement.children().stream()
        .filter(e -> StringUtils.isNotBlank(e.val()))
        .map(e -> {
          String walletId = e.val();
          Double amount = Double.valueOf(e.text().substring(e.text().indexOf("$") + 1, e.text().length()));
          return new TransferWallet(walletId, amount);
        })
        .sorted(Comparator.comparing(TransferWallet::getAmount).reversed())
        .collect(Collectors.toList());

    Element authTokenElement = doc.select("form[id=new_partition_transfer_partition]")
        .select("input[name=authenticity_token]").first();

    Element transferUserIdElement = doc.select("input[name=partition_transfer_partition[user_id]]").first();
    this.authToken = authTokenElement.val();
    this.transferUserId = transferUserIdElement.val();
    this.transferWallets = transferWallets;

  }

  public Boolean isActive() {
    if (doc == null) {
      return false;
    }
    if (CollectionUtils.isEmpty(this.transferWallets)) {
      return false;
    }
    return true;
  }

  public Boolean walletAmont() {
    List<TransferWallet> filterList = getTransferWallets()
        .stream()
        .filter(t -> t.getAmount() > 0)
        .collect(Collectors.toList());
    return filterList.size() > 0;
  }

  @Override
  public String toString() {
    return "TransferPageData{" +
        "authToken='" + authToken + '\'' +
        ", transferUserId='" + transferUserId + '\'' +
        ", transferWallets=" + transferWallets +
        '}';
  }
}
