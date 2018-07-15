package com.transfer.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Administrator on 2017/11/27.
 */
@Getter
@Setter
public class PayOutPageData {

  private String authToken = "";
  private String userAccountId = "";
  private List<PayOutWallet> payOutWallets = Lists.newArrayList();
  private Document doc = null;

  public PayOutPageData(Document doc, String accountName) {
    this.doc = doc;
    init(accountName);
  }

  private void init(String accountName) {
    if (doc == null) {
      return;
    }

    Element amonutElement = doc.select("select[name=partition_cashout_partition[user_account_id]]").first();
    if (Objects.isNull(amonutElement)) {
      return;
    }
    String amountId = amonutElement.children().stream()
        .filter(e -> StringUtils.isNotBlank(e.val()))
        .filter(e -> e.val().equals(accountName))
        .map(e -> e.val())
        .findFirst().orElse("");

    Element walletElement = doc.select("select[name=partition_cashout_partition[user_wallet_id]]").first();
    if (Objects.isNull(walletElement)) {
      return;
    }
    List<PayOutWallet> payOutWallets = walletElement.children().stream()
        .filter(e -> StringUtils.isNotBlank(e.val()))
        .map(e -> {
          String walletId = e.val();
          Double amount = Double.valueOf(e.text().substring(e.text().indexOf("$") + 1, e.text().length()));
          return new PayOutWallet(walletId, amount);
        }).collect(Collectors.toList());

    Element authTokenElement = doc.select("form[id=new_partition_cashout_partition]")
        .select("input[name=authenticity_token]").first();
    this.authToken = authTokenElement.val();
    this.userAccountId = amountId;
    this.payOutWallets = payOutWallets;
  }

  public Boolean isActive() {
    if (doc == null) {
      return false;
    }
    if (StringUtils.isBlank(this.userAccountId)) {
      return false;
    }
    if (CollectionUtils.isEmpty(this.payOutWallets)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TransferPageData{" +
        "authToken='" + authToken + '\'' +
        ", userAccountId='" + userAccountId + '\'' +
        ", payOutWallets=" + payOutWallets +
        '}';
  }
}