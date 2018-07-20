package com.transfer.entity;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

  private String addBitToken = "";
  private String authToken = "";
  private String userAccountId = "";
  private List<PayOutWallet> payOutWallets = Lists.newArrayList();
  private Document doc = null;
  private int ableWalletSize;

  public PayOutPageData(Document doc, String accountName) {
    this.doc = doc;
    init(accountName);
  }

  private void init(String accountName) {
    if (doc == null) {
      return;
    }

    Element amonutElement = doc.select("select[id=partition_cashout_partition_user_account_id]").first();
    if (Objects.isNull(amonutElement)) {
      return;
    }
    String amountId = amonutElement.children().stream()
        .filter(e -> StringUtils.isNotBlank(e.text()))
        .filter(e -> e.text().equals(accountName))
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
        }).sorted(Comparator.comparing(PayOutWallet::getAmount).reversed())
        .collect(Collectors.toList());

    Element authTokenElement = doc.select("form[id=new_partition_cashout_partition]")
        .select("input[name=authenticity_token]").first();

    Element addBitElement = doc.select("form[id=new_user_account]")
        .select("input[name=authenticity_token]").first();

    this.addBitToken = addBitElement.val();
    this.authToken = authTokenElement.val();
    this.userAccountId = amountId;
    this.payOutWallets = payOutWallets;
    this.ableWalletSize = (int) getPayOutWallets()
        .stream()
        .filter(t -> t.getAmount() > 10D)
        .count();
  }

  public Boolean isActive() {
    if (doc == null) {
      return false;
    }
    if (CollectionUtils.isEmpty(this.payOutWallets)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "PayOutPageData{" +
        "authToken='" + authToken + '\'' +
        ", userAccountId='" + userAccountId + '\'' +
        ", payOutWallets=" + payOutWallets +
        '}';
  }
}
