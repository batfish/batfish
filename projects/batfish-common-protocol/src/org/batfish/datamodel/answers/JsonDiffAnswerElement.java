package org.batfish.datamodel.answers;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.JsonDiff;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDiffAnswerElement implements AnswerElement {

   private static final String JSON_DIFF_VAR = "jsonDiff";

   private final JsonDiff _jsonDiff;

   @JsonCreator
   public JsonDiffAnswerElement(@JsonProperty(JSON_DIFF_VAR) JsonDiff jsonDiff) {
      _jsonDiff = jsonDiff;
   }

   @JsonProperty(JSON_DIFF_VAR)
   public JsonDiff getJsonDiff() {
      return _jsonDiff;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
