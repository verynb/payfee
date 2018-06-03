import com.bit.network.CrawlHttpConf;
import com.bit.network.CrawlMeta;
import com.bit.network.HttpPostResult;
import com.bit.network.HttpUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.RenewalIdConstance;
import support.RenewalParam;

import java.util.Map;

import static support.RenewalIdConstance.*;

/**
 * Created by yuanj on 2017/11/27.
 */
@Slf4j
public class RenewalTask {

    private static Logger logger = LoggerFactory.getLogger(RenewalTask.class);
    private static String URL = "https://www.bitbackoffice.com/bond_payments";

    private static Map getParam(RenewalParam param) {
        Map<String, String> paramMap = Maps.newHashMap();
        paramMap.put(RenewalIdConstance.AUTHENTICITYTOKEN, param.getAuthenticityToken());
        paramMap.put(RenewalIdConstance.PAYBONDID, param.getPayBondId());

        paramMap.put(PAYONEAMOUNT, param.getPayOneAmount());
        paramMap.put(PAYONEUSERWALLETID, param.getPayOneUserWalletId());
        paramMap.put(PAYONEUERID, param.getPayOneUerId());

        paramMap.put(PAYTWOAMOUNT, param.getPayTwoAmount());
        paramMap.put(PAYTWOUSERWALLETID, param.getPayTwoUserWalletId());
        paramMap.put(PAYTWOUERID, param.getPayTwoUerId());

        paramMap.put(PAYTHREEAMOUNT, param.getPayThreeAmount());
        paramMap.put(PAYTHREEUSERWALLETID, param.getPayThreeUserWalletId());
        paramMap.put(PAYTHREEUERID, param.getPayThreeUerId());

        paramMap.put(INPUTAMOUNT, param.getInputAmount());
        return paramMap;
    }

    public static int execute(RenewalParam param) {
        HttpPostResult response = null;
        try {
            CrawlHttpConf conf = new CrawlHttpConf(getParam(param));
            response = HttpUtils
                    .doPost(CrawlMeta.getNewInstance(RenewalTask.class, URL), conf);
            String returnStr = EntityUtils.toString(response.getResponse().getEntity());
            logger.debug("续期返回值"+returnStr);
            if(returnStr.contains("success")){
                return 200;
            }else {
                return 400;
            }
        } catch (Exception e) {
            return 500;
        } finally {
            response.getHttpPost().releaseConnection();
            response.getHttpClient().getConnectionManager().shutdown();
        }
    }
}
