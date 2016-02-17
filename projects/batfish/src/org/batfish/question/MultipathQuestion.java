package org.batfish.question;

public class MultipathQuestion extends Question {

   public MultipathQuestion(QuestionParameters parameters) {
      super(QuestionType.MULTIPATH, parameters);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

}
