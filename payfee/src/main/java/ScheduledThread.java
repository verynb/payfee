import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/2.
 */
public class ScheduledThread {

    private static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
    private static String version = "1.1";

    public static void main(String[] args) {
        ThreadConfig config = new ThreadConfig(1, 100, 50);
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(config.getThreadPoolSize());
        scheduledThreadPool.schedule(new SimpleCrawlJob(config,
                        "hyyi06", "puffs258180"),
                5, TimeUnit.SECONDS);
        scheduledThreadPool.shutdown();
        while (true) {
            if (scheduledThreadPool.isTerminated()) {
                break;
            }
        }
    }
}
