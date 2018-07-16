package com.mail.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class LoginResult {

    private int code;
    private String result;

    public boolean isActive() {
        return code == 200;
    }
}
