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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
   * Class that manages setting and editing users' nicknames.
   */
@WebServlet("/nickname")
public class NicknameServlet extends HttpServlet {

  /* Get the current nickname, if it exists. Returns an empty string otherwise. 
     This function is used to set the default value in the form for nickname
     set up, this way is easier for the user to edit it.*/
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");

    UserService userService = UserServiceFactory.getUserService();

    String nickname = getUserNickname(userService.getCurrentUser().getUserId());
    response.getWriter().println(nickname);
  }

  /**
   * If the user is not logged in, the form is hidden, so no post request can be
   * performed, no need for checking if the user is logged.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String nickname = request.getParameter("nickname");
    String id = userService.getCurrentUser().getUserId();

    String oldUsername = getUserNickname(id);
    /* Get all comments made with previous nickname. */
    Query query =
        new Query("Comment")
            .setFilter(new Query.FilterPredicate("username", Query.FilterOperator.EQUAL, oldUsername));
    PreparedQuery results = datastore.prepare(query);

    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("nickname", nickname);
    /* The put() function automatically inserts new data or updates existing
      data based on ID. */
    datastore.put(entity);
    /* For the comments that were made with the previous nickname(or mail if it
       did not exist), change the username field, so it will show the updated
       one. */
    for (Entity commEntity : results.asIterable()) {
      System.out.println(commEntity.getProperty("username"));
      commEntity.setProperty("username", nickname);
      System.out.println(nickname);
      datastore.put(commEntity);
    }

    response.sendRedirect("/index.html");
  }

  /**
   * Returns the nickname of the user with id, or the user's email if the user has not set a nickname.
   */
  public String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return userService.getCurrentUser().getEmail();
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}
