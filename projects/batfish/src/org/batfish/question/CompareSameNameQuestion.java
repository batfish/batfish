package org.batfish.question;

public final class CompareSameNameQuestion extends Question {

   public CompareSameNameQuestion(QuestionParameters parameters) {
      super(QuestionType.COMPARE_SAME_NAME, parameters);
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
