package com.bit.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yj on 2017/11/25.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocalCookie {

  private String sessionKey;

  private String sessionValue;
}
