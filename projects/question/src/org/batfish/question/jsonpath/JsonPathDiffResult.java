package org.batfish.question.jsonpath;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPathDiffResult {

   private SortedMap<ConcreteJsonPath, JsonNode> _added;

   private JsonPathQuery _path;

   private SortedMap<ConcreteJsonPath, JsonNode> _removed;

   public JsonPathDiffResult(JsonPathResult before, JsonPathResult after) {
      _added = new TreeMap<>();
      _removed = new TreeMap<>();
      _path = before.getPath();
      SortedMap<ConcreteJsonPath, JsonNode> beforeResult = before.getResult();
      SortedMap<ConcreteJsonPath, JsonNode> afterResult = after.getResult();
      Set<ConcreteJsonPath> beforeKeys = beforeResult.keySet();
      Set<ConcreteJsonPath> afterKeys = afterResult.keySet();
      Set<ConcreteJsonPath> removed = CommonUtil.difference(beforeKeys,
            afterKeys, TreeSet::new);
      Set<ConcreteJsonPath> added = CommonUtil.difference(afterKeys, beforeKeys,
            TreeSet::new);
      Set<ConcreteJsonPath> common = CommonUtil.intersection(afterKeys,
            beforeKeys, TreeSet::new);
      for (ConcreteJsonPath removedPath : removed) {
         _removed.put(removedPath, beforeResult.get(removedPath));
      }
      for (ConcreteJsonPath addedPath : added) {
         _added.put(addedPath, afterResult.get(addedPath));
      }
      for (ConcreteJsonPath commonPath : common) {
         JsonNode beforeNode = beforeResult.get(commonPath);
         JsonNode afterNode = afterResult.get(commonPath);
         if (!beforeNode.equals(afterNode)) {
            _removed.put(commonPath, beforeNode);
            _added.put(commonPath, afterNode);
         }
      }
   }

   public SortedMap<ConcreteJsonPath, JsonNode> getAdded() {
      return _added;
   }

   public JsonPathQuery getPath() {
      return _path;
   }

   public SortedMap<ConcreteJsonPath, JsonNode> getRemoved() {
      return _removed;
   }

   public void setAdded(SortedMap<ConcreteJsonPath, JsonNode> added) {
      _added = added;
   }

   public void setPath(JsonPathQuery path) {
      _path = path;
   }

   public void setRemoved(SortedMap<ConcreteJsonPath, JsonNode> removed) {
      _removed = removed;
   }

}
