package com.mail.api;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class ReceiveEmail {

  private MimeMessage mimeMessage = null;
  private StringBuffer bodyText = new StringBuffer(); // 存放邮件内容的StringBuffer对象
  private String replaceBodyTex = "";

  public ReceiveEmail(MimeMessage mimeMessage) {
    this.mimeMessage = mimeMessage;
    try {
      this.getMailContent(this.mimeMessage);
      replaceBodyTex = replaceBlank(this.bodyText.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Long getSentDate() {
    Date sentDate = null;
    try {
      sentDate = mimeMessage.getSentDate();
    } catch (MessagingException e) {
      e.printStackTrace();
    }
    return sentDate.getTime();
  }

  public String getToken() {
    String token = replaceBodyTex.substring(replaceBodyTex.lastIndexOf("*") + 1, replaceBodyTex.lastIndexOf("*") + 33);
    System.out.println("token[" + token + "]");
    return token;
  }

  public Boolean filterSendUser(String userName) {
    int start = replaceBodyTex.indexOf("**Dear")+6;
    return replaceBodyTex.substring(start, start + userName.length()).equals(userName);
  }

  public void getMailContent(Part part) throws Exception {
    String contentType = part.getContentType();
    int nameIndex = contentType.indexOf("name");
    boolean conName = false;
    if (nameIndex != -1) {
      conName = true;
    }
    if (part.isMimeType("text/plain") && conName == false) {
      // text/plain 类型
      bodyText.append((String) part.getContent());
    } else if (part.isMimeType("text/html") && conName == false) {
      bodyText.append((String) part.getContent());
    } else if (part.isMimeType("multipart/alternative")) {
      Multipart multipart = (Multipart) part.getContent();
      int counts = multipart.getCount();
      for (int i = 0; i < counts; i++) {
        getMailContent(multipart.getBodyPart(i));
      }
    } else if (part.isMimeType("message/rfc822")) {
      getMailContent((Part) part.getContent());
    } else {
    }
  }

  public String replaceBlank(String str) {
    String dest = "";
    if (str != null) {
      Pattern p = Pattern.compile("\\s*|\t|\r|\n");
      Matcher m = p.matcher(str);
      dest = m.replaceAll("");
    }
    return dest;
  }
}