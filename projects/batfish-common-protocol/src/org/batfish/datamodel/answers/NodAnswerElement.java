package org.batfish.datamodel.answers;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NodAnswerElement implements AnswerElement {
   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

}
