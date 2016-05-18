package org.batfish.datamodel.questions;

public class ErrorQuestion extends Question {

   public ErrorQuestion() {
      super(QuestionType.ERROR);
   }

   @Override
   public boolean getDataPlane() {
      return false;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

}
