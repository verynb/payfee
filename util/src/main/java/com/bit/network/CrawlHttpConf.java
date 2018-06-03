package com.bit.network;

import com.google.common.collect.Maps;

import java.util.Map;

import lombok.ToString;

/**
 * http的相关配置
 * 1. 请求参数头
 * 2. 返回的各项设置
 * Created by yuanj on 2017/6/27.
 */
@ToString
public class CrawlHttpConf {

    private Map<String, String> requestHeaders = Maps.newHashMap();//请求头
    private Map<String, Object> requestParams = Maps.newHashMap();//请求参数
    private static Map<String, String> DEFAULT_HEADERS = Maps.newHashMap();//默认头

    static {
        DEFAULT_HEADERS.put("accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        DEFAULT_HEADERS.put("Connection", "close");
        DEFAULT_HEADERS.put("user-agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        DEFAULT_HEADERS.put("accept-encoding", "gzip,deflate");
        DEFAULT_HEADERS.put("accept-language", "zh-CN,zh;q=0.8");
        DEFAULT_HEADERS.put("cache-control", "max-age=0");
        DEFAULT_HEADERS.put("upgrade-insecure-requests", "1");
    }

    public CrawlHttpConf() {
        this.requestHeaders.putAll(DEFAULT_HEADERS);
        requestHeaders.put("cookie", CookieUtil.cookieBuilder());
    }

    public CrawlHttpConf(Map<String, Object> requestParams) {
        this.requestHeaders.putAll(DEFAULT_HEADERS);
        this.requestHeaders.put("cookie", CookieUtil.cookieBuilder());
        this.requestParams = requestParams;
    }

    public enum HttpMethod {
        GET,
        POST,
        OPTIONS,
        PUT;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, Object> getRequestParams() {
        return requestParams;
    }

    public Map<String, Object> setRequestParams(Map<String, Object> param) {
        return this.requestParams = param;
    }

}
