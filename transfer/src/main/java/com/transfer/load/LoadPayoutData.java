package com.transfer.load;

import com.google.common.collect.Lists;
import com.transfer.entity.PayOutUserInfo;
import com.transfer.entity.TransferUserInfo;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
public class LoadPayoutData {

  private static Logger logger = LoggerFactory.getLogger(LoadPayoutData.class);

  private static String HEADER = "";

  public static List<PayOutUserInfo> loadUserInfoData(String filePath) {
    CsvReader csvReader = new CsvReader();
    csvReader.setContainsHeader(true);
    CsvContainer csv = null;
    List<PayOutUserInfo> userInfos = Lists.newArrayList();
    try {
      csv = csvReader.read(Paths.get(filePath), StandardCharsets.UTF_8);
      if (csv == null) {
      }
      int i = 0;
      for (CsvRow row : csv.getRows()) {
        if (i++ == 0) {
          String one = row.getField(0);
          String two = row.getField(1);
          String three = row.getField(2);
          String four = row.getField(3);
          String five = row.getField(4);
          String six = row.getField(5);
          HEADER = one + "," + two + "," + three + "," + four + "," + five + "," + six + "," + "结果标识,结果描述\r\n";
        } else {
          PayOutUserInfo userInfo = new PayOutUserInfo(
              i,
              row.getField(0),
              row.getField(1),
              row.getField(2),
              row.getField(3),
              row.getField(4),
              row.getField(5),
              "",
              "");
          userInfos.add(userInfo);
        }
      }
    } catch (IOException e) {
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


  public static void writeResult(List<PayOutUserInfo> userInfos, String userPath) {
    try {

      Writer writer = new BufferedWriter(
          new OutputStreamWriter(
              new FileOutputStream(new File(userPath)), StandardCharsets.UTF_8));

      writer.write(HEADER);
      for (int i = 0; i < userInfos.size(); i++) {
        PayOutUserInfo info = userInfos.get(i);
        StringBuffer str = new StringBuffer();
        str.append(
            info.getAccount().toString() + ","
                + info.getAccountPassword().toString() + ","
                + info.getMailbox().toString() + ","
                + info.getMailboxPassword().toString() + ","
                + info.getWalletName().toString() + ","
                + info.getWalletAddress().toString() + ","
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
