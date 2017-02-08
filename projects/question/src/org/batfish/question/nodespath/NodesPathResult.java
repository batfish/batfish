package org.batfish.question.nodespath;

import java.util.SortedMap;

import com.fasterxml.jackson.databind.JsonNode;

public class NodesPathResult {

   private Integer _numResults;

   private NodesPath _path;

   private SortedMap<ConcretePath, JsonNode> _result;

   public Integer getNumResults() {
      return _numResults;
   }

   public NodesPath getPath() {
      return _path;
   }

   public SortedMap<ConcretePath, JsonNode> getResult() {
      return _result;
   }

   public void setNumResults(Integer numResults) {
      _numResults = numResults;
   }

   public void setPath(NodesPath path) {
      _path = path;
   }

   public void setResult(SortedMap<ConcretePath, JsonNode> result) {
      _result = result;
   }

}
