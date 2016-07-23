package org.batfish.datamodel.answers;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringAnswerElement implements AnswerElement {

   private String _answer;

   public StringAnswerElement() {

   }

   public StringAnswerElement(String answer) {
      this();
      setAnswer(answer);
   }

   public String getAnswer() {
      return _answer;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setAnswer(String answer) {
      this._answer = answer;
   }

}
