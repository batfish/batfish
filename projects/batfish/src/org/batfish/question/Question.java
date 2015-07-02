package org.batfish.question;

public abstract class Question {

   private final QuestionType _type;

   public Question(QuestionType type) {
      _type = type;
   }

   public QuestionType getType() {
      return _type;
   }

}
