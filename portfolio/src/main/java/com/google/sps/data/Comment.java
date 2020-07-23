package com.google.sps.data;

/* Class that represents how a comment object */

public class Comment {
  private final String username;
  private String text;

  public Comment(String username, String text) {
    this.username = username;
    this.text = text;
  }

  public String getUsername() {
    return username;
  }

  public String getText() {
    return text;
  }

  public String setText() {
    return text;
  }
}
