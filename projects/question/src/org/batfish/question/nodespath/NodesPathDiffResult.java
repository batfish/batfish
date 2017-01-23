package org.batfish.question.nodespath;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;

import com.fasterxml.jackson.databind.JsonNode;

public class NodesPathDiffResult {

   private SortedMap<ConcretePath, JsonNode> _added;

   private NodesPath _path;

   private SortedMap<ConcretePath, JsonNode> _removed;

   public NodesPathDiffResult(NodesPathResult before, NodesPathResult after) {
      _added = new TreeMap<>();
      _removed = new TreeMap<>();
      _path = before.getPath();
      SortedMap<ConcretePath, JsonNode> beforeResult = before.getResult();
      SortedMap<ConcretePath, JsonNode> afterResult = after.getResult();
      Set<ConcretePath> beforeKeys = beforeResult.keySet();
      Set<ConcretePath> afterKeys = afterResult.keySet();
      Set<ConcretePath> removed = CommonUtil.difference(beforeKeys, afterKeys,
            TreeSet::new);
      Set<ConcretePath> added = CommonUtil.difference(afterKeys, beforeKeys,
            TreeSet::new);
      Set<ConcretePath> common = CommonUtil.intersection(afterKeys, beforeKeys,
            TreeSet::new);
      for (ConcretePath removedPath : removed) {
         _removed.put(removedPath, beforeResult.get(removedPath));
      }
      for (ConcretePath addedPath : added) {
         _added.put(addedPath, afterResult.get(addedPath));
      }
      for (ConcretePath commonPath : common) {
         JsonNode beforeNode = beforeResult.get(commonPath);
         JsonNode afterNode = afterResult.get(commonPath);
         if (!beforeNode.equals(afterNode)) {
            _removed.put(commonPath, beforeNode);
            _added.put(commonPath, afterNode);
         }
      }
   }

   public SortedMap<ConcretePath, JsonNode> getAdded() {
      return _added;
   }

   public NodesPath getPath() {
      return _path;
   }

   public SortedMap<ConcretePath, JsonNode> getRemoved() {
      return _removed;
   }

   public void setAdded(SortedMap<ConcretePath, JsonNode> added) {
      _added = added;
   }

   public void setPath(NodesPath path) {
      _path = path;
   }

   public void setRemoved(SortedMap<ConcretePath, JsonNode> removed) {
      _removed = removed;
   }

}
