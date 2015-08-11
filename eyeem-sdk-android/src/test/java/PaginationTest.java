import com.eyeem.sdk.BuildConfig;
import com.eyeem.sdk.EyeEm;
import com.eyeem.sdk.News;
import com.eyeem.sdk.Photo;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by vishna on 11/08/15.
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class PaginationTest {

   JSONObject news_feed;
   JSONObject photos_feed;

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

}
