import com.eyeem.sdk.BuildConfig;
import com.eyeem.sdk.EyeEm;
import com.eyeem.sdk.News;
import com.eyeem.sdk.Photo;
import com.eyeem.sdk.pagination.IDPagination;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by vishna on 11/08/15.
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class PaginationTest {

   JSONObject news_feed;
   JSONObject photos_feed;

   /**
    * Utility method mocking process of pagination
    * @param list
    * @param count
    */
   private static void appendPhotos(List<Photo> list, int count) {
      for (int i = 0; i < count; i++) {
         list.add(new Photo());
      }
   }

   @Before public void setup() throws Exception {
      news_feed = new JSONObject(Utils.readFile("news_feed.json"));

      // test sample from https://api.eyeem.com/v2/albums/10359833/photos - "Open Edit"
      // ironically Open Edit is disabled for now
      photos_feed = new JSONObject(Utils.readFile("photos_feed.json"));
   }

   @Test public void testDefaultPagination() {
      EyeEm request = EyeEm.albumPhotos("10359833");
      List<Photo> photosFeed = Photo.fromJSONArray(request.declutter.jsonArray(photos_feed));

      assertEquals("photosFeed.size()", photosFeed.size(), 30);

      EyeEm paginatedRequest = (EyeEm) request.fetchBack(photosFeed);

      assertEquals("pagination Url", "https://api.eyeem.com/v2/albums/10359833/photos?offset=30", paginatedRequest.toUrl());
   }

   @Test public void testNewsPagination() {
      EyeEm request = EyeEm.news();
      List<News> newsFeed = News.fromJSONArray(request.declutter.jsonArray(news_feed));

      assertEquals("newsFeed.size()", newsFeed.size(), 30);

      EyeEm paginatedRequest = (EyeEm) request.fetchBack(newsFeed);

      assertEquals("pagination Url", "https://api.eyeem.com/v2/news?aggregated=1&oldestId=804019524", paginatedRequest.toUrl());
   }

   @Test public void testIDPagination() {

      int limitPerPage = 3;

      ArrayList<Photo> photosFeed = new ArrayList<>();

      IDPagination pagination = new IDPagination(
         Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"),
         limitPerPage);

      EyeEm request = EyeEm.photos(pagination);

      assertEquals("photosFeed.size()", photosFeed.size(), 0);

      EyeEm paginatedRequest = (EyeEm) request.fetchFront(photosFeed);

      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=1,2,3", paginatedRequest.toUrl());
      appendPhotos(photosFeed, limitPerPage);
      assertEquals("photosFeed.size()", photosFeed.size(), 3);

      paginatedRequest = (EyeEm) request.fetchBack(photosFeed);
      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=4,5,6", paginatedRequest.toUrl());
      appendPhotos(photosFeed, limitPerPage);
      assertEquals("photosFeed.size()", photosFeed.size(), 6);

      paginatedRequest = (EyeEm) request.fetchBack(photosFeed);
      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=7,8,9", paginatedRequest.toUrl());
      appendPhotos(photosFeed, limitPerPage);
      assertEquals("photosFeed.size()", photosFeed.size(), 9);

      paginatedRequest = (EyeEm) request.fetchBack(photosFeed);
      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=10,11,12", paginatedRequest.toUrl());
      appendPhotos(photosFeed, limitPerPage);
      assertEquals("photosFeed.size()", photosFeed.size(), 12);

      paginatedRequest = (EyeEm) request.fetchBack(photosFeed);
      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=13", paginatedRequest.toUrl());
      appendPhotos(photosFeed, 1);
      assertEquals("photosFeed.size()", photosFeed.size(), 13);

      // FEED EXHAUSTED
      paginatedRequest = (EyeEm) request.fetchBack(photosFeed);
      assertEquals("pagination Url", "https://api.eyeem.com/v2/photos?ids=13", paginatedRequest.toUrl());
      appendPhotos(photosFeed, 0);
      assertEquals("photosFeed.size()", photosFeed.size(), 13);
   }

}
