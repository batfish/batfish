package org.batfish.question.jsonpath;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;
import org.batfish.question.jsonpath.JsonPathResult.JsonPathResultEntry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonPathDiffResult {

   private static final String ADDED_VAR = "added";

   private static final String PATH_VAR = "path";

   private static final String REMOVED_VAR = "removed";

   private SortedMap<String, JsonPathResultEntry> _added;

   private JsonPathQuery _path;

   private SortedMap<String, JsonPathResultEntry> _removed;

   public JsonPathDiffResult(JsonPathResult before, JsonPathResult after) {
      _added = new TreeMap<>();
      _removed = new TreeMap<>();
      _path = before.getPath();
      SortedMap<String, JsonPathResultEntry> beforeResult = before.getResult();
      SortedMap<String, JsonPathResultEntry> afterResult = after.getResult();
      Set<String> beforeKeys = beforeResult.keySet();
      Set<String> afterKeys = afterResult.keySet();
      Set<String> removed = CommonUtil.difference(beforeKeys, afterKeys,
            TreeSet::new);
      Set<String> added = CommonUtil.difference(afterKeys, beforeKeys,
            TreeSet::new);
      Set<String> common = CommonUtil.intersection(afterKeys, beforeKeys,
            TreeSet::new);
      for (String removedPath : removed) {
         _removed.put(removedPath, beforeResult.get(removedPath));
      }
      for (String addedPath : added) {
         _added.put(addedPath, afterResult.get(addedPath));
      }
      for (String commonPath : common) {
         JsonPathResultEntry beforeEntry = beforeResult.get(commonPath);
         JsonPathResultEntry afterEntry = afterResult.get(commonPath);
         JsonNode beforeNode = beforeEntry.getSuffix();
         JsonNode afterNode = afterEntry.getSuffix();
         if (!beforeNode.equals(afterNode)) {
            _removed.put(commonPath, beforeEntry);
            _added.put(commonPath, afterEntry);
         }
      }
   }

   @JsonProperty(ADDED_VAR)
   public SortedMap<String, JsonPathResultEntry> getAdded() {
      return _added;
   }

   @JsonProperty(PATH_VAR)
   public JsonPathQuery getPath() {
      return _path;
   }

   @JsonProperty(REMOVED_VAR)
   public SortedMap<String, JsonPathResultEntry> getRemoved() {
      return _removed;
   }

   @JsonProperty(ADDED_VAR)
   public void setAdded(SortedMap<String, JsonPathResultEntry> added) {
      _added = added;
   }

   @JsonProperty(PATH_VAR)
   public void setPath(JsonPathQuery path) {
      _path = path;
   }

   @JsonProperty(REMOVED_VAR)
   public void setRemoved(SortedMap<String, JsonPathResultEntry> removed) {
      _removed = removed;
   }

}
