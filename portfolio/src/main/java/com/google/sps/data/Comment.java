package com.google.sps.data;

/* Class that represents a comment object */

public class Comment {
  private final long id;
  private final String username;
  private String text;

  public Comment(long id, String username, String text) {
    this.id = id;
    this.username = username;
    this.text = text;
  }

  public long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getText() {
    return text;
  }
}
