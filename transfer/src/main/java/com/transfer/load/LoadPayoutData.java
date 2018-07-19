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
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LoadPayoutData {

  private static Logger logger = LoggerFactory.getLogger(LoadPayoutData.class);

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
        i++;
        PayOutUserInfo userInfo = new PayOutUserInfo(
            i,
            row.getField("user"),
            row.getField("pwd"),
            row.getField("mail"),
            row.getField("mail_pwd"),
            row.getField("account_name"),
            row.getField("wallet_add"),
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


  public static void writeResult(List<PayOutUserInfo> userInfos) {
    try {

      Writer writer = new BufferedWriter(
          new OutputStreamWriter(
              new FileOutputStream(new File("./account.csv")), "UTF-8"));

      FileWriter fw = new FileWriter("./account.csv");
      String header = "user,pwd,mail,mail_pwd,account_name,wallet_add,flag,flagMessage\r\n";
      writer.write(header);
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
