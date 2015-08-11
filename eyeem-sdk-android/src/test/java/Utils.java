import org.robolectric.RuntimeEnvironment;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by vishna on 11/08/15.
 */
public class Utils {

   public static String readFile(String fileName) throws Exception {
      String filePath = RuntimeEnvironment.application.getPackageResourcePath() + "/src/test/assets/" + fileName;

      BufferedReader br = new BufferedReader(new FileReader(filePath));
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
         sb.append(line);
         line = br.readLine();
      }
      return sb.toString();
   }
}
