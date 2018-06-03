package support;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginSuccessResult {
    private int code;
    private Document doc;

    /**
     * 是否需要续期
     *
     * @return
     */
    public boolean isRenewal() {
        if (code != 200) return false;
        if (doc == null) return false;
        Element e = doc.select("button[class=btn-action]button[id=rew-daily-home]").first();
        if (e != null) {
            if (e.text().equals("Pay Now!") || e.text().equals("立即付款!")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public RenewalAmount filterWallet() {
        if (!isRenewal()) return new RenewalAmount(0, Lists.newArrayList());
        Elements uls = filterUl();
        String amontStr = uls.select("li[class=list-group-item]").last()
                .select("label[class=label label-danger pull-right]").first()
                .text();
        List<RenewalWallet> wallets = uls.stream().map(e -> filterWallet(e))
                .filter(e -> !e.getWalletName().equals(WalletConstance.COMMISSIONS_WALLET))
                .collect(Collectors.toList());
        return new RenewalAmount(Double.valueOf(amontStr.substring(1)), wallets);
    }

    public RenewalParam filterIdValue() {
        if (!isRenewal()) return new RenewalParam();
        Element div = doc.select("div[id=renew-payment]").first();
        Elements uls = div.select("ul[class=list-group]");
        String authenticityToken = doc.select("input[name=" + RenewalIdConstance.AUTHENTICITYTOKEN).first().val();
        String payBondId = div.select("input[name=" + RenewalIdConstance.PAYBONDID).first().val();

        String eOneUserId = uls.select("input[name=" + RenewalIdConstance.PAYONEUERID + "]").first().val();
        String eOneWalletId = uls.select("input[name=" + RenewalIdConstance.PAYONEUSERWALLETID + "]").first().val();

        String eTwoUserId = uls.select("input[name=" + RenewalIdConstance.PAYTWOUERID + "]").first().val();
        String eTwoWalletId = uls.select("input[name=" + RenewalIdConstance.PAYTWOUSERWALLETID + "]").first().val();

        String eThreeUserId = uls.select("input[name=" + RenewalIdConstance.PAYTHREEUERID + "]").first().val();
        String eThreeWalletId = uls.select("input[name=" + RenewalIdConstance.PAYTHREEUSERWALLETID + "]").first().val();
        return new RenewalParam(authenticityToken, payBondId,
                eOneWalletId, eOneUserId,
                eTwoWalletId, eTwoUserId,
                eThreeWalletId, eThreeUserId);
    }

    private Elements filterUl() {
        Element div = doc.select("div[id=renew-payment]").first();
        Elements uls = div.select("ul[class=list-group]");
        return uls;
    }

    private RenewalWallet filterWallet(Element e) {
        Element li = e.select("li[class=list-group-item]").first();
        String walletNameContact = li.text();
        int space = walletNameContact.indexOf(" ");
        String walletMoney = walletNameContact.substring(0, space);
        String walletName = walletNameContact.substring(space + 1);
        RenewalWallet wallet = new RenewalWallet(walletName, Double.valueOf(walletMoney.substring(1)));
        return wallet;
    }
}
