package org.batfish.question;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolDependenciesQuestionPlugin extends QuestionPlugin {

   public static class ProtocolDependenciesAnswerElement
         implements AnswerElement {

      private String _zipBase64;

      public String getZipBase64() {
         return _zipBase64;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setZipBase64(String zipBase64) {
         _zipBase64 = zipBase64;
      }

   }

   public static class ProtocolDependenciesAnswerer extends Answerer {

      public ProtocolDependenciesAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         String zipBase64 = _batfish.answerProtocolDependencies();
         ProtocolDependenciesAnswerElement answerElement = new ProtocolDependenciesAnswerElement();
         answerElement.setZipBase64(zipBase64);
         return answerElement;
      }

   }

   public static class ProtocolDependenciesQuestion extends Question {

      public ProtocolDependenciesQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "protocoldependencies";
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new ProtocolDependenciesAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new ProtocolDependenciesQuestion();
   }

}
