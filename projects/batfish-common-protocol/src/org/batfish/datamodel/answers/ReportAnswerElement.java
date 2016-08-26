package org.batfish.datamodel.answers;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportAnswerElement implements AnswerElement {

   private List<Object> _jsonAnswers;

   public ReportAnswerElement() {
      _jsonAnswers = new ArrayList<Object>();
   }

   public List<Object> getJsonAnswers() {
      return _jsonAnswers;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setJsonAnswers(List<Object> jsonAnswers) {
      _jsonAnswers = jsonAnswers;
   }

}
