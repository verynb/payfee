package com.mail.api;

/**
 * Created by Administrator on 2017/11/30.
 */
public class MailTokenData {
  private  String token;
  private long date;

  public MailTokenData(String token, long date) {
    this.token = token;
    this.date = date;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return "MailTokenData{" +
        "token='" + token + '\'' +
        ", date=" + date +
        '}';
  }
}
