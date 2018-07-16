package com.mail.support;

public class RenewalUtil {
    /**
     * 判断余额是否充足
     *
     * @param amount
     * @return
     */
    public static Boolean usageFee(final RenewalAmount amount) {
        double bance = amount.getWallets().stream()
                .mapToDouble(RenewalWallet::getWallet)
                .sum();
        return amount.getAmount() <= bance && amount.getAmount() > 0;
    }

    public static ActFee actFee(final RenewalAmount amount) {
        RenewalWallet savings = amount.getWallets().stream()
                .filter(a -> a.getWalletName().equals(WalletConstance.SAVINGS_WALLET))
                .findFirst().orElse(new RenewalWallet());
        RenewalWallet rewards = amount.getWallets().stream()
                .filter(a -> a.getWalletName().equals(WalletConstance.REWARDS_WALLET))
                .findFirst().orElse(new RenewalWallet());
        RenewalWallet cash = amount.getWallets().stream()
                .filter(a -> a.getWalletName().equals(WalletConstance.CASH_WALLET))
                .findFirst().orElse(new RenewalWallet());
        double amonut = amount.getAmount();
        double acsavings = savings.getWallet();
        double acrewards = rewards.getWallet();
        double accash = cash.getWallet();
        if (acsavings >= amonut) {
            acsavings = amonut;
            acrewards = 0;
            accash = 0;
        }
        if (acsavings + acrewards >= amonut) {
            acrewards = amonut - acsavings;
            accash = 0;
        }
        if (acsavings + acrewards + accash >= amonut) {
            accash = amonut - acsavings - acrewards;
        }
        return new ActFee(acsavings, acrewards, accash);
    }

}
