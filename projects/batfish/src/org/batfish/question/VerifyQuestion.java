package org.batfish.question;

public class VerifyQuestion extends Question {

   private VerifyProgram _program;

   public VerifyQuestion() {
      super(QuestionType.VERIFY);
      _program = new VerifyProgram();
   }

   public VerifyProgram getProgram() {
      return _program;
   }

}
