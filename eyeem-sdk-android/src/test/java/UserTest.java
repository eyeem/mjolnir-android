import com.eyeem.sdk.*;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by vishna on 27/08/15.
 */
@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class UserTest {

   JSONObject userJSON;

   @Before public void setup() throws Exception {
      userJSON = new JSONObject(Utils.readFile("user.json")).optJSONObject("user");

      ShadowApplication application = ShadowApplication.getInstance();
      Assert.assertNotNull(application);
   }

   @Test public void testMarketTotals() throws Exception {
      User user = new User(userJSON);
      assertNotNull(user.marketTotals);
      assertEquals("user.marketTotals.editorial", 95, user.marketTotals.editorial);
      assertEquals("user.marketTotals.commercial", 112, user.marketTotals.commercial);
      assertEquals("user.marketTotals.getty", 18, user.marketTotals.getty);
   }

}
