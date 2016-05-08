package org.batfish.question;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.common.BatfishException;
import org.batfish.common.datamodel.questions.VariableType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class QMap {

   private final Map<String, QMap> _maps;

   private final Map<String, String> _strings;

   private final Map<String, VariableType> _typeBindings;

   public QMap() {
      _maps = new LinkedHashMap<String, QMap>();
      _strings = new LinkedHashMap<String, String>();
      _typeBindings = new HashMap<String, VariableType>();
   }

   public Map<String, QMap> getMaps() {
      return _maps;
   }

   public Map<String, String> getStrings() {
      return _strings;
   }

   public Map<String, VariableType> getTypeBindings() {
      return _typeBindings;
   }

   public JSONObject toJsonObject() {
      JSONObject output = new JSONObject();
      for (Entry<String, String> e : _strings.entrySet()) {
         String key = e.getKey();
         String value = e.getValue();
         try {
            output.put(key, value);
         }
         catch (JSONException e2) {
            throw new BatfishException("Error converting to json object", e2);
         }
      }
      for (Entry<String, QMap> e : _maps.entrySet()) {
         String key = e.getKey();
         QMap value = e.getValue();
         JSONObject valueObj = value.toJsonObject();
         try {
            output.put(key, valueObj);
         }
         catch (JSONException e3) {
            throw new BatfishException("Error converting to json object", e3);
         }
      }
      return output;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      JSONObject obj = null;
      try {
         obj = toJsonObject();
         String jsonText = obj.toString(3);
         sb.append(jsonText);
         sb.append("\n");
      }
      catch (JSONException e) {
         throw new BatfishException("Error converting to json object", e);
      }
      return sb.toString();
   }

}
