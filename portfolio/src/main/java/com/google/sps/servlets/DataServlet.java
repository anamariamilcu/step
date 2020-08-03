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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.lang.Exception;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import com.google.appengine.api.images.ImagesServiceFailureException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;

/** This class is responsible for loading comments and posting new ones. */
@WebServlet("/comment-section")
public class DataServlet extends HttpServlet {

  final Integer defaultCommentsNumber = 4;
  final SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yy kk:mm:ss");
  // TODO support local timezones. For now let it general.
  final TimeZone timeZone = TimeZone.getTimeZone("UTC");
  final Date date = new Date();
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");

    String commentsOrder;
    commentsOrder = request.getParameter("comments_order");
    // If there is no commentsorder query, make it descending order.
    if (commentsOrder == null || commentsOrder.equals("DESC")) {
      query.addSort("timestamp", SortDirection.DESCENDING);
    } else if (commentsOrder.equals("ASC")) {
      query.addSort("timestamp", SortDirection.ASCENDING);
    } else {
      // If the data provided by the user is invalid send a 400 Bad Request.
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    /**
    * Limit the number of comments to a value chosen by the user.
    * Extract that value from a query string.
    * If it does not exist, show 4 comments.
    */

    Integer commentsNumber = defaultCommentsNumber;
    Integer commentsCount = 0;
    /* Avoid null pointer exception. */
    if (request.getParameter("comments_number") != null) {
      try {
        commentsNumber = Integer.parseInt(request.getParameter("comments_number"));
      } catch (NumberFormatException e) {
        // If the data provided by the user is invalid send a 400 Bad Request.
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
      }
    }
    /* In the html page, -1 is used as a value that represents show all comments. */
    if (commentsNumber == -1) {
      commentsNumber = results.countEntities();
    }

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String username = (String) entity.getProperty("username");
      String text = (String) entity.getProperty("text");
      String date = (String) entity.getProperty("date");
      long timestamp = (long) entity.getProperty("timestamp");
      String imageURL = (String) entity.getProperty("imageURL");
      Comment comment;
      comment = new Comment(id, username, text, date, timestamp, imageURL);
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
    // Add new comment to the comment history.
    Entity commentEntity = new Entity("Comment");
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /* The entity has a field called username. This field will keep the email
    or the nickname. If the user doesn't have a problem with his email be
    displayed near his comments and doesn't set any nickname, then username
    = email. Otherwise, if he has a nickname, then username = nickname. */ 

    // Use NicknameServlet class to get current nickname.
    NicknameServlet nicknameServletObj = new NicknameServlet();
    String nickname = nicknameServletObj.getUserNickname(userService.getCurrentUser().getUserId());
    commentEntity.setProperty("username", nickname);

    commentEntity.setProperty("text", request.getParameter("comment"));
    commentEntity.setProperty("date", DateFor.format(date));
    commentEntity.setProperty("timestamp", date.getTime());

    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, response, "image");
    if (imageUrl != null) {
      commentEntity.setProperty("imageURL", imageUrl);
    }
    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, HttpServletResponse response, String formInputElementName) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Check the validity of the file here, to make sure it's an image file.
    if (blobInfo.getContentType()..startsWith("image/") == false) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only images allowed!");
      blobstoreService.delete(blobKey);
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    } catch (ImagesServiceFailureException e) {
      return blobKey.getKeyString();
    }
  }
}
