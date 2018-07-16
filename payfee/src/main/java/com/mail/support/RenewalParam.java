package com.mail.support;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class RenewalParam {
    private String authenticityToken;
    private String payBondId;

    private String payOneAmount;
    private String payOneUserWalletId;
    private String payOneUerId;
    private String payTwoAmount;
    private String payTwoUserWalletId;
    private String payTwoUerId;
    private String payThreeAmount;
    private String payThreeUserWalletId;
    private String payThreeUerId;

    private String inputAmount;

    public RenewalParam(String authenticityToken,
                        String payBondId,
                        String payOneUserWalletId,
                        String payOneUerId,
                        String payTwoUserWalletId,
                        String payTwoUerId,
                        String payThreeUserWalletId,
                        String payThreeUerId) {
        this.authenticityToken = authenticityToken;
        this.payBondId = payBondId;
        this.payOneUserWalletId = payOneUserWalletId;
        this.payOneUerId = payOneUerId;
        this.payTwoUserWalletId = payTwoUserWalletId;
        this.payTwoUerId = payTwoUerId;
        this.payThreeUserWalletId = payThreeUserWalletId;
        this.payThreeUerId = payThreeUerId;

    }
}
