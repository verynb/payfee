package load;

import com.google.common.collect.Lists;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.TransferUserInfo;

@Slf4j
public class LoadData {

  private static Logger logger = LoggerFactory.getLogger(LoadData.class);

  public static List<TransferUserInfo> loadUserInfoData(String filePath) {
    CsvReader csvReader = new CsvReader();
    csvReader.setContainsHeader(true);
    CsvContainer csv = null;
    List<TransferUserInfo> userInfos = Lists.newArrayList(new TransferUserInfo("hyyi08", "puffs258180",
        "lhha003", "hh8389lhhl", "lianghuihua01@bookbitbtc.com", "SHENzen007v", 200, "0", 1));
    try {
      csv = csvReader.read(Paths.get(filePath), StandardCharsets.UTF_8);
      if (csv == null) {
      }
      int i = 0;
      for (CsvRow row : csv.getRows()) {
        double account = row.getField("转帐金额") == null ? 200 : Double.valueOf(row.getField("转帐金额"));
        i++;
        TransferUserInfo userInfo = new TransferUserInfo(
            row.getField("用户id"),
            row.getField("用户密码"),
            row.getField("上层帐号"),
            row.getField("上层帐号密码"),
            row.getField("上层帐号邮件"),
            row.getField("上层帐号邮件密码"),
            account,
            row.getField("flag"),
            i
        );
        if (userInfo.filterUserInfo()) {
          userInfos.add(userInfo);
        }
      }
    } catch (IOException e) {
      logger.info("加载用户数据失败,请检查account.csv格式是否正确");
      System.out.println("输入任意结束:");
      Scanner scan = new Scanner(System.in);
      String read = scan.nextLine();
      while (StringUtils.isBlank(read)) {
      }
      System.exit(0);
    }
    logger.info("加载用户数据成功");
    return userInfos;
  }


  public static void writeResult(List<TransferUserInfo> userInfos) {
    try {
      Writer writer = new BufferedWriter(
          new OutputStreamWriter(
              new FileOutputStream(new File("./account.csv")), "UTF-8"));
      String header = "tuser,tpassword,flag\r\n";
      writer.write(header);
      for (int i = 0; i < userInfos.size(); i++) {
        /*TransferUserInfo info = userInfos.get(i);
        StringBuffer str = new StringBuffer();
        str.append(info.getUserName().toString() + ","
            + info.getPassword().toString() + ","
            + info.getFlag().toString() + "\r\n");
        writer.write(str.toString());
        writer.flush();*/
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static int countResult(final List<TransferUserInfo> userInfos, String flag) {
    return (int) userInfos.stream()
        .filter(u -> u.getFlag().equals(flag))
        .count();
  }
}
