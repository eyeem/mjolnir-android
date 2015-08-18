import com.eyeem.sdk.*;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by vishna on 18/08/15.
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class PhotoGroupTest {

   /**
    * Example response from /v2/feed/follow [API 2.3.5]
    */
   JSONObject feed_follow;

   /**
    * Hand crafted version of feed_follow with multiple duplicates
    * that ought to be removed
    */
   JSONObject feed_follow_dedupe;

   public final static String[] GROUP_1_IDS = {"71344207", "71344190", "71344166", "71344131", "71344116", "71344062"};
   public final static String[] GROUP_2_IDS = {"71323423", "71323408", "71323393", "71323375", "71323339", "71323308", "71323296", "71323275"};

   @Before public void setup() throws Exception {
      feed_follow = new JSONObject(Utils.readFile("feed_follow.json"));
      feed_follow_dedupe = new JSONObject(Utils.readFile("feed_follow_dedupe.json"));

      ShadowApplication application = ShadowApplication.getInstance();
      Assert.assertNotNull(application);
   }

   /**
    * Test if object get parsed properly and contain appropriate data
    * @throws Exception
    */
   @Test public void testParse() throws Exception {
      EyeEm request = EyeEm.feedFollow();
      List<FeedItem> feedFollow = FeedItem.fromJSONArray(request.declutter.jsonArray(feed_follow));

      assertEquals("feedFollow.size()", feedFollow.size(), 100);

      List<Integer> ALBUM_POSITIONS = Arrays.asList(6, 13, 20, 27, 34, 41, 48, 55, 62, 69, 76, 83, 90, 97);
      List<Integer> PHOTO_GROUP_POSITIONS = Arrays.asList(16, 66);

      for (int i = 0; i < 100; i++) {
         FeedItem item = feedFollow.get(i);
         if (ALBUM_POSITIONS.contains(i)) assertEquals("isAlbum", FeedItem.TYPE_ALBUM, item.type);
         else if (PHOTO_GROUP_POSITIONS.contains(i)) assertEquals("isPhotoGroup", FeedItem.TYPE_PHOTO_GROUP, item.type);
         else assertEquals("isPhoto", FeedItem.TYPE_PHOTO, item.type);
      }

      FeedItem photoGroup1 = feedFollow.get(16);
      assertNotNull("photoGroup1.id", photoGroup1.id);
      assertTrue("photoGroup1.id startsWith photoGroup", photoGroup1.id.startsWith(FeedItem.TYPE_PHOTO_GROUP));
      assertNotNull("photoGroup1.photoGroup", photoGroup1.photoGroup);
      assertNotNull("photoGroup1.photoGroup.sample", photoGroup1.photoGroup.sample);
      assertEquals("photoGroup1.photoGroup.sample.size()", 5, photoGroup1.photoGroup.sample.size());
      assertNotNull("photoGroup1.photoGroup.sample.get(0).user", photoGroup1.photoGroup.sample.get(0).user);
      assertNotNull("photoGroup1.photoGroup.photoIds", photoGroup1.photoGroup.photoIds);
      assertEquals("photoGroup1.photoGroup.photoIds.size()", 6, photoGroup1.photoGroup.photoIds.size());
      assertEquals("photoGroup1.photoGroup.userId", "14854922", photoGroup1.photoGroup.userId);
      assertArrayEquals(
         "photoGroup1.photoGroup.photoIDs",
         GROUP_1_IDS,
         photoGroup1.photoGroup.photoIds.toArray(new String[photoGroup1.photoGroup.photoIds.size()])
      );

      FeedItem photoGroup2 = feedFollow.get(66);
      assertNotNull("photoGroup2.id", photoGroup2.id);
      assertTrue("photoGroup2.id startsWith photoGroup", photoGroup2.id.startsWith(FeedItem.TYPE_PHOTO_GROUP));
      assertNotNull("photoGroup2.photoGroup", photoGroup2.photoGroup);
      assertNotNull("photoGroup2.photoGroup.sample", photoGroup2.photoGroup.sample);
      assertEquals("photoGroup2.photoGroup.sample.size()", 5, photoGroup2.photoGroup.sample.size());
      assertNotNull("photoGroup2.photoGroup.sample.get(0).user", photoGroup2.photoGroup.sample.get(0).user);
      assertNotNull("photoGroup2.photoGroup.photoIds", photoGroup2.photoGroup.photoIds);
      assertEquals("photoGroup2.photoGroup.photoIds.size()", 8, photoGroup2.photoGroup.photoIds.size());
      assertEquals("photoGroup2.photoGroup.userId", "10476144", photoGroup2.photoGroup.userId);
      assertArrayEquals(
         "photoGroup2.photoGroup.photoIDs",
         GROUP_2_IDS,
         photoGroup2.photoGroup.photoIds.toArray(new String[photoGroup2.photoGroup.photoIds.size()])
      );

      // timestamps are not really reliable here
      assertTrue("photoGroup1 has higher latest id than photoGroup2",
         Long.valueOf(photoGroup1.photoGroup.photoIds.get(0)) > Long.valueOf(photoGroup2.photoGroup.photoIds.get(0))
      );

   }

   /**
    * Test case when we have 2 or more groups sharing same photo ids. This can happen when
    * paginating or when having locally cached groups.
    *
    * Feed Item local IDs:
    *
    * photoGroup1439902489 correct item
    * photoGroup1439901889 duplicate
    * photoGroup1439901289 duplicate
    * photoGroup1439864611 correct item
    * photoGroup1439864011 duplicate
    * photoGroup1439863531 duplicate
    *
    * @throws Exception
    */
   @Test public void testDedupe() throws Exception {
      EyeEm request = EyeEm.feedFollow();
      List<FeedItem> feedFollow = FeedItem.fromJSONArray(request.declutter.jsonArray(feed_follow_dedupe));

      assertEquals("feedFollow.size()", feedFollow.size(), 6);

      assertArrayEquals(
         "proper photoGroup1 check",
         GROUP_1_IDS,
         feedFollow.get(0).photoGroup.photoIds.toArray(new String[feedFollow.get(0).photoGroup.photoIds.size()])
      );

      assertArrayEquals(
         "proper photoGroup2 check",
         GROUP_2_IDS,
         feedFollow.get(3).photoGroup.photoIds.toArray(new String[feedFollow.get(3).photoGroup.photoIds.size()])
      );

      HashSet<String> uniqueIds = new HashSet<>();
      for (FeedItem item : feedFollow) {
         uniqueIds.add(item.id);
      }
      assertEquals("uniqueIds count", 6, uniqueIds.size());

      List<String> idsToBeRemoved = com.eyeem.sdk.Utils.dedupePhotoGroups(feedFollow);

      assertArrayEquals(
         "idsToBeRemoved",
         new String[] {"photoGroup1439901889", "photoGroup1439901289", "photoGroup1439864011", "photoGroup1439863531"},
         idsToBeRemoved.toArray(new String[idsToBeRemoved.size()])
      );
   }
}
