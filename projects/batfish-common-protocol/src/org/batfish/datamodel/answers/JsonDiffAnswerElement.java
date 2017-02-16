package org.batfish.datamodel.answers;

import org.batfish.common.util.JsonDiff;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonDiffAnswerElement implements AnswerElement {

   private static final String JSON_DIFF_VAR = "jsonDiff";

   private final JsonDiff _jsonDiff;

   @JsonCreator
   public JsonDiffAnswerElement(
         @JsonProperty(JSON_DIFF_VAR) JsonDiff jsonDiff) {
      _jsonDiff = jsonDiff;
   }

   @JsonProperty(JSON_DIFF_VAR)
   public JsonDiff getJsonDiff() {
      return _jsonDiff;
   }

   @Override
   public String prettyPrint() {
      final StringBuilder sb = new StringBuilder(
            "Difference between base and delta\n");
      sb.append(_jsonDiff.prettyPrint("  "));
      return sb.toString();

   }
}
