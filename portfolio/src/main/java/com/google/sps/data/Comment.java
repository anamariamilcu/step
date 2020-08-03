package com.google.sps.data;

import java.util.Date; 

/* Class that represents a comment object */

public class Comment {
  //TODO use AutoValue
  private final long id;
  /* The username field represents the email of the user or if he has
  a nickname setted, the nickname. This way, if the user does not want the
  email to be displayed on the page, the comment will be signed with the
  nickname, so the user chooses how to identify on the page.*/
  private final String username;
  private final String text;
  private final String date;
  private final long timestamp;
  private final String imageURL;

  public Comment(long id, String username, String text, String date, long timestamp, String imageURL) {
    this.id = id;
    this.username = username;
    this.text = text;
    this.date = date;
    this.timestamp = timestamp;
    this.imageURL = imageURL;
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

  public String getImageURL() {
    return imageURL;
  }
}
