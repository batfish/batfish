package org.batfish.question.jsonpath;

import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPathResult {

   private Integer _numResults;

   private JsonPathQuery _path;

   private SortedMap<ConcreteJsonPath, JsonNode> _result;

   public JsonPathResult() {
      _result = new TreeMap<>();
   }

   public Integer getNumResults() {
      return _numResults;
   }

   public JsonPathQuery getPath() {
      return _path;
   }

   public SortedMap<ConcreteJsonPath, JsonNode> getResult() {
      return _result;
   }

   public void setNumResults(Integer numResults) {
      _numResults = numResults;
   }

   public void setPath(JsonPathQuery path) {
      _path = path;
   }

   public void setResult(SortedMap<ConcreteJsonPath, JsonNode> result) {
      _result = result;
   }

}
