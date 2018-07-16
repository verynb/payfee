package com.mail.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by yuanj on 2017/11/29.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class LoginAuthTokenData {
    private int code;
    private String result;

    public boolean isActive() {
        return code == 200;
    }
}
