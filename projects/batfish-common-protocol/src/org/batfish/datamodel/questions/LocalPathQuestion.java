package org.batfish.datamodel.questions;

public class LocalPathQuestion extends Question {

   public LocalPathQuestion() {
      super(QuestionType.LOCAL_PATH);
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return true;
   }

   @Override
   public boolean getTraffic() {
      return true;
   }

}
