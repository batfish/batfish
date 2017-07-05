package org.batfish.representation.aws_vpcs;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Utils {

   public static boolean tryGetBoolean(
         JSONObject jsonObject, String key,
         boolean defaultValue) throws JSONException {
      if (jsonObject.has(key)) {
         return jsonObject.getBoolean(key);
      }
      return defaultValue;
   }

   public static int tryGetInt(
         JSONObject jsonObject, String key,
         int defaultValue) throws JSONException {
      if (jsonObject.has(key)) {
         return jsonObject.getInt(key);
      }
      return defaultValue;
   }

   public static String tryGetString(JSONObject jsonObject, String key)
         throws JSONException {
      if (jsonObject.has(key)) {
         return jsonObject.getString(key);
      }
      return null;
   }
}
