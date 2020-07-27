// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.sps.data.Comment;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.Exception;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;

@WebServlet("/comment-section")
public class DataServlet extends HttpServlet {

  Integer defaultCommentsNumber = 4;
  SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yy kk:mm:ss");
  TimeZone timeZone = TimeZone.getTimeZone("Europe/Bucharest");
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");
    String commentsOrder;
    try {
      commentsOrder = request.getParameter("commentsorder");
      if (commentsOrder.equals("DESC")) {
        query.addSort("timestamp", SortDirection.DESCENDING);
      } else {
        query.addSort("timestamp", SortDirection.ASCENDING);
      }
    } catch (Exception e) {
      //No sorting.
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

     /**
    * Limit the number of comments to a value chosen by the user.
    * Extract that value from a query string.
    * If it does not exist, show 4 comments.
    */

    Integer commentsNumber;
    Integer commentsCount = 0;

    try {
      commentsNumber = Integer.parseInt(request.getParameter("commentsnumber"));
      if (commentsNumber == 1) {
          commentsNumber = results.countEntities();
      }
    } catch (Exception e) {
      commentsNumber = defaultCommentsNumber;
    }

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String username = (String) entity.getProperty("username");
      String text = (String) entity.getProperty("text");
      String date = (String) entity.getProperty("date");
      long timestamp = (long) entity.getProperty("timestamp");
      Comment comment;
      comment = new Comment(id, username, text, date, timestamp);
      comments.add(comment);
      commentsCount++;
      if (commentsCount == commentsNumber) {
        break;
      }
    }
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the converted input from form.
    Comment newComment = getCommentFromForm(request);
    // Add it to the comment history.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", newComment.getUsername());
    commentEntity.setProperty("text", newComment.getText());
    commentEntity.setProperty("date", newComment.getDate());
    commentEntity.setProperty("timestamp", newComment.getTimestamp());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("index.html");
  }

  private Comment getCommentFromForm(HttpServletRequest request) {
    // Get the input from the form and make it a Comment object.
    String usernameString = request.getParameter("username");
    String commentString = request.getParameter("comment");
    DateFor.setTimeZone(timeZone);
    return new Comment(0, usernameString, commentString, 
      DateFor.format(new Date()), System.currentTimeMillis());
  }
}
