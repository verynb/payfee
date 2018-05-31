package com.bit.network;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yuanj on 9/21/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {

  private List<LocalCookie> cookies;
}
