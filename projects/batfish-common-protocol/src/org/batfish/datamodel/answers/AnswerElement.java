package org.batfish.datamodel.answers;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface AnswerElement {

   String SUMMARY_VAR = "summary";

   @JsonProperty(SUMMARY_VAR)
   default String getSummary() {
      return null;
   }

   default String prettyPrint() {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         return mapper.writeValueAsString(this);
      }
      catch (JsonProcessingException e) {
         throw new BatfishException("Failed to pretty print answer element", e);
      }
   }

   @JsonProperty(SUMMARY_VAR)
   default void setSummary(String summary) {
   }

}
