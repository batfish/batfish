package org.batfish.question;

public class MultipathQuestion extends Question {

   private String _masterEnvironment;

   public MultipathQuestion() {
      super(QuestionType.MULTIPATH);
   }

   public String getMasterEnvironment() {
      return _masterEnvironment;
   }

   public void setMasterEnvironment(String masterEnvironment) {
      _masterEnvironment = masterEnvironment;
   }

}
