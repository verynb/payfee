package com.transfer.load;

import com.google.common.collect.Lists;
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
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LoadTransferData {

  private static Logger logger = LoggerFactory.getLogger(LoadTransferData.class);

  public static List<TransferUserInfo> loadUserInfoData(String filePath) {
    CsvReader csvReader = new CsvReader();
    csvReader.setContainsHeader(true);
    CsvContainer csv = null;
    List<TransferUserInfo> userInfos = Lists.newArrayList();
    try {
      csv = csvReader.read(Paths.get(filePath), StandardCharsets.UTF_8);
      if (csv == null) {
      }
      int i = 0;
      for (CsvRow row : csv.getRows()) {
        i++;
        TransferUserInfo userInfo = new TransferUserInfo(
            i,
            row.getField("tuser"),
            row.getField("tpassword"),
            row.getField("tmail"),
            row.getField("tmailpassword"),
            row.getField("ruser"),
            row.getField("flag"),
            row.getField("flagMessage"));
        userInfos.add(userInfo);
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

      FileWriter fw = new FileWriter("./account.csv");
      String header = "tuser,tpassword,tmail,tmailpassword,ruser,flag,flagMessage\r\n";
      writer.write(header);
      for (int i = 0; i < userInfos.size(); i++) {
        TransferUserInfo info = userInfos.get(i);
        StringBuffer str = new StringBuffer();
        str.append(
            info.getUserName().toString() + ","
                + info.getPassword().toString() + ","
                + info.getEmail().toString() + ","
                + info.getMailPassword().toString() + ","
                + info.getTransferTo().toString() + ","
                + info.getFlag().toString() + ","
                + info.getFlagMessage().toString() + "\r\n");
        writer.write(str.toString());
        writer.flush();
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
