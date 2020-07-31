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

import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  /* Class used to send data as JSON to the web page regarding user login status. */
  static class LoginData {
    /* Field that contains the user that is currently logged in. If it's empty,
    no user is logged in. */
    final private String userEmail;
    /* Field that represents the link where the user will be redirected. They
      can either log in or either log out, depending on their status. */
    final private String logInOutUrl;

    public LoginData(String userEmail, String logInOutUrl) {
      this.userEmail = userEmail;
      this.logInOutUrl = logInOutUrl;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    LoginData loginData;
    UserService userService = UserServiceFactory.getUserService();
    
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/index.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      loginData = new LoginData(userEmail, logoutUrl);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/index.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      /* No email is neccesary. If that field is blank, that means the user
         is not logged in.*/
      loginData = new LoginData("", loginUrl);
    }

    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(loginData));
  }
}
