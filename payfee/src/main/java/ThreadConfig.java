import lombok.Value;

/**
 * Created by yuanj on 2017/12/6.
 */
@Value
public class ThreadConfig {

    private Integer transferErrorTimes;
    private Integer threadspaceTime;
    private Integer threadPoolSize;

    public ThreadConfig(Integer transferErrorTimes, Integer threadspaceTime, Integer threadPoolSize) {
        this.transferErrorTimes = transferErrorTimes;
        this.threadspaceTime = threadspaceTime;
        this.threadPoolSize = threadPoolSize;
    }

    public Integer getTransferErrorTimes() {
        return transferErrorTimes;
    }

    public Integer getThreadspaceTime() {
        return threadspaceTime;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

}
