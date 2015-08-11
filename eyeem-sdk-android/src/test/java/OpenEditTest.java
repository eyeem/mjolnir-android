import com.eyeem.sdk.BuildConfig;
import com.eyeem.sdk.Photo;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.robolectric.RobolectricGradleTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by vishna on 11/08/15.
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class OpenEditTest {

   JSONObject photo_open_edit_correct;
   JSONObject photo_open_edit_wrong;

   @Before public void setup() throws Exception {
      photo_open_edit_correct = new JSONObject(Utils.readFile("photo_open_edit_correct.json"));
      photo_open_edit_wrong = new JSONObject(Utils.readFile("photo_open_edit_wrong.json"));

      ShadowApplication application = ShadowApplication.getInstance();
      Assert.assertNotNull(application);
   }

   @Test public void testOpenEditClusterduck() throws Exception {
      // the following line was throwing till we applied "the paranoid fix"
      Photo photoWrong = new Photo(photo_open_edit_wrong);
      assertNotNull(photoWrong.openEdit);
      assertFalse(photoWrong.openEdit.perspective);
      assertFalse(photoWrong.openEdit.cropped);
      assertFalse(photoWrong.openEdit.rotated);
      assertNull(photoWrong.openEdit.version);
      assertNull(photoWrong.openEdit.transformations);
   }

   @Test public void testOpenEditV1() throws Exception {
      Photo photoRight = new Photo(photo_open_edit_correct);
      assertNotNull(photoRight.openEdit);
      assertFalse(photoRight.openEdit.perspective);
      assertFalse(photoRight.openEdit.cropped);
      assertFalse(photoRight.openEdit.rotated);
      assertNotNull(photoRight.openEdit.transformations);
      assertEquals(photoRight.openEdit.transformations.size(), 6);
      assertEquals(photoRight.openEdit.version, "1");
   }
}
