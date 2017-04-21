package org.batfish.question.jsonpath;

import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonPathResult {

   public static class JsonPathResultEntry {

      private static final String CONCRETE_PATH_VAR = "concretePath";

      private static final String SUFFIX_VAR = "suffix";

      private final ConcreteJsonPath _concretePath;

      private final JsonNode _suffix;

      @JsonCreator
      public JsonPathResultEntry(
            @JsonProperty(CONCRETE_PATH_VAR) ConcreteJsonPath concretePath,
            @JsonProperty(SUFFIX_VAR) JsonNode suffix) {
         _concretePath = concretePath;
         if (suffix != null && suffix.isNull()) {
            _suffix = null;
         }
         else {
            _suffix = suffix;
         }
      }

      @JsonProperty(CONCRETE_PATH_VAR)
      public ConcreteJsonPath getConcretePath() {
         return _concretePath;
      }

      @JsonProperty(SUFFIX_VAR)
      public JsonNode getSuffix() {
         return _suffix;
      }

   }

   private Integer _numResults;

   private JsonPathQuery _path;

   private SortedMap<String, JsonPathResultEntry> _result;

   public JsonPathResult() {
      _result = new TreeMap<>();
   }

   public Integer getNumResults() {
      return _numResults;
   }

   public JsonPathQuery getPath() {
      return _path;
   }

   public SortedMap<String, JsonPathResultEntry> getResult() {
      return _result;
   }

   public void setNumResults(Integer numResults) {
      _numResults = numResults;
   }

   public void setPath(JsonPathQuery path) {
      _path = path;
   }

   public void setResult(SortedMap<String, JsonPathResultEntry> result) {
      _result = result;
   }

}
