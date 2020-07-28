package com.google.sps.data;

import java.util.Date; 

/* Class that represents a comment object */

public class Comment {
  //TODO use AutoValue
  private final long id;
  private final String username;
  private final String text;
  private final String date;
  private final long timestamp;

  public Comment(long id, String username, String text, String date, long timestamp) {
    this.id = id;
    this.username = username;
    this.text = text;
    this.date = date;
    this.timestamp = timestamp;
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

  public String getDate() {
    return date;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
