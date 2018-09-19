package identity;

import com.google.common.collect.Lists;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Administrator on 2017/12/9.
 */
public class IdentityCheck {

  private static final int DEFAULT_SALT_SIZE = 32;
  private static final int HASH_ITERATIONS = 1000;
  public static final String EMPTY_STRING = "";
  private static final int HASH_KEY_LENGTH = 192;
  //密码：bit123456
  private static final String PASSWORD = "WmRlc3BtbkJZREZGcU5XRzM0clJVUXJrdG1TTElqUFZUdVk2bEVYdkx0cz0=$qpM2VGiDCLtyfML1TnI9hh7Gu2iMvD6l";

  public static void checkVersion(String version, String currentVer) {

    if (!version.equals(currentVer)) {
      System.out.println("当前版本[" + version + "]非最新版本,请更新后使用");
      System.out.print("输入任意结束:");
      Scanner scan = new Scanner(System.in);
      String read = scan.nextLine();
      while (StringUtils.isBlank(read)) {
      }
      System.exit(0);
    } else {
      return;
    }
  }

  public static void checkIdentity(String time) {
    if (!TimeCheck.checkMonth(time)) {
      System.out.println("使用到期，请联系管理员");
      System.out.print("输入任意结束:");
      Scanner scan = new Scanner(System.in);
      String read = scan.nextLine();
      while (StringUtils.isBlank(read)) {
      }
      System.exit(0);
    } else {
      return;
    }
  }

  public static Boolean checkPassword(int times,String password) {
    for (int i = 1; i <= times; i++) {
      System.out.println("请输入密码:");
      Scanner scan = new Scanner(System.in);
      String read = scan.next();
      while (StringUtils.isBlank(read)) {
      }
      if (isValidPassword(read, password)) {
        return true;
      } else {
        System.out.println("密码错误请重新输入,剩余" + (times - i) + "次机会");
      }
    }
    return false;
  }

  private static boolean isValidPassword(String password, String hashedPassword) {
    String[] saltAndPass = hashedPassword.split("\\$");
    if (saltAndPass.length != 2) {
      throw new IllegalStateException(
          "The stored password have the form 'salt$hash'");
    }
    String hashOfInput = hashPasswordAddingSalt(password, Base64.decodeBase64(saltAndPass[0]));
    return hashOfInput.equals(saltAndPass[1]);
  }

  private static String hashPasswordAddingSalt(String password) {
    byte[] salt = generateSalt().getBytes();
    return Base64.encodeBase64String(salt) + '$' + hashPasswordAddingSalt(password, salt);
  }

  private static String hashPasswordAddingSalt(String password, byte[] salt) {
    if (isEmpty(password)) {
      return EMPTY_STRING;
    }
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      SecretKey key = f.generateSecret(new PBEKeySpec(
          password.toCharArray(), salt, HASH_ITERATIONS, HASH_KEY_LENGTH)
      );
      return Base64.encodeBase64String(key.getEncoded());
    } catch (Exception e) {
      return EMPTY_STRING;
    }
  }

  private static boolean isEmpty(String source) {
    return (source == null || "".equals(source)) ? true : false;
  }

  private static String generateSalt() {
    Random r = new SecureRandom();
    byte[] saltBinary = new byte[DEFAULT_SALT_SIZE];
    r.nextBytes(saltBinary);
    return Base64.encodeBase64String(saltBinary);
  }

  public static void main(String[]args){

    List<String> passwords= Lists.newArrayList("sz0124","dg4578","sh1345","jx1235","fs1345","123457");
    passwords.forEach(p ->{
      System.out.println(p+"--->"+hashPasswordAddingSalt(p));
    });
  }
}
