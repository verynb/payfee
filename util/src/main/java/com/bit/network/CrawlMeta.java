package com.bit.network;


import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Data;

/**
 * Created by yuanj on 2017/6/27.
 */
@Data
public class CrawlMeta {

  private String url;
  private Set<String> selectorRules;

  public CrawlMeta(String url) {
    this.url = url;
    this.selectorRules = Sets.newHashSet();
  }

  public CrawlMeta(String url, Set<String> selectorRules) {
    this.url = url;
    this.selectorRules = selectorRules;
  }
}
