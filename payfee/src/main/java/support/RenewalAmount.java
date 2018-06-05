package support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RenewalAmount {

  private double amount;
  private List<RenewalWallet> wallets;

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("\n");
    builder.append("续期金额[" + this.amount + "]");
    builder.append("\n");
    wallets.forEach(w -> {
      builder.append("钱包[" + w.getWalletName() + "]-->" + "可用额度[" + w.getWallet() + "]");
      builder.append("\n");
    });
    return builder.toString();
  }
}
