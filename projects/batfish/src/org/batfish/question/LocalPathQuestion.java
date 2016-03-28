package org.batfish.question;

public class LocalPathQuestion extends Question {

   public LocalPathQuestion(QuestionParameters parameters) {
      super(QuestionType.LOCAL_PATH, parameters);
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
