package org.batfish.datamodel.questions;

public class DestinationQuestion extends Question {

   public DestinationQuestion(QuestionParameters parameters) {
      super(QuestionType.DESTINATION, parameters);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return true;
   }

}
