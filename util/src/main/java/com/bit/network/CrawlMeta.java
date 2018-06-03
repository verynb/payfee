package com.bit.network;


import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanj on 2017/6/27.
 */
@Data
public class CrawlMeta {
    private static Logger logger = LoggerFactory.getLogger(CrawlMeta.class);
    private String url;

    public static CrawlMeta getNewInstance(Class c, String url) {
        logger.debug("taskName[" + c.getName() + "]url[" + url + "]");
        return new CrawlMeta(url);
    }

    private CrawlMeta(String url) {
        this.url = url;
    }
}
