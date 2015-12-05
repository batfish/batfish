package org.batfish.question;

public class VerifyQuestion extends Question {

   private VerifyProgram _program;

   public VerifyQuestion(QuestionParameters parameters) {
      super(QuestionType.VERIFY);
      _program = new VerifyProgram(parameters);
   }

   public VerifyProgram getProgram() {
      return _program;
   }

}
