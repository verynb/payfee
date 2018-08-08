package load;

import com.google.common.collect.Lists;
import com.mail.api.TransferUserInfo;
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
import java.util.Optional;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LoadData {

  private static Logger logger = LoggerFactory.getLogger(LoadData.class);
  private static String HEADER = "";

  public static List<TransferUserInfo> loadUserInfoData(String filePath) {
    CsvReader csvReader = new CsvReader();
    csvReader.setContainsHeader(false);
    CsvContainer csv = null;
    List<TransferUserInfo> userInfos = Lists.newArrayList();
    try {
      csv = csvReader.read(Paths.get(filePath), StandardCharsets.UTF_8);
      if (csv == null) {
      }
      int i = 0;
      for (CsvRow row : csv.getRows()) {

        if (i++ == 0) {
          int cont = row.getFieldCount();
          String one = row.getField(0);
          String two = row.getField(1);
          String three = row.getField(2);
          String four = row.getField(3);
          String five = row.getField(4);
          String six = row.getField(5);
          String seven = row.getField(6);
          HEADER =
              one + "," + two + "," + three + "," + four + "," + five + "," + six + "," + String.valueOf(seven) + ","
                  + "结果标识,结果描述\r\n";
        } else {
          int cont = row.getFieldCount()-1;
          TransferUserInfo userInfo = new TransferUserInfo(
              row.getField(0>cont?cont:0),
              row.getField(1>cont?cont:1),
              row.getField(2>cont?cont:2),
              row.getField(3>cont?cont:3),
              row.getField(4>cont?cont:4),
              row.getField(5>cont?cont:5),
              row.getField(6>cont?cont:6) == null ? 200 : Double.valueOf(row.getField(6>cont?cont:6)),
              "", "",
              i
          );
          if (userInfo.filterUserInfo()) {
            userInfos.add(userInfo);
          }
        }
      }
    } catch (Exception e) {
      logger.info("加载用户数据失败,请检查" + filePath + "格式是否正确");
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


  public static void writeResult(final List<TransferUserInfo> userInfos, String filePath) {
    try {
      Writer writer = new BufferedWriter(
          new OutputStreamWriter(
              new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8));
      writer.write(HEADER);
      for (int i = 0; i < userInfos.size(); i++) {
        TransferUserInfo info = userInfos.get(i);
        StringBuffer str = new StringBuffer();
        str.append(info.getUserName().toString() + ","
            + info.getPassword().toString() + ","
            + info.getPuserName().toString() + ","
            + info.getPpassword().toString() + ","
            + info.getPmail().toString() + ","
            + info.getPmailPassword().toString() + ","
            + info.getTransferAmount() + ","
            + Optional.ofNullable(info.getFlag()).orElse("0").toString() + ","
            + Optional.ofNullable(info.getFlagMessage()).orElse("未知错误").toString() + "\r\n");
        writer.write(str.toString());
        writer.flush();
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
