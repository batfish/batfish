package org.batfish.common;

import java.util.List;
import java.util.stream.Collectors;

import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CompositeBatfishException extends RuntimeException
      implements AnswerElement {

   public static class CompositeBatfishExceptionAnswerElement
         implements AnswerElement {

      private BatfishStackTrace _cause;

      private List<BatfishStackTrace> _contributingCauses;

      @JsonCreator
      public CompositeBatfishExceptionAnswerElement() {
      }

      public CompositeBatfishExceptionAnswerElement(BatfishException cause,
            List<BatfishException> contributingCauses) {
         _cause = cause.getBatfishStackTrace();
         _contributingCauses = contributingCauses.stream().map(
               contributingCause -> contributingCause.getBatfishStackTrace())
               .collect(Collectors.toList());
      }

      public BatfishStackTrace getCause() {
         return _cause;
      }

      public List<BatfishStackTrace> getContributingCauses() {
         return _contributingCauses;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setCause(BatfishStackTrace cause) {
         _cause = cause;
      }

      public void setContributingCauses(
            List<BatfishStackTrace> contributingCauses) {
         _contributingCauses = contributingCauses;
      }
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private CompositeBatfishExceptionAnswerElement _answerElement;

   public CompositeBatfishException(BatfishException cause,
         List<BatfishException> contributingCauses) {
      super(cause);
      _answerElement = new CompositeBatfishExceptionAnswerElement(cause,
            contributingCauses);
   }

   public CompositeBatfishExceptionAnswerElement getAnswerElement() {
      return _answerElement;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
