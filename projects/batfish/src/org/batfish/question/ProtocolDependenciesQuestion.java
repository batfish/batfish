package org.batfish.question;

public class ProtocolDependenciesQuestion extends Question {

   public ProtocolDependenciesQuestion(QuestionParameters parameters) {
      super(QuestionType.PROTOCOL_DEPENDENCIES, parameters);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

}
