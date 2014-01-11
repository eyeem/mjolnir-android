package com.eyeem.mjolnir;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by vishna on 31/10/13.
 */
public class PathDeclutter implements Serializable {

   ArrayList<String> keys;

   PathDeclutter() { /*kryo*/ }

   PathDeclutter(String path) {
      keys = new ArrayList<String>();
      for(String node : path.split("\\.")) {
         keys.add(node);
      }
   }

   public JSONArray jsonArray(JSONObject jsonObject) {
      for (int i = 0; i < keys.size() - 1; i++) {
         jsonObject = jsonObject.optJSONObject(keys.get(i));
      }
      return jsonObject.optJSONArray(keys.get(keys.size() - 1));
   }

   public JSONObject jsonObject(JSONObject jsonObject) {
      for (int i = 0; i < keys.size() - 1; i++) {
         jsonObject = jsonObject.optJSONObject(keys.get(i));
      }
      return jsonObject.optJSONObject(keys.get(keys.size() - 1));
   }
}
