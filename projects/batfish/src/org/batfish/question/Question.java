package org.batfish.question;

public class Question {

   private String _masterEnvironment;

   private final QuestionType _type;

   public Question(QuestionType type) {
      _type = type;
   }

   public String getMasterEnvironment() {
      return _masterEnvironment;
   }

   public QuestionType getType() {
      return _type;
   }

   public void setMasterEnvironment(String masterEnvironment) {
      _masterEnvironment = masterEnvironment;
   }

}
