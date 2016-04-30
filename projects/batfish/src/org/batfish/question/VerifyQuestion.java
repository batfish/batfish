package org.batfish.question;

public class VerifyQuestion extends Question {

   private VerifyProgram _program;

   public VerifyQuestion(QuestionParameters parameters) {
      super(QuestionType.VERIFY, parameters);
      _program = new VerifyProgram(parameters);
   }

   @Override
   public boolean getDataPlane() {
      return _program.getDataPlane();
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   public VerifyProgram getProgram() {
      return _program;
   }

}
