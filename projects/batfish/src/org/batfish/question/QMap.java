package org.batfish.question;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.util.NamedStructure;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class QMap extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Map<String, QMap> _maps;

   private final Map<String, String> _strings;

   private final Map<String, VariableType> _typeBindings;

   public QMap(String name) {
      super(name);
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

   public JSONObject toJsonObject() throws JSONException {
      JSONObject output = new JSONObject();
      output.put("name", _name);
      for (Entry<String, String> e : _strings.entrySet()) {
         String key = e.getKey();
         String value = e.getValue();
         output.put(key, value);
      }
      for (Entry<String, QMap> e : _maps.entrySet()) {
         String key = e.getKey();
         QMap value = e.getValue();
         JSONObject valueObj = value.toJsonObject();
         output.put(key, valueObj);
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
